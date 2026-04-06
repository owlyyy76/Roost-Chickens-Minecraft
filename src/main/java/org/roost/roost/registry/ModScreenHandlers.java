package org.roost.roost.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.roost.roost.screen.RoostBreederScreenHandler;
import org.roost.roost.screen.RoostCollectorScreenHandler;
import org.roost.roost.screen.RoostScreenHandler;

public class ModScreenHandlers {
    public static ScreenHandlerType<RoostScreenHandler> ROOST_SCREEN_HANDLER;
    public static ScreenHandlerType<RoostCollectorScreenHandler> ROOST_COLLECTOR_SCREEN_HANDLER;
    public static ScreenHandlerType<RoostBreederScreenHandler> ROOST_BREEDER_SCREEN_HANDLER;

    public static void register() {
        ROOST_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("roost", "roost"),
                new ScreenHandlerType<>(RoostScreenHandler::new, FeatureSet.empty())
        );

        ROOST_COLLECTOR_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("roost", "roost_collector"),
                new ScreenHandlerType<>(RoostCollectorScreenHandler::new, FeatureSet.empty())
        );

        ROOST_BREEDER_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("roost", "roost_breeder"),
                new ScreenHandlerType<>(RoostBreederScreenHandler::new, FeatureSet.empty())
        );
    }
}