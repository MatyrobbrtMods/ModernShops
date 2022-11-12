package com.matyrobbrt.modernshops.wsd.api;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;

public interface WSDProcedure<WSD extends SyncedWSD<WSD>, CONTEXT> extends BiConsumer<FriendlyByteBuf, WSD>, SyncStrategy.Sender<WSD, CONTEXT> {
    void handle(WSD wsd, CONTEXT context);
}
