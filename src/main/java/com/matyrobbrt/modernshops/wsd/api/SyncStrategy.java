package com.matyrobbrt.modernshops.wsd.api;

import com.matyrobbrt.modernshops.wsd.api.network.SyncWSDChangePacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SyncStrategy<WSD extends SyncedWSD<WSD>> {
    private final List<BiConsumer<FriendlyByteBuf, WSD>> handlers = new ArrayList<>();
    private final List<Sender<WSD, ?>> senders = new ArrayList<>();

    public <CONTEXT> Sender<WSD, CONTEXT> register(Sender<WSD, CONTEXT> sender, BiConsumer<FriendlyByteBuf, WSD> handler) {
        handlers.add(handler);
        senders.add(sender);
        return sender;
    }

    public <CONTEXT> WSDProcedure<WSD, CONTEXT> register(Sender<WSD, CONTEXT> sender, BiFunction<FriendlyByteBuf, WSD, CONTEXT> decoder, BiConsumer<WSD, CONTEXT> handler) {
        final WSDProcedure<WSD, CONTEXT> changer = new WSDProcedure<>() {
            @Override
            public void handle(WSD wsd, CONTEXT context) {
                handler.accept(wsd, context);
            }

            @Override
            public void send(FriendlyByteBuf buffer, CONTEXT context, WSD wsd) {
                sender.send(buffer, context, wsd);
            }

            @Override
            public void accept(FriendlyByteBuf buf, WSD wsd) {
                handle(wsd, decoder.apply(buf, wsd));
            }
        };
        handlers.add(changer);
        senders.add(changer);
        return changer;
    }

    public <CONTEXT> SyncWSDChangePacket packet(String wsdId, int wsdType, CONTEXT context, WSD wsd, Sender<WSD, CONTEXT> sender) {
        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        sender.send(data, context, wsd);
        return new SyncWSDChangePacket(wsdId, wsdType, senders.indexOf(sender), data);
    }

    public void handle(int index, FriendlyByteBuf buf, WSD wsd) {
        handlers.get(index).accept(buf, wsd);
    }

    public interface Sender<WSD, C> {
        void send(FriendlyByteBuf buffer, C context, WSD wsd);
    }
}
