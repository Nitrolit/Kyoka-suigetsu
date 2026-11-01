package com.kyoka.addon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KyokaSwordVisual {
    public enum Stage {
        HOLD,
        BREAK,
        FALL,
        STUCK,
        PULL_OUT
    }

    private static final Map<UUID, KyokaSwordVisual> ACTIVE = new HashMap<>();

    private final PlayerEntity owner;
    private final float[] originalRot = new float[2];
    private boolean capturedRot = false;
    private Stage stage = Stage.HOLD;
    private float timer = 0.0F;
    private float pulse = 0.0F;

    private KyokaSwordVisual(PlayerEntity owner) {
        this.owner = owner;
    }

    public static void start(PlayerEntity player) {
        if (player == null) return;
        ACTIVE.put(player.getUUID(), new KyokaSwordVisual(player));
    }

    public static void tick(PlayerEntity player) {
        if (player == null) return;
        KyokaSwordVisual visual = ACTIVE.get(player.getUUID());
        if (visual == null) return;
        visual.update();
    }

    private void update() {
        if (!capturedRot && owner instanceof ClientPlayerEntity) {
            originalRot[0] = owner.xRot;
            originalRot[1] = owner.yRot;
            capturedRot = true;
        }

        timer += 0.05F;
        pulse += 0.08F;

        switch (stage) {
            case HOLD:
                if (timer >= 2.0F) {
                    stage = Stage.BREAK;
                    timer = 0.0F;
                    spawnBurst(owner, 0.8F, 3.0F);
                }
                break;
            case BREAK:
                if (timer >= 1.0F) {
                    stage = Stage.FALL;
                    timer = 0.0F;
                    spawnBurst(owner, 1.2F, 6.0F);
                }
                break;
            case FALL:
                if (timer >= 3.0F) {
                    stage = Stage.STUCK;
                    timer = 0.0F;
                }
                break;
            case STUCK:
                if (timer >= 3.0F) {
                    stage = Stage.PULL_OUT;
                    timer = 0.0F;
                }
                break;
            case PULL_OUT:
                if (timer >= 2.0F) {
                    resetPose();
                    ACTIVE.remove(owner.getUUID());
                    return;
                }
                break;
            default:
                break;
        }

        applyPose();
    }

    private void applyPose() {
        if (!(owner instanceof ClientPlayerEntity)) return;

        float xRotTarget = originalRot[0];
        float yRotTarget = originalRot[1];
        float headTilt = 0.0F;

        switch (stage) {
            case HOLD:
                headTilt = 14.0F + (float) Math.sin(pulse * 2.0F) * 2.0F;
                xRotTarget += headTilt;
                break;
            case BREAK:
                headTilt = 32.0F;
                xRotTarget += headTilt;
                break;
            case FALL:
                headTilt = 70.0F;
                xRotTarget += headTilt;
                yRotTarget += 12.0F * (float) Math.sin(pulse * 3.0F);
                break;
            case STUCK:
                headTilt = 58.0F;
                xRotTarget += headTilt;
                yRotTarget += 8.0F * (float) Math.sin(pulse * 2.5F);
                break;
            case PULL_OUT:
                headTilt = 22.0F;
                xRotTarget += headTilt;
                yRotTarget += 18.0F;
                break;
            default:
                break;
        }

        owner.xRot = MathHelper.lerp(0.25F, owner.xRot, xRotTarget);
        owner.yRot = MathHelper.lerp(0.2F, owner.yRot, yRotTarget);

        spawnTrailParticles();
    }

    private void resetPose() {
        if (owner instanceof ClientPlayerEntity) {
            owner.xRot = originalRot[0];
            owner.yRot = originalRot[1];
        }
    }

    private void spawnTrailParticles() {
        if (!(owner.level.isClientSide)) return;

        double px = owner.getX() + owner.getLookAngle().x * 0.5D;
        double py = owner.getY() + 1.0D;
        double pz = owner.getZ() + owner.getLookAngle().z * 0.5D;

        if (stage == Stage.BREAK || stage == Stage.FALL) {
            owner.level.addParticle(ParticleTypes.SMOKE,
                    px + (owner.getRandom().nextDouble() - 0.5D) * 0.4D,
                    py + (owner.getRandom().nextDouble() - 0.5D) * 0.6D,
                    pz + (owner.getRandom().nextDouble() - 0.5D) * 0.4D,
                    0.0D, -0.03D, 0.0D);
        }

        if (stage == Stage.FALL || stage == Stage.STUCK) {
            owner.level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    px + (owner.getRandom().nextDouble() - 0.5D) * 0.8D,
                    owner.getY() + 0.2D,
                    pz + (owner.getRandom().nextDouble() - 0.5D) * 0.8D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnBurst(PlayerEntity player, float radius, float strength) {
        if (player == null || !player.level.isClientSide) return;

        Vector3d base = player.position().add(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 18; i++) {
            double angle = (Math.PI * 2.0D * i) / 18.0D;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            player.level.addParticle(ParticleTypes.EXPLOSION,
                    base.x + dx,
                    base.y + 0.25D,
                    base.z + dz,
                    Math.cos(angle) * strength * 0.05D,
                    0.08D,
                    Math.sin(angle) * strength * 0.05D);
        }
    }
}

class KyokaSwordVisualEvents {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        KyokaSwordVisual.tick(mc.player);
    }
}
