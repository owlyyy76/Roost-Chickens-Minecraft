package org.roost.roost.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.roost.roost.Roost;
import org.roost.roost.block.RoostBlock;
import org.roost.roost.block.RoostBreederBlock;
import org.roost.roost.block.RoostCollectorBlock;

public class ModBlocks {
    public static final Block ROOST = new RoostBlock(
            AbstractBlock.Settings.create().strength(2.0f).hardness(4.0f)
    );

    public static final Block ROOST_COLLECTOR = new RoostCollectorBlock(
            AbstractBlock.Settings.create().strength(2.0f).hardness(4.0f)
    );

    public static final Block ROOST_BREEDER = new RoostBreederBlock(
            AbstractBlock.Settings.create().strength(2.0f).hardness(4.0f)
    );

    public static void register() {
        Registry.register(Registries.BLOCK, Identifier.of(Roost.MOD_ID, "roost"), ROOST);
        Registry.register(Registries.BLOCK, Identifier.of(Roost.MOD_ID, "roost_collector"), ROOST_COLLECTOR);
        Registry.register(Registries.BLOCK, Identifier.of(Roost.MOD_ID, "roost_breeder"), ROOST_BREEDER);

        Registry.register(Registries.ITEM, Identifier.of(Roost.MOD_ID, "roost"),
                new BlockItem(ROOST, new Item.Settings()));
        Registry.register(Registries.ITEM, Identifier.of(Roost.MOD_ID, "roost_collector"),
                new BlockItem(ROOST_COLLECTOR, new Item.Settings()));
        Registry.register(Registries.ITEM, Identifier.of(Roost.MOD_ID, "roost_breeder"),
                new BlockItem(ROOST_BREEDER, new Item.Settings()));
    }
}