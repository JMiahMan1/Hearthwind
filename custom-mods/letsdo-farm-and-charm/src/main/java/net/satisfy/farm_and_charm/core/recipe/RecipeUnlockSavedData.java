package net.satisfy.farm_and_charm.core.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

// 26.2: SavedData persistence is Codec-based (SavedDataType + ValueInput/ValueOutput).
// Same data model preserved: per-player set of unlocked recipe ids under DATA_NAME.
public class RecipeUnlockSavedData extends SavedData {
    private final Map<UUID, Set<Identifier>> playerRecipes = new HashMap<>();
    public static final String DATA_NAME = "farm_and_charm_recipe_unlock_data";

    public RecipeUnlockSavedData() {}

    public static RecipeUnlockSavedData fromNbt(CompoundTag tag) {
        RecipeUnlockSavedData data = new RecipeUnlockSavedData();
        CompoundTag playersTag = tag.getCompoundOrEmpty("players");
        for (String key : playersTag.keySet()) {
            ListTag list = playersTag.getListOrEmpty(key);
            Set<Identifier> recipes = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                list.getString(i).ifPresent(s -> recipes.add(Identifier.parse(s)));
            }
            try {
                UUID uuid = UUID.fromString(key);
                data.playerRecipes.put(uuid, recipes);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    public CompoundTag saveToTag(CompoundTag compoundTag) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, Set<Identifier>> entry : playerRecipes.entrySet()) {
            ListTag list = new ListTag();
            for (Identifier recipe : entry.getValue()) {
                list.add(StringTag.valueOf(recipe.toString()));
            }
            playersTag.put(entry.getKey().toString(), list);
        }
        compoundTag.put("players", playersTag);
        return compoundTag;
    }

    public Set<Identifier> getPlayerRecipes(UUID uuid) {
        return playerRecipes.computeIfAbsent(uuid, k -> new HashSet<>());
    }

    public void setPlayerRecipes(UUID uuid, Set<Identifier> recipes) {
        playerRecipes.put(uuid, recipes);
        setDirty();
    }

    public static final Codec<RecipeUnlockSavedData> CODEC = com.mojang.serialization.MapCodec.unit(RecipeUnlockSavedData::new).codec();

    public static final SavedDataType<RecipeUnlockSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("farm_and_charm", DATA_NAME),
            RecipeUnlockSavedData::new, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);
}
