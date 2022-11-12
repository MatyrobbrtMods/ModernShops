package com.matyrobbrt.modernshops.wsd.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TestWSD extends SyncedWSD<TestWSD> {
    public static final int TYPE = WSDRegistry.REGISTRY.register(
            TestWSD::decode, TestWSD::new
    );

    private static final SyncStrategy<TestWSD> STRATEGY = new SyncStrategy<>();
    private static final WSDProcedure<TestWSD, Integer> testNumberChange = STRATEGY.register(
            (buf, context, wsd) -> buf.writeInt(context),
            (buf, wsd) -> buf.readInt(),
            TestWSD::setTestNumber
    );

    public TestWSD(String id) {
        super(id);
    }

    private int testNumber;
    private void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }
    public int getTestNumber() {
        return this.testNumber;
    }

    public static TestWSD decode(String id, CompoundTag tag) {
        final var wsd = new TestWSD(id);
        wsd.testNumber = tag.getInt("testNumber");
        return wsd;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public SyncStrategy<TestWSD> getStrategy() {
        return STRATEGY;
    }

    @Override
    public WSDRegistry getRegistry() {
        return WSDRegistry.REGISTRY;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        pCompoundTag.putInt("testNumber", testNumber);
        return pCompoundTag;
    }

    public Interaction interact() {
        return new Interaction() {};
    }

    public static void changeNumber(MinecraftServer server, int newTestNumber) {
        final TestWSD wsd = get(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)), "test", TYPE);
        wsd.interact()
                .and(newTestNumber, testNumberChange)
                .syncToClient()
                .execute();
    }
}
