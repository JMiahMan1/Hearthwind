package ht.treechop.api;

import java.util.Set;

public interface TreeData extends TreeDataImmutable {
   @Deprecated
   void setLogBlocks(Set<net.minecraft.core.BlockPos> var1);

   @Deprecated
   void setLeaves(boolean var1);
}
