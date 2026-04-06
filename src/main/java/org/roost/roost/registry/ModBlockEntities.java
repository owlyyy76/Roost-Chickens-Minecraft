package org.roost.roost.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.roost.roost.Roost;
import org.roost.roost.block.entity.RoostBlockEntity;
import org.roost.roost.block.entity.RoostBreederBlockEntity;
import org.roost.roost.block.entity.RoostCollectorBlockEntity;

public class ModBlockEntities {
    public static BlockEntityType<RoostBlockEntity> ROOST_BLOCK_ENTITY;
    public static BlockEntityType<RoostCollectorBlockEntity> ROOST_COLLECTOR_BLOCK_ENTITY;
    public static BlockEntityType<RoostBreederBlockEntity> ROOST_BREEDER_BLOCK_ENTITY;

    public static void register() {
        ROOST_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(Roost.MOD_ID, "roost"),
                FabricBlockEntityTypeBuilder.create(RoostBlockEntity::new, ModBlocks.ROOST).build()
        );

        ROOST_COLLECTOR_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(Roost.MOD_ID, "roost_collector"),
                FabricBlockEntityTypeBuilder.create(RoostCollectorBlockEntity::new, ModBlocks.ROOST_COLLECTOR).build()
        );

        ROOST_BREEDER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(Roost.MOD_ID, "roost_breeder"),
                FabricBlockEntityTypeBuilder.create(RoostBreederBlockEntity::new, ModBlocks.ROOST_BREEDER).build()
        );
    }
}