package org.roost.roost.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.roost.roost.client.screen.RoostBreederScreen;
import org.roost.roost.client.screen.RoostCollectorScreen;
import org.roost.roost.client.screen.RoostScreen;
import org.roost.roost.registry.ModScreenHandlers;

public class RoostClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(
                ModScreenHandlers.ROOST_SCREEN_HANDLER,
                RoostScreen::new
        );
        HandledScreens.register(
                ModScreenHandlers.ROOST_COLLECTOR_SCREEN_HANDLER,
                RoostCollectorScreen::new
        );
        HandledScreens.register(
                ModScreenHandlers.ROOST_BREEDER_SCREEN_HANDLER,
                RoostBreederScreen::new
        );
    }
}