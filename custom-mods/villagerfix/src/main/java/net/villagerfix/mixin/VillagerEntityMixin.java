package net.villagerfix.mixin;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.villagerfix.VillagerFixMain;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin extends AbstractVillager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private List<MerchantOffers> offerList = new ArrayList<>();
    private List<String> jobList = new ArrayList<>();

    public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = {"readAdditionalSaveData"}, at = {@At("TAIL")})
    public void readAdditionalSaveDataMixin(ValueInput input, CallbackInfo info) {
        for (int i = 0; i < input.getIntOr("JobCount", 0); i++) {
            String jobString = "OldOffer" + i;
            this.jobList.add(input.getStringOr(jobString + "OldWork", ""));
            input.read(jobString, MerchantOffers.CODEC).ifPresent(offers -> this.offerList.add(offers));
        }
    }

    @Inject(method = {"addAdditionalSaveData"}, at = {@At("TAIL")})
    public void addAdditionalSaveDataMixin(ValueOutput output, CallbackInfo info) {
        for (int i = 0; i < this.jobList.size(); i++) {
            String jobString = "OldOffer" + i;
            output.putString(jobString + "OldWork", this.jobList.get(i));
            output.store(jobString, MerchantOffers.CODEC, this.offerList.get(i));
        }

        output.putInt("JobCount", this.jobList.size());
    }

    @Redirect(
            method = {"updateSpecialPrices"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;addToSpecialPriceDiff(I)V", ordinal = 0)
    )
    private void prepareOffersForRedirect(MerchantOffer tradeOffer, int amount) {
        if (tradeOffer.getBaseCostA().getCount() * VillagerFixMain.CONFIG.maxReputationDiscount < Mth.abs(amount)) {
            tradeOffer.addToSpecialPriceDiff(-Mth.floor((float) VillagerFixMain.CONFIG.maxReputationDiscount * tradeOffer.getBaseCostA().getCount()));
        } else {
            tradeOffer.addToSpecialPriceDiff(amount);
        }
    }

    @Redirect(
            method = {"updateSpecialPrices"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;addToSpecialPriceDiff(I)V", ordinal = 1)
    )
    private void prepareOffersForRedirect(MerchantOffer tradeOffer, int amount, Player playerEntity) {
        MobEffectInstance effect = playerEntity.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
        int amplifier = effect == null ? 0 : effect.getAmplifier();
        double d = VillagerFixMain.CONFIG.baseVillagerOfTheHeroDiscount
                + VillagerFixMain.CONFIG.extraVillagerOfTheHeroDiscount * amplifier;
        int k = (int) Math.floor(d * (double) tradeOffer.getBaseCostA().getCount());
        tradeOffer.addToSpecialPriceDiff(-Math.max(k, 0));
    }

    @Inject(
            method = {"setVillagerData"},
            at = {@At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/npc/villager/Villager;getVillagerData()Lnet/minecraft/world/entity/npc/villager/VillagerData;"
            )},
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void setVillagerDataMixin(VillagerData villagerData, CallbackInfo info, VillagerData villagerData2) {
        if (!this.offerList.contains(this.getOffers())) {
            this.offerList.add(this.getOffers());
            this.jobList.add(professionKey(villagerData2));
        }
    }

    @ModifyVariable(
            method = {"updateSpecialPrices"},
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/npc/villager/Villager;getPlayerReputation(Lnet/minecraft/world/entity/player/Player;)I"
            ),
            ordinal = 0
    )
    private int prepareOffersForReputationMixin(int i) {
        if (i > VillagerFixMain.CONFIG.maxReputation) {
            i = VillagerFixMain.CONFIG.maxReputation;
        }

        return i;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            String profession = professionKey(((Villager) (Object) this).getVillagerData());
            int index = this.jobList.indexOf(profession);
            if (index >= 0) {
                this.offers = this.offerList.get(index);
            } else {
                this.offers = new MerchantOffers();
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.updateTrades(serverLevel);
                }
            }
        }

        return this.offers;
    }

    private static String professionKey(VillagerData data) {
        return data.profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }
}
