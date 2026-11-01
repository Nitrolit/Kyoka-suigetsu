package com.kyoka.addon;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class KyokaSwordItem extends SwordItem {
    private static final IItemTier TIER = new IItemTier() {
        @Override
        public int getUses() { return 2000; }

        @Override
        public float getSpeed() { return 8.0f; }

        @Override
        public float getAttackDamageBonus() { return 0.0f; }

        @Override
        public int getLevel() { return 4; }

        @Override
        public int getEnchantmentValue() { return 15; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    private static final float SEALED_RMB_DAMAGE = 15.0f;
    private static final float RELEASED_RMB_DAMAGE = 60.0f;

    private final boolean released;

    public KyokaSwordItem(Properties properties, boolean released) {
        super(TIER, 9, -2.4f, properties);
        this.released = released;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return ActionResult.success(stack);

        float damage = released ? RELEASED_RMB_DAMAGE : SEALED_RMB_DAMAGE;
        if (KyokaSkills.isCrystal(player)) damage *= 0.5f;

        double width = released ? 7.0 : 3.0;
        double length = released ? 5.0 : 2.0;
        slash((ServerWorld) world, player, damage, width, length);
        return ActionResult.success(stack);
    }

    private static void slash(ServerWorld world, PlayerEntity player, float damage, double width, double length) {
        Vector3d look = player.getLookAngle();
        if (look.lengthSqr() < 1.0E-6) return;
        look = look.normalize();

        Vector3d forward = look;
        Vector3d side = new Vector3d(-forward.z, 0.0D, forward.x).normalize();
        Vector3d up = new Vector3d(0.0D, 1.0D, 0.0D);

        double eyeY = player.getEyeY();
        Vector3d origin = player.position();

        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(length + width + 2.0D, length + width + 2.0D, length + width + 2.0D),
                e -> e != player && e.isAlive());

        for (LivingEntity e : targets) {
            Vector3d rel = e.position().subtract(origin);
            double forwardDist = rel.dot(forward);
            double sideDist = rel.dot(side);
            double verticalDist = rel.dot(up);

            double halfWidth = width / 2.0D + e.getBbWidth() / 2.0D;
            double halfHeight = 1.6D + e.getBbHeight() / 2.0D;

            if (forwardDist > 0.0D
                    && forwardDist <= length + e.getBbWidth() / 2.0D
                    && Math.abs(sideDist) <= halfWidth
                    && Math.abs(verticalDist) <= halfHeight) {
                e.hurt(DamageSource.playerAttack(player), damage);
                e.setDeltaMovement(0.0D, 0.0D, 0.0D);
            }
        }

        int blockY = (int) Math.floor(eyeY);
        for (double f = 0.5D; f < length; f += 1.0D) {
            for (double s = -width / 2.0D + 0.5D; s < width / 2.0D; s += 1.0D) {
                for (double v = -1.0D; v <= 1.5D; v += 1.0D) {
                    BlockPos pos = new BlockPos(
                            (int) Math.floor(origin.x + forward.x * f + side.x * s),
                            (int) Math.floor(blockY + v),
                            (int) Math.floor(origin.z + forward.z * f + side.z * s));
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(world, pos) < 0) continue;
                    world.destroyBlock(pos, true);
                }
            }
        }

        double cx = origin.x + forward.x * (length / 2.0D);
        double cy = eyeY - 0.5D;
        double cz = origin.z + forward.z * (length / 2.0D);
        for (double t = -width / 2.0D; t <= width / 2.0D; t += 1.0D) {
            world.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    cx + side.x * t,
                    cy + (float) Math.sin(t) * 0.15D,
                    cz + side.z * t,
                    1, 0, 0, 0, 0);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
