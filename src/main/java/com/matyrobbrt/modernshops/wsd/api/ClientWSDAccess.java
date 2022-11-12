package com.matyrobbrt.modernshops.wsd.api;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.modernshops.wsd.api.network.SyncWholeWSDPacket;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClientWSDAccess {
    private static final Map<String, SyncedWSD> WSD_MAP = new LinkedHashMap<>();
    private static final ListMultimap<String, Consumer<SyncedWSD>> POOLED = Multimaps.synchronizedListMultimap(Multimaps.newListMultimap(new HashMap<>(), ArrayList::new));

    public static <WSD extends SyncedWSD<WSD>> void acceptAndCast(String id, int type, Consumer<WSD> consumer) {
        accept(id, type, it -> consumer.accept((WSD) it));
    }

    public static void accept(String id, int type, Consumer<SyncedWSD> consumer) {
        synchronized (WSD_MAP) {
            final SyncedWSD wsd = WSD_MAP.get(id);
            if (wsd != null) {
                consumer.accept(wsd);
            } else {
                synchronized (POOLED) {
                    POOLED.put(id, consumer);
                }
                new SyncWholeWSDPacket.Request(id, type).send();
            }
        }
    }

    public static <WSD extends SyncedWSD<WSD>> void acceptOrRequest(String id, int type, Consumer<WSD> consumer) {
        synchronized (WSD_MAP) {
            final SyncedWSD wsd = WSD_MAP.get(id);
            if (wsd != null) {
                consumer.accept((WSD)wsd);
            } else {
                new SyncWholeWSDPacket.Request(id, type).send();
            }
        }
    }

    public static void handleWhole(String id, int type, CompoundTag tag) {
        final var wsd = WSDRegistry.REGISTRY.decode(id, type, tag);
        synchronized (WSD_MAP) {
            WSD_MAP.put(id, wsd);
        }
        synchronized (POOLED) {
            POOLED.removeAll(id).forEach(consumer -> consumer.accept(wsd));
        }
    }
}
