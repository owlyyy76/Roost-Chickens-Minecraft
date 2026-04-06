package org.roost.roost;

import net.fabricmc.api.ModInitializer;
import org.roost.roost.registry.ModBlocks;
import org.roost.roost.registry.ModBlockEntities;
import org.roost.roost.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roost.roost.registry.ModScreenHandlers;

public class Roost implements ModInitializer {
    public static final String MOD_ID = "roost";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register(); // <-- añadir
        LOGGER.info("Roost initialized!");
    }


}
