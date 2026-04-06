package org.roost.roost.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;
import org.roost.roost.block.entity.RoostBlockEntity;
import org.roost.roost.registry.ModBlockEntities;

public class RoostBlock extends BlockWithEntity {

    public static final MapCodec<RoostBlock> CODEC = createCodec(RoostBlock::new);
    public static final BooleanProperty HAS_CHICKEN = BooleanProperty.of("has_chicken");
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public RoostBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(HAS_CHICKEN, false)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_CHICKEN, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(HAS_CHICKEN, false);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoostBlockEntity(pos, state);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof RoostBlockEntity roost) {
                ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

                if (player.isSneaking()) {
                    if (!stackInHand.isEmpty() &&
                            stackInHand.isOf(Registries.ITEM.get(Identifier.of("roost", "chicken_item")))) {
                        roost.addChickensFromPlayer(stackInHand);
                        player.setStackInHand(Hand.MAIN_HAND, stackInHand);
                        world.setBlockState(pos, state.with(HAS_CHICKEN, !roost.getStack(0).isEmpty()));
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

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof RoostBlockEntity roost) {
                for (int i = 0; i < roost.size(); i++) {
                    ItemStack stack = roost.getStack(i);
                    if (!stack.isEmpty()) {
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
                roost.clear();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type == ModBlockEntities.ROOST_BLOCK_ENTITY) {
            return (tickerWorld, pos, blockState, blockEntity) -> {
                if (blockEntity instanceof RoostBlockEntity roost) {
                    RoostBlockEntity.tick(tickerWorld, pos, blockState, roost);
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