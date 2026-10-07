package com.github.legoatoom.connectiblechains.migrator.migrations;

import com.github.legoatoom.connectiblechains.entity.Chainable;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;

public final class ChainToNewSystem {
   private static final String OLD_CHAINS_NBT_KEY = "Chains";
   private static final String NEW_CHAINS_NBT_KEY = "%s_%s".formatted("connectiblechains", "Chains");
   private static final String OLD_SOURCE_ITEM_KEY = "SourceItem";
   private static final String NEW_SOURCE_ITEM_KEY = "%s_%s".formatted("connectiblechains", "SourceItem");

   public static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void migrate(
      net.minecraft.world.level.storage.ValueInput readView, net.minecraft.world.level.storage.ValueOutput writeView, E entity
   ) {
      Item knotSourceItem = readView.getString("SourceItem")
         .map(sourceKey -> (Item)BuiltInRegistries.ITEM.getValue(Identifier.tryParse(sourceKey)))
         .orElse(Items.IRON_CHAIN);
      writeView.store(NEW_SOURCE_ITEM_KEY, BuiltInRegistries.ITEM.byNameCodec(), knotSourceItem);
      Optional<net.minecraft.world.level.storage.ValueInput.ValueInputList> optionalList = readView.childrenList("Chains");
      optionalList.ifPresent(readViews -> {
         ValueOutputList linksTag = writeView.childrenList(NEW_CHAINS_NBT_KEY);

         for (ValueInput element : (ValueInputList)optionalList.get()) {
            ValueOutput tag = linksTag.addChild();
            migrateChainData(element, tag, entity);
         }
      });
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void migrateChainData(
      net.minecraft.world.level.storage.ValueInput readView, net.minecraft.world.level.storage.ValueOutput writeView, E entity
   ) {
      Item source = readView.getString("SourceItem")
         .map(sourceKey -> (Item)BuiltInRegistries.ITEM.getValue(Identifier.tryParse(sourceKey)))
         .orElse(Items.IRON_CHAIN);
      writeView.store(NEW_SOURCE_ITEM_KEY, BuiltInRegistries.ITEM.byNameCodec(), source);
      Optional<String> optionalUUID = readView.getString("UUID");
      Optional<Integer> optionalDest = readView.getInt("DestX");
      Optional<Integer> optionalRel = readView.getInt("RelX");
      if (optionalUUID.isPresent()) {
         UUID uuid = UUID.fromString(optionalUUID.get());
         writeView.store("UUID", UUIDUtil.AUTHLIB_CODEC, uuid);
      } else if (optionalDest.isPresent()) {
         Integer destX = optionalDest.get();
         Integer destY = (Integer)readView.getInt("DestY").get();
         Integer destZ = (Integer)readView.getInt("DestZ").get();
         BlockPos desPos = new BlockPos(destX, destY, destZ);
         BlockPos relPos = desPos.subtract(entity.getPos());
         writeView.store("RelativePos", BlockPos.CODEC, relPos);
      } else if (optionalRel.isPresent()) {
         Integer relX = optionalRel.get();
         Integer relY = (Integer)readView.getInt("RelY").get();
         Integer relZ = (Integer)readView.getInt("RelZ").get();
         writeView.store("RelativePos", BlockPos.CODEC, new BlockPos(relX, relY, relZ));
      }
   }
}
