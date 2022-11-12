package com.matyrobbrt.modernshops.wsd.api;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class WSDRegistry {
    public static final WSDRegistry REGISTRY = new WSDRegistry();

    private List<WSDType> types;
    public WSDRegistry() {
        this.types = new ArrayList<>();
    }

    public <WSD extends SyncedWSD<WSD>> int register(BiFunction<String, CompoundTag, WSD> decoder, Function<String, WSD> emptyGetter) {
        final int index = types.size();
        types.add(new WSDType<WSD>() {
            @Override
            public WSD decode(String id, CompoundTag tag) {
                return decoder.apply(id, tag);
            }

            @Override
            public WSD empty(String id) {
                return emptyGetter.apply(id);
            }
        });
        return index;
    }

    public void lock() {
        this.types = List.copyOf(types);
    }

    public SyncedWSD empty(String id, int index) {
        return types.get(index).empty(id);
    }

    public SyncedWSD decode(String id, int index, CompoundTag tag) {
        return types.get(index).decode(id, tag);
    }

    public interface WSDType<WSD extends SyncedWSD<WSD>> {
        WSD decode(String id, CompoundTag tag);
        WSD empty(String id);
    }

}
