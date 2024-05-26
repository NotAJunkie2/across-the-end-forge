package net.notajunkie.acrosstheend.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.custom.EnderflyEntity;

public class EnderflyRenderer extends MobRenderer<EnderflyEntity, EnderflyModel<EnderflyEntity>> {
    public EnderflyRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new EnderflyModel<>(pContext.bakeLayer(ModModelLayers.ENDERFLY)), 0.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderflyEntity pEntity) {
        return new ResourceLocation(AcrossTheEnd.MOD_ID, "textures/entity/enderfly.png");
    }

    @Override
    public void render(EnderflyEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.85F, 0.85F, 0.85F);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
