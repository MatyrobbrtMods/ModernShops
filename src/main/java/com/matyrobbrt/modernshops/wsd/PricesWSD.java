package com.matyrobbrt.modernshops.wsd;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PricesWSD extends SavedData {
    public static final int CURRENT_DATA_VERSION = 1;

    private final String teamId;
    private final Map<ItemData, Price> prices = new HashMap<>();

    public PricesWSD(String teamId) {
        this.teamId = teamId;
    }

    public void setPrice(ItemData data, Price price) {
        prices.put(data, price);
        setDirty();
    }

    @Nullable
    public Price getPrice(ItemData data) {
        return prices.get(data);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putString("teamId", teamId);
        tag.putInt("dataVersion", CURRENT_DATA_VERSION);

        final var pricesCompound = new ListTag();
        prices.forEach((data, price) -> {
            final var compound = new CompoundTag();
            compound.put("item", data.serialize());
            compound.put("price", price.serialize());
            pricesCompound.add(compound);
        });
        tag.put("prices", pricesCompound);

        return tag;
    }

    public static PricesWSD load(CompoundTag tag) {
        final var wsd = new PricesWSD(tag.getString("teamId"));
        {
            final var pricesCompound = tag.getList("prices", Tag.TAG_COMPOUND);
            pricesCompound.forEach(t -> {
                final var compound = (CompoundTag) t;
                wsd.prices.put(
                        ItemData.deserialize(compound.getCompound("item")),
                        Price.deserialize(compound.getCompound("price"))
                );
            });
        }
        return wsd;
    }

    public record ItemData(Item item, @Nullable CompoundTag tag) {
        public static ItemData from(ItemStack stack) {
            return new ItemData(stack.getItem(), stack.getTag());
        }

        public static ItemData deserialize(CompoundTag tag) {
            return new ItemData(
                    Registry.ITEM.get(new ResourceLocation(tag.getString("item"))),
                    (CompoundTag) tag.get("tag")
            );
        }
        public CompoundTag serialize() {
            final var tag = new CompoundTag();
            tag.putString("item", Registry.ITEM.getKey(item).toString());
            if (this.tag() != null) tag.put("tag", this.tag());
            return tag;
        }
    }

    public record Price(ItemStack price, int productAmount) {
        public static Price deserialize(CompoundTag tag) {
            return new Price(
                    ItemStack.of(tag.getCompound("price")),
                    tag.getInt("productAmount")
            );
        }
        public CompoundTag serialize() {
            final var tag = new CompoundTag();
            tag.put("price", price.serializeNBT());
            tag.putInt("productAmount", productAmount);
            return tag;
        }
    }
}
