package org.roost.roost.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.roost.roost.registry.ModScreenHandlers;

public class RoostCollectorScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    public RoostCollectorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.ROOST_COLLECTOR_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        checkSize(inventory, 27);

        inventory.onOpen(playerInventory.player);

        // 27 slots del collector (3 filas x 9 columnas), todos input/output
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Inventario del jugador (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public RoostCollectorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();

            // Slots 0-26 son del collector → mover al inventario jugador
            if (slotIndex < 27) {
                if (!this.insertItem(slotStack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Inventario jugador → mover al collector
                if (!this.insertItem(slotStack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }

        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}