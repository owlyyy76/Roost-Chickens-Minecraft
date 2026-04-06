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

public class RoostBreederScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public RoostBreederScreenHandler(int syncId, PlayerInventory playerInventory,
                                     Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.ROOST_BREEDER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        checkSize(inventory, 6);

        inventory.onOpen(playerInventory.player);

        // Slot 0: Chicken A - solo acepta chicken_item
        this.addSlot(new Slot(inventory, 0, 44, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Registries.ITEM.get(Identifier.of("roost", "chicken_item")));
            }
            @Override
            public int getMaxItemCount() { return 16; }
        });

        // Slot 1: Chicken B - solo acepta chicken_item
        this.addSlot(new Slot(inventory, 1, 62, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Registries.ITEM.get(Identifier.of("roost", "chicken_item")));
            }
            @Override
            public int getMaxItemCount() { return 16; }
        });

        // Slot 2: Seeds
        this.addSlot(new Slot(inventory, 2, 8, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(net.minecraft.item.Items.WHEAT_SEEDS)
                        || stack.isOf(net.minecraft.item.Items.MELON_SEEDS)
                        || stack.isOf(net.minecraft.item.Items.PUMPKIN_SEEDS)
                        || stack.isOf(net.minecraft.item.Items.BEETROOT_SEEDS);
            }
            @Override
            public int getMaxItemCount() { return 64; }
        });

        // Slots 3-5: Output (no se puede insertar)
        this.addSlot(new Slot(inventory, 3, 116, 53) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inventory, 4, 134, 53) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(inventory, 5, 152, 53) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        // Inventario jugador
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

    public RoostBreederScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(6), new PropertyDelegate() {
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

            if (slotIndex < 6) {
                if (!this.insertItem(slotStack, 6, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.insertItem(slotStack, 0, 6, false)) return ItemStack.EMPTY;
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