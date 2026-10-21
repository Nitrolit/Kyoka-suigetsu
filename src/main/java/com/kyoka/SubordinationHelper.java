package com.kyoka.addon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;
import java.util.UUID;

public final class SubordinationHelper {
    public static final double RADIUS = 37.0;

    private SubordinationHelper() {}

    public static SubordinationData data(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(SubordinationData::new, SubordinationData.NAME);
    }

    public static boolean hasKyokaInInventory(PlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            Item item = player.inventory.getItem(i).getItem();
            if (item == ModItems.KYOKA_SEALED.get() || item == ModItems.KYOKA_RELEASED.get()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSubordinate(LivingEntity owner, LivingEntity target) {
        if (!(owner.level instanceof ServerWorld)) return false;
        return data((ServerWorld) owner.level).contains(owner.getUUID(), target.getUUID());
    }

    // Called when the sword is activated. Adds everything in range, keeps the older history.
    public static void onActivate(ServerPlayerEntity owner) {
        ServerWorld world = owner.getLevel();
        SubordinationData data = data(world);
        Set<UUID> set = data.of(owner.getUUID());

        AxisAlignedBB box = owner.getBoundingBox().inflate(RADIUS);
        double maxSq = RADIUS * RADIUS;
        for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive() && e.distanceToSqr(owner) <= maxSq
                        && !(e instanceof PlayerEntity && hasKyokaInInventory((PlayerEntity) e)))) {
            set.add(e.getUUID());
        }
        data.setDirty();
    }
}
