package dev.silverandro.lootbeams;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public final class LootbeamsGameTests {
    public LootbeamsGameTests() {
    }

    @GameTest
    public void configDefaultsMatchUpstream(GameTestHelper helper) {
        LootbeamsConfig config = new LootbeamsConfig();
        helper.assertTrue(!config.showWhiteItems, "showWhiteItems defaults to false");
        helper.assertTrue(config.particleCount == 1, "particleCount defaults to 1");
        helper.assertTrue(config.beamHeight == 0.8D, "beamHeight defaults to 0.8");
        helper.assertTrue(config.beamOffset == 0.2D, "beamOffset defaults to 0.2");
        helper.assertTrue(config.minimumAge == 12, "minimumAge defaults to 12");
        helper.assertTrue(config.beamDistance == 64.0D, "beamDistance defaults to 64");
        helper.assertTrue(config.enchantedParticles, "enchantedParticles defaults to true");
        helper.assertTrue(!config.useBaseColor, "useBaseColor defaults to false");
        helper.succeed();
    }

    @GameTest
    public void beamColorMatchesItemName(GameTestHelper helper) {
        helper.assertTrue(LootbeamsColors.colorFromStack(new ItemStack(Items.STONE)) == 0xFFFFFF,
                "common drops are white");
        ItemStack named = new ItemStack(Items.STONE);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Relic").withStyle(ChatFormatting.GOLD));
        helper.assertTrue(LootbeamsColors.colorFromStack(named) == 0xFFAA00,
                "custom names use their own colour, got " + Integer.toHexString(LootbeamsColors.colorFromStack(named)));
        ItemStack rare = new ItemStack(Items.NETHERITE_INGOT);
        rare.set(DataComponents.RARITY, Rarity.RARE);
        helper.assertTrue(LootbeamsColors.colorFromStack(rare) == 0x55FFFF,
                "rarity colours pass through, got " + Integer.toHexString(LootbeamsColors.colorFromStack(rare)));
        helper.succeed();
    }

    @GameTest
    public void whiteDropsAreFilteredUnlessConfigured(GameTestHelper helper) {
        ItemEntity item = new ItemEntity(helper.getLevel(), 1.0D, 2.0D, 1.0D, new ItemStack(Items.STONE));
        LootbeamsConfig config = new LootbeamsConfig();
        for (int i = 0; i < config.minimumAge + 1; i++) {
            item.tick();
        }
        helper.assertTrue(LootbeamsColors.isWhite(0xFFFFFF), "white is detected");
        helper.assertTrue(!LootbeamsColors.shouldShow(item, 0xFFFFFF, config),
                "white drops are hidden by default");
        config.showWhiteItems = true;
        helper.assertTrue(LootbeamsColors.shouldShow(item, 0xFFFFFF, config),
                "white drops show when configured");
        helper.assertTrue(LootbeamsColors.shouldShow(item, 0x55FFFF, new LootbeamsConfig()),
                "coloured drops always show");
        helper.succeed();
    }

    @GameTest
    public void ageGateSkipsFreshDrops(GameTestHelper helper) {
        ItemEntity item = new ItemEntity(helper.getLevel(), 1.0D, 2.0D, 1.0D, new ItemStack(Items.DIAMOND));
        LootbeamsConfig config = new LootbeamsConfig();
        helper.assertTrue(!LootbeamsColors.shouldShow(item, 0x55FFFF, config),
                "fresh drops have no beam yet");
        for (int i = 0; i < config.minimumAge; i++) {
            item.tick();
        }
        helper.assertTrue(item.getAge() >= config.minimumAge,
                "age advances with ticks, got " + item.getAge());
        helper.assertTrue(LootbeamsColors.shouldShow(item, 0x55FFFF, config),
                "aged drops pass the gate");
        helper.succeed();
    }
}
