package com.matyrobbrt.modernshops.wsd.api;

import com.matyrobbrt.modernshops.wsd.api.network.SyncWSDChangePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class SyncedWSD<SELF extends SyncedWSD<SELF>> extends SavedData {
    public final String id;

    public SyncedWSD(String id) {
        this.id = id;
    }

    public abstract int getType();
    public abstract SyncStrategy<SELF> getStrategy();
    public abstract WSDRegistry getRegistry();

    @SuppressWarnings("unchecked")
    public static <WSD extends SyncedWSD<WSD>> WSD get(ServerLevel level, String id, int type) {
        return (WSD) level.getDataStorage().computeIfAbsent(tag -> WSDRegistry.REGISTRY.decode(id, type, tag), () -> WSDRegistry.REGISTRY.empty(id, type), id);
    }

    @SuppressWarnings("unchecked")
    public abstract class Interaction {
        private boolean syncToClient;
        private final List<Transaction<SELF, ?>> transactions = new ArrayList<>();

        public <CONTEXT> Interaction and(
                CONTEXT context,
                BiConsumer<SELF, CONTEXT> consumer,
                SyncStrategy.Sender<SELF, CONTEXT> sender
        ) {
            this.transactions.add(new Transaction<>(consumer, sender, context));
            return this;
        }

        public <CONTEXT> Interaction and(
                CONTEXT context,
                WSDProcedure<SELF, CONTEXT> changer
        ) {
            return and(context, changer::handle, changer);
        }

        public Interaction syncToClient() {
            this.syncToClient = true;
            return this;
        }

        public void execute() {
            final List<SyncWSDChangePacket> packets = new ArrayList<>();
            this.transactions.forEach(transaction -> {
                final var thisWsd = (SELF) SyncedWSD.this;
                if (syncToClient) packets.add(transaction.packet(thisWsd));
                transaction.execute(thisWsd);
                setDirty();
            });
            packets.forEach(SyncWSDChangePacket::sendToEveryone);
        }

        public record Transaction<WSD extends SyncedWSD<WSD>, CONTEXT>(
                BiConsumer<WSD, CONTEXT> consumer,
                SyncStrategy.Sender<WSD, CONTEXT> sender,
                CONTEXT context
        ){
            public void execute(WSD wsd) {
                consumer.accept(wsd, context);
            }

            public SyncWSDChangePacket packet(WSD wsd) {
                return wsd.getStrategy().packet(wsd.id, wsd.getType(), context, wsd, sender);
            }
        }
    }
}
