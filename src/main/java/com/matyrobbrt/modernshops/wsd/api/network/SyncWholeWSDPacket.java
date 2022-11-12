package com.matyrobbrt.modernshops.wsd.api.network;

import com.matyrobbrt.modernshops.network.ModernShopsNetwork;
import com.matyrobbrt.modernshops.network.Packet;
import com.matyrobbrt.modernshops.wsd.api.ClientWSDAccess;
import com.matyrobbrt.modernshops.wsd.api.SyncedWSD;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.filters.VanillaPacketSplitter;

import java.util.ArrayList;
import java.util.List;

// TODO type should probably be sent as short / byte
public record SyncWholeWSDPacket(String wsdId, int wsdType, CompoundTag data) implements Packet {
    @Override
    public void handle(NetworkEvent.Context context) {
        ClientWSDAccess.handleWhole(wsdId, wsdType, data);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(wsdId);
        buffer.writeVarInt(wsdType);
        buffer.writeNbt(data);
    }

    public static SyncWholeWSDPacket decode(FriendlyByteBuf buf) {
        return new SyncWholeWSDPacket(
                buf.readUtf(), buf.readVarInt(),
                buf.readAnySizeNbt()
        );
    }

    public record Request(String wsdId, int wsdType) implements Packet {
        @Override
        @SuppressWarnings("ConstantConditions")
        public void handle(NetworkEvent.Context context) {
            final SavedData savedData = SyncedWSD.get(
                    context.getSender().getServer()
                            .getLevel(Level.OVERWORLD),
                    wsdId, wsdType
            );

            if (savedData != null) {
                final Object message = new SyncWholeWSDPacket(wsdId, wsdType, savedData.save(new CompoundTag()));
                final List<net.minecraft.network.protocol.Packet<?>> packets = new ArrayList<>();

                VanillaPacketSplitter.appendPackets(
                        null, PacketFlow.CLIENTBOUND,
                        ModernShopsNetwork.WSD_CHANNEL.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT),
                        packets
                );

                final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(context::getSender);
                packets.forEach(target::send);
            }
        }

        @Override
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUtf(wsdId);
            buffer.writeVarInt(wsdType);
        }

        public static Request decode(FriendlyByteBuf buf) {
            return new Request(buf.readUtf(), buf.readVarInt());
        }

        public void send() {
            ModernShopsNetwork.WSD_CHANNEL.sendToServer(this);
        }
    }
}
