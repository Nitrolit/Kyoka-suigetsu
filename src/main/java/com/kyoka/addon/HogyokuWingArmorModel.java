package com.kyoka.addon;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.entity.model.BipedModel;

@OnlyIn(Dist.CLIENT)
public class HogyokuWingArmorModel extends BipedModel<LivingEntity> {
    private final ModelRenderer robeBody;
    private final ModelRenderer wingLeft;
    private final ModelRenderer wingRight;
    private final ModelRenderer chestCore;

    public HogyokuWingArmorModel(float modelSize) {
        super(modelSize, 0.0F, 64, 64);

        robeBody = new ModelRenderer(this, 0, 0);
        robeBody.addBox(-4.5F, 0.0F, -2.0F, 9, 12, 4, modelSize);

        wingLeft = new ModelRenderer(this, 0, 18);
        wingLeft.addBox(-18.0F, -8.0F, -1.0F, 18, 22, 2, modelSize);
        wingLeft.yRot = -1.15F;
        wingLeft.zRot = -0.2F;

        wingRight = new ModelRenderer(this, 0, 18);
        wingRight.addBox(0.0F, -8.0F, -1.0F, 18, 22, 2, modelSize);
        wingRight.yRot = 1.15F;
        wingRight.zRot = 0.2F;

        chestCore = new ModelRenderer(this, 32, 0);
        chestCore.addBox(-2.0F, -4.5F, -2.0F, 4, 4, 2, modelSize);

        body.addChild(robeBody);
        body.addChild(wingLeft);
        body.addChild(wingRight);
        body.addChild(chestCore);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float flutter = (float) Math.sin(ageInTicks * 0.08F);
        wingLeft.yRot = -1.15F + flutter * 0.18F;
        wingRight.yRot = 1.15F - flutter * 0.18F;
        chestCore.zRot = (float) Math.sin(ageInTicks * 0.12F) * 0.22F;

        if (entity instanceof PlayerEntity) {
            chestCore.yRot = 0.0F;
        }
    }
}
