package dev.jmiahman.hearthwind.client.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * Every item and entity in the live registry must show a real English name,
 * never a raw translation key like "item.bakery.bread".
 */
public class ItemNameGameTests implements FabricClientGameTest {
    /** A missing translation renders as its key: lowercase words joined by dots. */
    private static final Pattern RAW_KEY = Pattern.compile("^[a-z0-9_]+(\\.[a-z0-9_/]+){2,}$");

    @Override
    public void runTest(ClientGameTestContext context) {
        List<String> offenders = context.computeOnClient(minecraft -> {
            List<String> bad = new ArrayList<>();
            BuiltInRegistries.ITEM.forEach(item -> {
                String name = new ItemStack(item).getHoverName().getString();
                if (RAW_KEY.matcher(name).matches()) {
                    String id = BuiltInRegistries.ITEM.getKey(item).toString();
                    bad.add(id + " -> " + name);
                }
            });
            BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
                String name = type.getDescription().getString();
                if (RAW_KEY.matcher(name).matches()) {
                    String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                    bad.add(id + " -> " + name);
                }
            });
            return bad;
        });

        context.takeScreenshot("item_names_audit");

        if (!offenders.isEmpty()) {
            throw new AssertionError(offenders.size() + " items/entities render a raw translation key:\n  "
                    + String.join("\n  ", offenders));
        }
    }
}
