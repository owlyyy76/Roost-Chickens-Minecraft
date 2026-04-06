package org.roost.roost.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChickenItem extends Item {

    public ChickenItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.PASS;

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();

        if (player != null && player.isSneaking()) {
            // Shift + click sobre RoostBlock → slot 0
            if (world.getBlockState(pos).getBlock() instanceof org.roost.roost.block.RoostBlock) {
                if (world.getBlockEntity(pos) instanceof org.roost.roost.block.entity.RoostBlockEntity roost) {
                    roost.addChickensFromPlayer(stack);
                    player.setStackInHand(context.getHand(), stack);
                    return ActionResult.SUCCESS;
                }
            }

            // Shift + click sobre RoostBreederBlock → slot 0, si lleno slot 1
            if (world.getBlockState(pos).getBlock() instanceof org.roost.roost.block.RoostBreederBlock) {
                if (world.getBlockEntity(pos) instanceof org.roost.roost.block.entity.RoostBreederBlockEntity breeder) {
                    breeder.addChickensFromPlayer(stack);
                    player.setStackInHand(context.getHand(), stack);
                    return ActionResult.SUCCESS;
                }
            }

            // Shift + click sobre cualquier otro bloque → no spawnear
            return ActionResult.PASS;
        }

        // Click normal → spawnear gallina en la cara clickada
        BlockPos spawnPos = pos.offset(context.getSide());

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = nbtComponent != null ? nbtComponent.copyNbt() : new NbtCompound();

        float yaw = player != null ? player.getYaw() : 0f;

        ChickenEntity chicken = new ChickenEntity(
                net.minecraft.entity.EntityType.CHICKEN, world
        );
        chicken.refreshPositionAndAngles(
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yaw, 0f
        );

        if (nbt.getBoolean("IsBaby")) {
            chicken.setBaby(true);
        }

        if (nbt.contains("CustomChickenName")) {
            try {
                Text name = Text.Serialization.fromJson(
                        nbt.getString("CustomChickenName"),
                        world.getRegistryManager()
                );
                if (name != null) {
                    chicken.setCustomName(name);
                    chicken.setCustomNameVisible(true);
                }
            } catch (Exception e) {
                // Sin nombre si falla la deserialización
            }
        }

        world.spawnEntity(chicken);
        chicken.setYaw(yaw);
        chicken.setHeadYaw(yaw);
        chicken.setBodyYaw(yaw);

        if (player != null && !player.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}