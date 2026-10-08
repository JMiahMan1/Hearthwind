package folk.sisby.surveyor.util;

import java.util.Collection;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * 26.2 shim. {@code new ListTag(List<Tag>)} is package-private since 26.2, so
 * external code can only use the no-arg constructor plus add(). This keeps the
 * call sites reading like the collection constructor they used to call.
 */
public final class ListTags {
    private ListTags() {
    }

    public static ListTag of(Collection<? extends Tag> tags) {
        ListTag list = new ListTag();
        for (Tag tag : tags) {
            list.add(tag);
        }
        return list;
    }
}
