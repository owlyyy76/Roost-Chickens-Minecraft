package org.roost.roost.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.roost.roost.Roost;
import org.roost.roost.item.ChickenCatcherItem;
import org.roost.roost.item.ChickenItem;

public class ModItems {

    public static final Item CHICKEN_CATCHER = new ChickenCatcherItem(
            new Item.Settings().maxCount(1).maxDamage(66)
    );

    public static final Item CHICKEN_ITEM = new ChickenItem(
            new Item.Settings().maxCount(16)
    );

    public static final RegistryKey<ItemGroup> ROOST_GROUP = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of("roost", "roost")
    );

    public static void register() {
        Registry.register(Registries.ITEM, Identifier.of(Roost.MOD_ID, "chicken_catcher"), CHICKEN_CATCHER);
        Registry.register(Registries.ITEM, Identifier.of(Roost.MOD_ID, "chicken_item"), CHICKEN_ITEM);

        Registry.register(Registries.ITEM_GROUP, ROOST_GROUP, FabricItemGroup.builder()
                .displayName(Text.translatable("itemgroup.roost.roost"))
                .icon(() -> new ItemStack(CHICKEN_CATCHER))
                .entries((context, entries) -> {
                    entries.add(CHICKEN_CATCHER);
                    entries.add(CHICKEN_ITEM);
                    entries.add(ModBlocks.ROOST);
                    entries.add(ModBlocks.ROOST_COLLECTOR);
                    entries.add(ModBlocks.ROOST_BREEDER);
                })
                .build()
        );
    }
}