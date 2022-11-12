package com.matyrobbrt.modernshops;

import com.matyrobbrt.modernshops.network.ModernShopsNetwork;
import com.matyrobbrt.modernshops.wsd.api.ClientWSDAccess;
import com.matyrobbrt.modernshops.wsd.api.TestWSD;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModernShops.MOD_ID)
public class ModernShops {
    public static final String MOD_ID = "modernshops";

    public ModernShops() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.ITEMS.register(bus);
        Registration.BLOCKS.register(bus);
        Registration.MENU_TYPES.register(bus);
        Registration.BLOCK_ENTITY_TYPES.register(bus);

        bus.addListener((final FMLCommonSetupEvent event) -> ModernShopsNetwork.register());

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        bus.addListener((final RegisterGuiOverlaysEvent event) -> {
            event.registerAboveAll("wsdtest", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
                ClientWSDAccess.<TestWSD>acceptOrRequest("test", TestWSD.TYPE, wsd -> {
                    Screen.drawCenteredString(poseStack, Minecraft.getInstance().font, Component.literal("The test number is: ")
                            .append(Component.literal(Integer.toString(wsd.getTestNumber()))), 200, 200, 0xffff00);
                });
            });
        });
    }

    public void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(MOD_ID)
                .then(Commands.literal("testwsd")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    TestWSD.changeNumber(ctx.getSource().getServer(), IntegerArgumentType.getInteger(ctx, "amount"));
                                    return 1;
                                }))));
    }

}
