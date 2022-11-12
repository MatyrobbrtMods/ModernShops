package com.matyrobbrt.modernshops.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class Utils {
    public static FriendlyByteBuf readBuffer(FriendlyByteBuf buf, int maxLength) {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(maxLength)));
    }

    public static void writeBuffer(FriendlyByteBuf target, FriendlyByteBuf source) {
        target.writeVarInt(source.readableBytes());
        target.writeBytes(source);
    }
}
