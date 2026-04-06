package org.roost.roost.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.roost.roost.block.RoostBlock;
import org.roost.roost.registry.ModBlockEntities;
import org.roost.roost.screen.RoostScreenHandler;

import java.util.Random;

public class RoostBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int tickCounter = 0;
    private static final int TICKS_TO_PRODUCE = 600;
    private final Random random = new Random();

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> tickCounter;
                case 1 -> TICKS_TO_PRODUCE;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) tickCounter = value;
        }
        @Override public int size() { return 2; }
    };

    public RoostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROOST_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RoostBlockEntity be) {
        if (world.isClient()) return;

        int chickenCount = be.getChickenCount();
        if (chickenCount == 0) {
            if (be.tickCounter != 0) {
                be.tickCounter = 0;
                be.markDirty();
            }
            return;
        }

        be.tickCounter++;

        if (be.tickCounter >= TICKS_TO_PRODUCE) {
            be.tickCounter = 0;
            for (int i = 0; i < chickenCount; i++) {
                be.tryProduceDrop();
            }
            be.markDirty();
        }
    }

    private int getChickenCount() {
        ItemStack chickenSlot = items.get(0);
        return chickenSlot.isEmpty() ? 0 : chickenSlot.getCount();
    }

    private void tryProduceDrop() {
        Item drop = random.nextFloat() < 0.8f ? Items.EGG : Items.FEATHER;
        ItemStack dropStack = new ItemStack(drop, 1);

        if (world != null) {
            dropStack = notifyCollectors(dropStack);
        }

        if (!dropStack.isEmpty()) {
            for (int i = 1; i <= 4; i++) {
                ItemStack slot = items.get(i);
                if (slot.isEmpty()) {
                    items.set(i, dropStack.copy());
                    return;
                } else if (slot.isOf(drop) && slot.getCount() < slot.getMaxCount()) {
                    slot.increment(dropStack.getCount());
                    return;
                }
            }
        }
    }

    private ItemStack notifyCollectors(ItemStack stack) {
        if (world == null) return stack;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos neighborPos = pos.add(dx, dy, dz);
                    if (world.getBlockEntity(neighborPos) instanceof RoostCollectorBlockEntity collector) {
                        stack = collector.insertItem(stack);
                        if (stack.isEmpty()) return ItemStack.EMPTY;
                    }
                }
            }
        }
        return stack;
    }

    public void addChickensFromPlayer(ItemStack stackInHand) {
        if (stackInHand.isEmpty()) return;

        ItemStack slotStack = items.get(0);

        if (slotStack.isEmpty()) {
            int toInsert = Math.min(stackInHand.getCount(), 16);
            items.set(0, stackInHand.split(toInsert));
        } else if (slotStack.isOf(stackInHand.getItem())) {
            int toAdd = Math.min(stackInHand.getCount(), 16 - slotStack.getCount());
            slotStack.increment(toAdd);
            stackInHand.decrement(toAdd);
        }

        // Actualizar blockstate desde shift+click
        if (world != null && !world.isClient()) {
            world.setBlockState(pos, world.getBlockState(pos)
                    .with(RoostBlock.HAS_CHICKEN, !items.get(0).isEmpty()));
        }

        markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.roost.roost");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RoostScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override public int size() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return items.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { markDirty(); return Inventories.splitStack(items, slot, amount); }
    @Override public ItemStack removeStack(int slot) { markDirty(); return Inventories.removeStack(items, slot); }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (slot == 0 && world != null && !world.isClient()) {
            world.setBlockState(pos, world.getBlockState(pos)
                    .with(RoostBlock.HAS_CHICKEN, !stack.isEmpty()));
        }
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
        tickCounter = nbt.getInt("TickCounter");
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putInt("TickCounter", tickCounter);
    }
}