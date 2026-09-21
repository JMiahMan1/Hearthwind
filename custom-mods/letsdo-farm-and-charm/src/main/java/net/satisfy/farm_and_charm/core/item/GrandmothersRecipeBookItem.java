package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GrandmothersRecipeBookItem extends Item {
    private static final Map<ServerLevel, Map<UUID, Set<Identifier>>> worldUnlockedRecipes = new HashMap<>();

    public GrandmothersRecipeBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            List<Identifier> recipeIds = new ArrayList<>();
            if (data != null && data.copyTag().contains("Recipes")) {
                ListTag list = data.copyTag().getListOrEmpty("Recipes");
                for (int i = 0; i < list.size(); i++) {
                    recipeIds.add(Identifier.parse(list.getString(i).orElse("")));
                }
            } else if (data.copyTag().contains("Recipe")) {
                recipeIds.add(Identifier.parse(data.copyTag().getStringOr("Recipe", "")));
            }
            if (!recipeIds.isEmpty()) {
                for (Identifier id : recipeIds) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.recipe_unlocker.unlocks", id.toString())
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (data != null) {
                List<Identifier> recipeIds = new ArrayList<>();
                if (data.copyTag().contains("Recipes")) {
                    ListTag list = data.copyTag().getListOrEmpty("Recipes");
                    for (int i = 0; i < list.size(); i++) {
                        recipeIds.add(Identifier.parse(list.getString(i).orElse("")));
                    }
                } else if (data.copyTag().contains("Recipe")) {
                    recipeIds.add(Identifier.parse(data.copyTag().getStringOr("Recipe", "")));
                }
                if (!recipeIds.isEmpty()) {
                    ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                    Map<UUID, Set<Identifier>> worldMap = worldUnlockedRecipes.computeIfAbsent(serverLevel, k -> new HashMap<>());
                    Set<Identifier> unlocked = worldMap.computeIfAbsent(serverPlayer.getUUID(), k -> new HashSet<>());
                    Identifier firstId = recipeIds.get(0);
                    if (unlocked.contains(firstId)) {
                        serverPlayer.sendSystemMessage(Component.translatable("tooltip.farm_and_charm.recipe_unlocker.already_unlocked")
                                .withStyle(ChatFormatting.RED), false);
                        return InteractionResult.SUCCESS;
                    }
                    RecipeManager manager = ((ServerLevel) level).recipeAccess();
                    List<RecipeHolder<?>> recipes = new ArrayList<>();
                    for (Identifier id : recipeIds) {
                        Optional<? extends RecipeHolder<?>> opt = manager.byKey(ResourceKey.create(Registries.RECIPE, id));
                        opt.ifPresent(recipes::add);
                        unlocked.add(id);
                    }
                    if (!recipes.isEmpty()) {
                        RecipeHolder<?> firstRecipe = recipes.get(0);
                        ItemStack resultStack = net.satisfy.farm_and_charm.core.recipe.RecipeDisplays26.resultOf(firstRecipe.value());
                        MutableComponent message = Component.literal("")
                                .append(Component.translatable("tooltip.farm_and_charm.recipe_unlocker.unlocked.prefix")
                                        .withStyle(ChatFormatting.YELLOW))
                                .append(Component.literal(" [").withStyle(ChatFormatting.WHITE))
                                .append(resultStack.getHoverName().copy().withStyle(style -> style.withColor(ChatFormatting.WHITE)
                                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowItem(net.minecraft.world.item.ItemStackTemplate.fromStack(resultStack)))))
                                .append(Component.literal("]").withStyle(ChatFormatting.WHITE));
                        serverPlayer.sendSystemMessage(message, false);
                        spawnGoldenParticles(serverPlayer);
                        stack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static ItemStack createUnlockerForRecipes(GrandmothersRecipeBookItem item, String... recipeIds) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        if (recipeIds.length == 1) {
            tag.putString("Recipe", recipeIds[0]);
        } else {
            ListTag list = new ListTag();
            for (String id : recipeIds) {
                list.add(StringTag.valueOf(id));
            }
            tag.put("Recipes", list);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private void spawnGoldenParticles(ServerPlayer serverPlayer) {
        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40) {
                    timer.cancel();
                    return;
                }
                serverLevel.sendParticles(new DustParticleOptions(0xFFFFD600, 1.0f),
                        serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                        5, 0.5, 0.5, 0.5, 0.0);
                ticks++;
            }
        }, 0, 50);
    }
}