package org.roost.roost.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.roost.roost.registry.ModBlockEntities;
import org.roost.roost.screen.RoostCollectorScreenHandler;

public class RoostCollectorBlockEntity extends BlockEntity
        implements NamedScreenHandlerFactory, Inventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);

    public RoostCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROOST_COLLECTOR_BLOCK_ENTITY, pos, state);
    }

    // Al colocarse, aspira inmediatamente el output de los Roosts cercanos
    public void drainNearbyRoosts(World world) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) { // mismo nivel y 1 arriba
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos neighborPos = pos.add(dx, dy, dz);
                    if (world.getBlockEntity(neighborPos) instanceof RoostBlockEntity roost) {
                        for (int i = 1; i <= 4; i++) {
                            ItemStack stack = roost.getStack(i);
                            if (!stack.isEmpty()) {
                                ItemStack leftover = insertItem(stack.copy());
                                if (leftover.isEmpty()) {
                                    roost.removeStack(i);
                                } else {
                                    roost.setStack(i, leftover);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ItemStack insertItem(ItemStack incoming) {
        if (incoming.isEmpty()) return ItemStack.EMPTY;

        for (int i = 0; i < items.size(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && slot.isOf(incoming.getItem())) {
                int space = slot.getMaxCount() - slot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, incoming.getCount());
                    slot.increment(toAdd);
                    incoming.decrement(toAdd);
                    markDirty();
                    if (incoming.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, incoming.copy());
                markDirty();
                return ItemStack.EMPTY;
            }
        }

        return incoming;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.roost.roost_collector");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RoostCollectorScreenHandler(syncId, playerInventory, this);
    }

    @Override public int size() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return items.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { markDirty(); return Inventories.splitStack(items, slot, amount); }
    @Override public ItemStack removeStack(int slot) { markDirty(); return Inventories.removeStack(items, slot); }
    @Override public void setStack(int slot, ItemStack stack) { items.set(slot, stack); markDirty(); }
    @Override public boolean canPlayerUse(PlayerEntity player) {
        return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
    @Override public void clear() { items.clear(); }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, items, registryLookup);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, items, registryLookup);
    }
}