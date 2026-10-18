package ht.treechop.common.platform;

import ht.treechop.api.ChopData;
import ht.treechop.api.ChopDataImmutable;
import ht.treechop.api.FellData;
import ht.treechop.api.TreeData;
import ht.treechop.common.chop.ChopResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface Platform {
   boolean isDedicatedServer();

   boolean uses(ModLoader var1);

   TreeData detectTreeEvent(Level var1, ServerPlayer var2, BlockPos var3, BlockState var4, TreeData var5);

   boolean startChopEvent(ServerPlayer var1, ServerLevel var2, BlockPos var3, BlockState var4, ChopData var5, Object var6);

   void finishChopEvent(ServerPlayer var1, ServerLevel var2, BlockPos var3, BlockState var4, ChopDataImmutable var5, ChopResult var6);

   Block getChoppedLogBlock();

   net.minecraft.world.level.block.entity.BlockEntityType<?> getChoppedLogBlockEntity();

   Identifier getResourceLocationForBlock(Block var1);

   Identifier getResourceLocationForItem(Item var1);

   BlockState getStrippedState(BlockGetter var1, BlockPos var2, BlockState var3);

   boolean startFellTreeEvent(ServerPlayer var1, Level var2, BlockPos var3, FellData var4);

   void finishFellTreeEvent(ServerPlayer var1, Level var2, BlockPos var3, FellData var4);
}
