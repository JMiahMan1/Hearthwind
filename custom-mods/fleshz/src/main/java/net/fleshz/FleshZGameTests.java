package net.fleshz;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fleshz.block.entity.WoodRackEntity;
import net.fleshz.init.BlockInit;
import net.fleshz.init.ItemInit;
import net.fleshz.init.RecipeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FleshZGameTests {

    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public FleshZGameTests() {}

    @GameTest
    public void hideRegistered(GameTestHelper helper) {
        helper.assertTrue(ItemInit.HIDE != null, "hide registered");
        helper.assertTrue(ItemInit.ROTTEN_LEATHER != null, "rotten_leather registered");
        helper.assertTrue(ItemInit.PREPARED_HIDE != null, "prepared_hide registered");
        helper.assertTrue(BlockInit.OAK_WOOD_RACK != null, "oak rack registered");
        helper.assertTrue(BlockInit.WOOD_RACK_ENTITY != null, "rack entity type registered");
        helper.succeed();
    }

    @GameTest
    public void rackRecipesLoaded(GameTestHelper helper) {
        helper.assertTrue(!RecipeInit.RACK_ITEM_LIST.isEmpty(), "rack recipes must be loaded");
        helper.assertTrue(RecipeInit.RACK_ITEM_LIST.contains(Items.KELP), "kelp must be a rack input");
        helper.assertTrue(RecipeInit.RACK_RESULT_ITEM_LIST.contains(Items.DRIED_KELP),
                "kelp must dry to dried_kelp");
        int idx = RecipeInit.RACK_ITEM_LIST.indexOf(Items.KELP);
        helper.assertTrue(RecipeInit.RACK_RESULT_TIME_LIST.get(idx) == 2400,
                "kelp drying time must be 2400");
        helper.succeed();
    }

    @GameTest
    public void rackHangAndDry(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockInit.OAK_WOOD_RACK);
        WoodRackEntity be = helper.getBlockEntity(pos, WoodRackEntity.class);
        helper.assertTrue(be != null, "rack block entity must exist");

        be.setItem(0, new ItemStack(Items.KELP));
        helper.assertTrue(!be.isEmpty(), "hang must store kelp");
        helper.assertTrue(be.dryingTime == 2400, "drying time must be 2400, got " + be.dryingTime);
        helper.assertTrue(be.result == Items.DRIED_KELP, "result must be dried_kelp");

        for (int i = 0; i < be.dryingTime + 5; i++) {
            WoodRackEntity.serverTick(helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockState(pos), be);
        }
        ItemStack out = be.getItem(0);
        helper.assertTrue(out.is(Items.DRIED_KELP), "after full dry must be dried_kelp, got " + out);
        helper.succeed();
    }
}
