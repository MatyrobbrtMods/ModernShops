package com.matyrobbrt.modernshops.network;

import com.matyrobbrt.modernshops.ModernShops;
import com.matyrobbrt.modernshops.wsd.api.network.SyncWSDChangePacket;
import com.matyrobbrt.modernshops.wsd.api.network.SyncWholeWSDPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Function;

public class ModernShopsNetwork {
    public static final String WSD_VERSION = "1.0";
    public static final SimpleChannel WSD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModernShops.MOD_ID, "wsd"))
            .networkProtocolVersion(() -> WSD_VERSION)
            .clientAcceptedVersions(str -> str.equals(WSD_VERSION))
            .serverAcceptedVersions(str -> str.equals(WSD_VERSION))
            .simpleChannel();

    public static void register() {
        class Registrar {
            private final SimpleChannel channel;
            int id = 0;

            Registrar(SimpleChannel channel) {
                this.channel = channel;
            }

            <P extends Packet> void register(Class<P> pkt, Function<FriendlyByteBuf, P> decoder) {
                channel.messageBuilder(pkt, id++)
                        .consumerMainThread((packet, contextSupplier) -> {
                            final var ctx = contextSupplier.get();
                            packet.handle(ctx);
                        })
                        .encoder(Packet::encode)
                        .decoder(decoder)
                        .add();
            }

            <P extends Packet> void registerNetworkThread(Class<P> pkt, Function<FriendlyByteBuf, P> decoder) {
                channel.messageBuilder(pkt, id++)
                        .consumerNetworkThread((packet, contextSupplier) -> {
                            final var ctx = contextSupplier.get();
                            packet.handle(ctx);
                            ctx.setPacketHandled(true);
                        })
                        .encoder(Packet::encode)
                        .decoder(decoder)
                        .add();
            }
        }

        {
            final var registry = new Registrar(WSD_CHANNEL);
            registry.registerNetworkThread(SyncWholeWSDPacket.class, SyncWholeWSDPacket::decode);
            registry.registerNetworkThread(SyncWSDChangePacket.class, SyncWSDChangePacket::decode);

            registry.register(SyncWholeWSDPacket.Request.class, SyncWholeWSDPacket.Request::decode);
        }
    }

    public static void sendToEveryone(SimpleChannel channel, Packet packet) {
        channel.send(PacketDistributor.ALL.noArg(), packet);
    }
}
