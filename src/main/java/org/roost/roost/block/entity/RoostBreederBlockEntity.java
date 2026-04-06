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
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.roost.roost.block.RoostBreederBlock;
import org.roost.roost.registry.ModBlockEntities;
import org.roost.roost.screen.RoostBreederScreenHandler;

public class RoostBreederBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 600;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }
        @Override public int size() { return 2; }
    };

    public RoostBreederBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROOST_BREEDER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RoostBreederBlockEntity be) {
        if (world.isClient()) return;

        if (!be.canWork()) {
            if (be.progress != 0) {
                be.progress = 0;
                be.markDirty();
            }
            be.updateBlockState();
            return;
        }

        int chickenA = be.items.get(0).getCount();
        int chickenB = be.items.get(1).getCount();
        int speed = Math.max(1, Math.min(chickenA, chickenB) / 2);

        be.progress += speed;

        if (be.progress >= be.maxProgress) {
            be.progress = 0;
            be.produce();
            be.markDirty();
        } else {
            be.markDirty();
        }
    }

    private boolean canWork() {
        ItemStack chickenA = items.get(0);
        ItemStack chickenB = items.get(1);
        ItemStack seeds = items.get(2);

        if (chickenA.isEmpty() || chickenB.isEmpty()) return false;
        if (seeds.isEmpty() || seeds.getCount() < 2) return false;
        if (!chickenA.isOf(chickenB.getItem())) return false;

        for (int i = 3; i <= 5; i++) {
            ItemStack output = items.get(i);
            if (output.isEmpty()) return true;
            if (output.isOf(chickenA.getItem()) && output.getCount() < output.getMaxCount()) return true;
        }
        return false;
    }

    private void produce() {
        ItemStack chickenA = items.get(0);
        ItemStack seeds = items.get(2);

        seeds.decrement(2);

        for (int i = 3; i <= 5; i++) {
            ItemStack output = items.get(i);
            if (output.isEmpty()) {
                items.set(i, new ItemStack(chickenA.getItem(), 1));
                updateBlockState();
                return;
            } else if (output.isOf(chickenA.getItem()) && output.getCount() < output.getMaxCount()) {
                output.increment(1);
                updateBlockState();
                return;
            }
        }
    }

    // Shift + click con seeds → slot 2
    public void addSeedsFromPlayer(ItemStack stackInHand) {
        if (stackInHand.isEmpty()) return;

        ItemStack seedSlot = items.get(2);

        if (seedSlot.isEmpty()) {
            int toInsert = Math.min(stackInHand.getCount(), 64);
            items.set(2, stackInHand.split(toInsert));
        } else if (seedSlot.isOf(stackInHand.getItem())) {
            int toAdd = Math.min(stackInHand.getCount(), 64 - seedSlot.getCount());
            seedSlot.increment(toAdd);
            stackInHand.decrement(toAdd);
        }

        updateBlockState();
        markDirty();
    }

    // Shift + click con chicken_item → slot 0, si lleno slot 1
    public void addChickensFromPlayer(ItemStack stackInHand) {
        if (stackInHand.isEmpty()) return;

        // Intentar slot 0 primero
        ItemStack slot0 = items.get(0);
        if (slot0.isEmpty()) {
            int toInsert = Math.min(stackInHand.getCount(), 16);
            items.set(0, stackInHand.split(toInsert));
            updateBlockState();
            markDirty();
            return;
        } else if (slot0.isOf(stackInHand.getItem()) && slot0.getCount() < 16) {
            int toAdd = Math.min(stackInHand.getCount(), 16 - slot0.getCount());
            slot0.increment(toAdd);
            stackInHand.decrement(toAdd);
            updateBlockState();
            markDirty();
            return;
        }

        // Si slot 0 lleno → intentar slot 1
        ItemStack slot1 = items.get(1);
        if (slot1.isEmpty()) {
            int toInsert = Math.min(stackInHand.getCount(), 16);
            items.set(1, stackInHand.split(toInsert));
        } else if (slot1.isOf(stackInHand.getItem()) && slot1.getCount() < 16) {
            int toAdd = Math.min(stackInHand.getCount(), 16 - slot1.getCount());
            slot1.increment(toAdd);
            stackInHand.decrement(toAdd);
        }

        updateBlockState();
        markDirty();
    }

    private void updateBlockState() {
        if (world == null || world.isClient()) return;
        boolean hasChickens = !items.get(0).isEmpty() || !items.get(1).isEmpty();
        boolean hasSeeds = !items.get(2).isEmpty() && items.get(2).getCount() >= 2;
        world.setBlockState(pos, world.getBlockState(pos)
                .with(RoostBreederBlock.HAS_CHICKENS, hasChickens)
                .with(RoostBreederBlock.HAS_SEEDS, hasSeeds));
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.roost.roost_breeder");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RoostBreederScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override public int size() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return items.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { markDirty(); return Inventories.splitStack(items, slot, amount); }
    @Override public ItemStack removeStack(int slot) { markDirty(); return Inventories.removeStack(items, slot); }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        updateBlockState();
        markDirty();
    }

    @Override public boolean canPlayerUse(PlayerEntity player) {
        return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
    @Override public void clear() { items.clear(); }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, items, registryLookup);
        progress = nbt.getInt("Progress");
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putInt("Progress", progress);
    }
}