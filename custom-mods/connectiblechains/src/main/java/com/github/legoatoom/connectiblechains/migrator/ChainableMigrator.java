package com.github.legoatoom.connectiblechains.migrator;

import com.github.legoatoom.connectiblechains.entity.Chainable;
import com.github.legoatoom.connectiblechains.migrator.migrations.ChainToNewSystem;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;

public class ChainableMigrator<E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> extends DataMigrator<E> {
   @Override
   public void registerMigrations() {
      this.registerMigration(10000, (x$0, x$1, x$2) -> ChainToNewSystem.migrate(x$0, x$1, (E)((BlockAttachedEntity)x$2)));
   }
}
