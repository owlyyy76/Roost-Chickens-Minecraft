package org.roost.roost.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.roost.roost.registry.ModScreenHandlers;

public class RoostScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public RoostScreenHandler(int syncId, PlayerInventory playerInventory,
                              Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.ROOST_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        checkSize(inventory, 5);

        inventory.onOpen(playerInventory.player);

        // Slot de gallinas - solo acepta chicken_item
        this.addSlot(new Slot(inventory, 0, 26, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Registries.ITEM.get(Identifier.of("roost", "chicken_item")));
            }
            @Override
            public int getMaxItemCount() { return 16; }
        });

        // 4 slots de output - no se puede insertar manualmente
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(inventory, i + 1, 80 + i * 18, 53) {
                @Override
                public boolean canInsert(ItemStack stack) { return false; }
            });
        }

        // Inventario jugador (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addProperties(propertyDelegate);
    }

    // Constructor cliente
    public RoostScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5), new PropertyDelegate() {
            private final int[] data = new int[2];
            @Override public int get(int index) { return data[index]; }
            @Override public void set(int index, int value) { data[index] = value; }
            @Override public int size() { return 2; }
        });
    }

    public int getProgress() { return propertyDelegate.get(0); }
    public int getMaxProgress() { return propertyDelegate.get(1); }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();

            if (slotIndex < 5) {
                // Slots del roost → mover al inventario jugador
                if (!this.insertItem(slotStack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // Inventario jugador → solo al slot de gallinas
                if (!this.insertItem(slotStack, 0, 1, false)) return ItemStack.EMPTY;
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