package com.github.legoatoom.connectiblechains.migrator;

public interface Migration<Context> {
   void migrate(net.minecraft.world.level.storage.ValueInput var1, net.minecraft.world.level.storage.ValueOutput var2, Context var3);
}
