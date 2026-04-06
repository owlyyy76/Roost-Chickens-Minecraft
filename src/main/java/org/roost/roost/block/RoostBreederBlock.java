package org.roost.roost.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.roost.roost.block.entity.RoostBreederBlockEntity;
import org.roost.roost.registry.ModBlockEntities;

public class RoostBreederBlock extends BlockWithEntity {

    public static final MapCodec<RoostBreederBlock> CODEC = createCodec(RoostBreederBlock::new);
    public static final BooleanProperty HAS_CHICKENS = BooleanProperty.of("has_chickens");
    public static final BooleanProperty HAS_SEEDS = BooleanProperty.of("has_seeds");

    public RoostBreederBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(HAS_CHICKENS, false)
                .with(HAS_SEEDS, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_CHICKENS, HAS_SEEDS);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoostBreederBlockEntity(pos, state);
    }

    // Se llama ANTES que el item → intercepta semillas
    @Override
    public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world,
                                          BlockPos pos, PlayerEntity player, Hand hand,
                                          BlockHitResult hit) {
        if (player.isSneaking() && isSeed(stack)) {
            if (!world.isClient()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof RoostBreederBlockEntity breeder) {
                    breeder.addSeedsFromPlayer(stack);
                    player.setStackInHand(hand, stack);
                }
            }
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    // Click derecho normal → abrir GUI
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof RoostBreederBlockEntity breeder) {
                ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

                if (player.isSneaking()) {
                    if (!stackInHand.isEmpty()) {
                        if (stackInHand.isOf(Registries.ITEM.get(Identifier.of("roost", "chicken_item")))) {
                            breeder.addChickensFromPlayer(stackInHand);
                            player.setStackInHand(Hand.MAIN_HAND, stackInHand);
                        }
                    }
                    return ActionResult.SUCCESS;
                }

                NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
                if (factory != null) {
                    player.openHandledScreen(factory);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    // Dropear inventario al romper
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof RoostBreederBlockEntity breeder) {
                for (int i = 0; i < breeder.size(); i++) {
                    ItemStack stack = breeder.getStack(i);
                    if (!stack.isEmpty()) {
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
                breeder.clear();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private boolean isSeed(ItemStack stack) {
        return stack.isOf(Items.WHEAT_SEEDS)
                || stack.isOf(Items.MELON_SEEDS)
                || stack.isOf(Items.PUMPKIN_SEEDS)
                || stack.isOf(Items.BEETROOT_SEEDS);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type == ModBlockEntities.ROOST_BREEDER_BLOCK_ENTITY) {
            return (tickerWorld, pos, blockState, blockEntity) -> {
                if (blockEntity instanceof RoostBreederBlockEntity breeder) {
                    RoostBreederBlockEntity.tick(tickerWorld, pos, blockState, breeder);
                }
            };
        }
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}