package com.kyoka.addon;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CraterEvents {
    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.level.isClientSide) return;
        if (HogyokuHelper.stage(player) < 2) return;
        if (event.getDistance() < 10.0f) return;
        crater(player);
    }

    // Centre 3x3 (+ block under player): 3 layers deep. Next ring 5x5: 2 deep. Outer ring 7x7: 1 deep.
    private static void crater(PlayerEntity player) {
        World world = player.level;
        BlockPos base = player.blockPosition().below();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int ring = Math.max(Math.abs(dx), Math.abs(dz));
                int depth = ring <= 1 ? 3 : (ring == 2 ? 2 : 1);
                for (int dy = 0; dy < depth; dy++) {
                    BlockPos pos = base.offset(dx, -dy, dz);
                    BlockState state = world.getBlockState(pos);
                    if (state.getDestroySpeed(world, pos) < 0) continue; // unbreakable, e.g. bedrock
                    world.destroyBlock(pos, false);
                }
            }
        }
    }
}
