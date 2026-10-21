package com.kyoka.addon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class KyokaSwordItem extends Item {
    private final boolean released;

    public KyokaSwordItem(Properties properties, boolean released) {
        super(properties);
        this.released = released;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return ActionResult.success(stack);

        float damage = released ? 120.0f : 30.0f;
        double width = released ? 7.0 : 3.0;
        double length = released ? 5.0 : 2.0;
        slash((ServerWorld) world, player, damage, width, length);
        return ActionResult.success(stack);
    }

    private static void slash(ServerWorld world, PlayerEntity player, float damage, double width, double length) {
        Vector3d look = player.getLookAngle();
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSqr() < 1.0E-6) return;
        fwd = fwd.normalize();
        Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);

        double eyeY = player.getEyeY();
        Vector3d origin = player.position();

        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(length + width + 2),
                e -> e != player && e.isAlive());

        for (LivingEntity e : targets) {
            Vector3d d = e.position().subtract(origin);
            double forward = d.x * fwd.x + d.z * fwd.z;
            double sideways = d.x * side.x + d.z * side.z;
            double halfWidth = width / 2 + e.getBbWidth() / 2;
            double centerY = e.getY() + e.getBbHeight() / 2;
            boolean inHeight = Math.abs(centerY - eyeY) <= 0.5 + e.getBbHeight() / 2;

            if (forward > 0 && forward <= length + e.getBbWidth() / 2
                    && Math.abs(sideways) <= halfWidth && inHeight) {
                e.hurt(DamageSource.playerAttack(player), damage);
            }
        }

        // Visual: vanilla sword sweep particles across the slash line.
        double cx = origin.x + fwd.x * (length / 2);
        double cz = origin.z + fwd.z * (length / 2);
        for (double t = -width / 2; t <= width / 2; t += 1.0) {
            world.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    cx + side.x * t, eyeY - 0.5, cz + side.z * t, 1, 0, 0, 0, 0);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
