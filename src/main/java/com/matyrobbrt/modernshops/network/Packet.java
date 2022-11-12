package com.matyrobbrt.modernshops.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface Packet {
    void handle(NetworkEvent.Context context);
    void encode(FriendlyByteBuf buffer);
}
