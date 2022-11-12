package com.matyrobbrt.modernshops.wsd.api.network;

import com.matyrobbrt.modernshops.network.ModernShopsNetwork;
import com.matyrobbrt.modernshops.network.Packet;
import com.matyrobbrt.modernshops.util.Utils;
import com.matyrobbrt.modernshops.wsd.api.ClientWSDAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncWSDChangePacket(String wsdId, int wsdType, int strategyIndex, FriendlyByteBuf data) implements Packet {
    public static final int SIZE = 50000;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(NetworkEvent.Context context) {
        ClientWSDAccess.accept(wsdId, wsdType, wsd -> wsd.getStrategy().handle(strategyIndex, data, wsd));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(wsdId + ":" + wsdType);
        buffer.writeVarInt(strategyIndex);
        Utils.writeBuffer(buffer, data);
    }

    public static SyncWSDChangePacket decode(FriendlyByteBuf buf) {
        final String[] id = buf.readUtf().split(":");
        return new SyncWSDChangePacket(
                id[0], Integer.parseInt(id[1]), buf.readVarInt(),
                Utils.readBuffer(buf, SIZE)
        );
    }

    public void sendToEveryone() {
        ModernShopsNetwork.sendToEveryone(ModernShopsNetwork.WSD_CHANNEL, this);
    }
}
