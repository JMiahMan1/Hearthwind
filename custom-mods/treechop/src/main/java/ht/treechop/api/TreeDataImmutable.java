package ht.treechop.api;

import ht.treechop.common.chop.Chop;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Deprecated
public interface TreeDataImmutable {
   Optional<Set<net.minecraft.core.BlockPos>> getLogBlocks();

   @Deprecated
   Set<net.minecraft.core.BlockPos> getLogBlocksOrEmpty();

   int getChops();

   Stream<net.minecraft.core.BlockPos> streamLogs();

   Stream<net.minecraft.core.BlockPos> streamLeaves();

   boolean hasLeaves();

   boolean isAProperTree(boolean var1);

   boolean readyToFell(int var1);

   int numChopsNeededToFell();

   Collection<Chop> chop(net.minecraft.core.BlockPos var1, int var2);

   void forEachLeaves(Consumer<net.minecraft.core.BlockPos> var1);
}
