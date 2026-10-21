package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HogyokuItem extends Item {
    public HogyokuItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return ActionResult.success(stack);
        if (HogyokuHelper.stage(player) > 0) return ActionResult.fail(stack);

        BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
        if (v == null || v.reiatsu_Bar < HogyokuHelper.USE_COST) return ActionResult.fail(stack);

        ReiatsuHelper.spend(player, HogyokuHelper.USE_COST);
        HogyokuHelper.setStage(player, 1);

        // Reiatsu limit +30%.
        v = ReiatsuHelper.vars(player);
        v.reiatsu_Max *= 1.3;
        v.reiatsu_CurrentMax *= 1.3;
        ReiatsuHelper.spend(player, 0); // syncs the new values to the client

        stack.shrink(1);
        return ActionResult.consume(stack);
    }
}
