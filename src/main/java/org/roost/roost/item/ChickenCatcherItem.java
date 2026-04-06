package org.roost.roost.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ChickenCatcherItem extends Item {

    public ChickenCatcherItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player,
                                    LivingEntity entity, Hand hand) {
        if (entity.getWorld().isClient()) return ActionResult.PASS;
        if (!(entity instanceof ChickenEntity chicken)) return ActionResult.PASS;

        Item chickenItem = Registries.ITEM.get(Identifier.of("roost", "chicken_item"));
        ItemStack chickenStack = new ItemStack(chickenItem);

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("IsBaby", chicken.isBaby());

        if (chicken.hasCustomName() && chicken.getCustomName() != null) {
            nbt.putString("CustomChickenName",
                    Text.Serialization.toJsonString(chicken.getCustomName(),
                            entity.getWorld().getRegistryManager()));
            chickenStack.set(DataComponentTypes.CUSTOM_NAME, chicken.getCustomName());
        }

        chickenStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        // Partículas donde estaba la gallina al capturarla
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.POOF,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }

        chicken.discard();

        stack.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);

        entity.getWorld().spawnEntity(
                new net.minecraft.entity.ItemEntity(
                        entity.getWorld(),
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        chickenStack
                )
        );

        return ActionResult.SUCCESS;
    }
}