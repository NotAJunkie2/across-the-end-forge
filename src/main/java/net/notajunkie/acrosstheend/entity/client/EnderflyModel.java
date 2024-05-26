package net.notajunkie.acrosstheend.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.notajunkie.acrosstheend.entity.ModEntities;
import net.notajunkie.acrosstheend.entity.animations.ModAnimationDefinitions;
import net.notajunkie.acrosstheend.entity.custom.EnderflyEntity;

public class EnderflyModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	private final ModelPart enderfly;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart right_wing;
	private final ModelPart left_wing;

	public EnderflyModel(ModelPart root) {
		this.enderfly = root.getChild("enderfly");
		this.head = enderfly.getChild("head");
		this.body = enderfly.getChild("body");
		this.right_wing = enderfly.getChild("right_wing");
		this.left_wing = enderfly.getChild("left_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition enderfly = partdefinition.addOrReplaceChild("enderfly", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = enderfly.addOrReplaceChild("head", CubeListBuilder.create().texOffs(11, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition body = enderfly.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition right_wing = enderfly.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(-0.5F, -0.5F, 0.0F));

		PartDefinition right_wing_r1 = right_wing.addOrReplaceChild("right_wing_r1", CubeListBuilder.create().texOffs(0, 6).addBox(-3.0F, -1.0F, -2.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-0.55F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.4363F));

		PartDefinition left_wing = enderfly.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(0.5F, -0.5F, 0.0F));

		PartDefinition left_wing_r1 = left_wing.addOrReplaceChild("left_wing_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, -2.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.55F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.4363F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);

		this.animateWalk(ModAnimationDefinitions.ENDERFLY_FLY, limbSwing, 0.25f, 0.25f, 1f);
		this.animate(((EnderflyEntity) entity).flyAnimationState, ModAnimationDefinitions.ENDERFLY_FLY, ageInTicks, 0.25f);
	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0f, 30.0f);
		pHeadPitch = Mth.clamp(pHeadPitch, -25.0f, 45.0f);

		this.enderfly.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.enderfly.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		enderfly.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.enderfly;
	}


}