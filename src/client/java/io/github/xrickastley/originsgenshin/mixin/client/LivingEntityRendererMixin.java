package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;
import io.github.xrickastley.originsgenshin.element.DurationElementalApplication;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementEntry;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.SphereRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import me.shedaniel.autoconfig.AutoConfig;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
	protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
		super(context);

		throw new AssertionError();
	}

	@Inject(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At("TAIL")
	)
	private void addElementRenderer(final T entity, final float yaw, final float tickDelta, final MatrixStack matrixStack, final VertexConsumerProvider vertexConsumers, final int light, CallbackInfo ci) {
		this.originsgenshin$renderElementsIfPresent(entity, matrixStack, tickDelta);
		this.originsgenshin$renderElementalGauges(entity, matrixStack, tickDelta);
		this.originsgenshin$renderCrystallizeShield(entity, matrixStack);
	}

	@Unique
	private void originsgenshin$renderElementsIfPresent(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final List<ElementEntry> elementArray = new ArrayList<>();

		if (component.hasValidLastReaction()) {
			final ElementalReaction reaction = component.getLastReaction().getLeft();
			final long reactionAt = component.getLastReaction().getRight();

			reaction
				.getReactionDisplayOrder()
				.forEach(element -> elementArray.add(new ElementEntry(element, 60.0, reactionAt, tickDelta)));
		} else {
			if (component.getAppliedElements().isEmpty()) return;

			final Optional<Integer> priority = component.getHighestElementPriority();

			if (priority.isEmpty()) return;

			elementArray.addAll(
				component
					.getAppliedElements()
					.filter(application -> application.getElement().getPriority() == priority.get())
					.map(a -> ElementEntry.of(a, tickDelta))
			);
		}

		final Set<Identifier> textures = new HashSet<>();

		elementArray.removeIf(entry -> !entry.getElement().hasTexture() || !textures.add(entry.getElement().getTexture()));

		final Iterator<Vec3d> coords = this
			.originsgenshin$generateTexturesUsingCenter(new Vec3d(0, 0, 0), 1, elementArray.size())
			.iterator();

		final Set<Identifier> elementTexs = new HashSet<>();

		elementArray.removeIf(entry -> !elementTexs.add(entry.getElement().getTexture()));
		elementArray.forEach(entry -> entry.render(entity, matrixStack, dispatcher.camera, (float) coords.next().getZ()));
	}

	@Unique
	private ArrayList<Vec3d> originsgenshin$generateTexturesUsingCenter(Vec3d center, double length, int amount) {
		double totalDistance = length * (amount - 1);
		double offset = totalDistance / 2;

		final ArrayList<Vec3d> result = new ArrayList<>();
		double curDistance = center.getZ() + offset;
		for (int i = 0; i < amount; i++) {
			result.add(new Vec3d(center.getX(), center.getY(), curDistance));

			curDistance -= length;
		}

		return result;
	}

	@Unique
	private void originsgenshin$renderElementalGauges(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.developer.displayElementalGauges) return;

		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ArrayList<ElementalApplication> appliedElements = new ArrayList<>(component
			.getAppliedElements()
			.sortElements((a, b) -> a.getElement().getPriority() - b.getElement().getPriority()));

		final int elementCount = appliedElements.size();
		final Iterator<ElementalApplication> aeIterator = appliedElements.iterator();

		Stream
			.iterate(0.0f, n -> (n / 1.25f) < elementCount, n -> n + 1.25f)
			.forEachOrdered(yOffset ->
				originsgenshin$renderElementalGauge(entity, aeIterator.next(), yOffset - 0.5f, matrixStack, tickDelta)
			);
	}

	@Unique
	private void originsgenshin$renderElementalGauge(final LivingEntity entity, final ElementalApplication application, final float yOffset, final MatrixStack matrixStack, final float tickDelta) {
		if (application.isEmpty()) return;

		final float GAUGE_SCALE = 0.35f;
		final float SCALE_PER_GU = 2.5f;

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		matrixStack.push();
		matrixStack.translate(0f, entity.getBoundingBox().getLengthY() * 1.15, 0f);
		matrixStack.multiplyPositionMatrix(new Matrix4f().rotation(dispatcher.camera.getRotation()));
		matrixStack.scale(-GAUGE_SCALE, GAUGE_SCALE * 0.5f, GAUGE_SCALE);

		final float xOffset = (float) (entity.getBoundingBox().getLengthX() * 1.5f) / GAUGE_SCALE;
		final float gaugeWidth = application.isGaugeUnits()
			? (float) Math.min(SCALE_PER_GU * application.getGaugeUnits(), SCALE_PER_GU * 4)
			: 2 * SCALE_PER_GU;

		final Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		buffer.vertex(positionMatrix, 0 + xOffset, 0 - yOffset, 0).color(0xffffffff).next();
		buffer.vertex(positionMatrix, gaugeWidth + xOffset, 0 - yOffset, 0).color(0xffffffff).next();
		buffer.vertex(positionMatrix, gaugeWidth + xOffset, 1 - yOffset, 0).color(0xffffffff).next();
		buffer.vertex(positionMatrix, 0 + xOffset, 1 - yOffset, 0).color(0xffffffff).next();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		tessellator.draw();

		final float progress = this.originsgenshin$getProgress(application, tickDelta);
		final Color elementColor = application.getElement().getDamageColor();
		final int color = application.isGaugeUnits()
			? elementColor.asARGB()
			: elementColor.multiply(1, 1, 1, 0.5).asARGB();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		buffer.vertex(positionMatrix, xOffset, 0 - yOffset, -0.0001f).color(color).next();
		buffer.vertex(positionMatrix, (gaugeWidth * progress) + xOffset, 0 - yOffset, -0.0001f).color(color).next();
		buffer.vertex(positionMatrix, (gaugeWidth * progress) + xOffset, 1 - yOffset, -0.0001f).color(color).next();
		buffer.vertex(positionMatrix, xOffset, 1 - yOffset, -0.0001f).color(color).next();

		tessellator.draw();

		if (application.isDuration()) {
			final float gaugeProgress = (float) (application.getCurrentGauge() / application.getGaugeUnits());

			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			buffer.vertex(positionMatrix, xOffset, 0 - yOffset, -0.0001f).color(color).next();
			buffer.vertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 0 - yOffset, -0.0001f).color(color).next();
			buffer.vertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 1 - yOffset, -0.0001f).color(color).next();
			buffer.vertex(positionMatrix, xOffset, 1 - yOffset, -0.0001f).color(color).next();

			tessellator.draw();
		}

		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
		RenderSystem.disableCull();

		final float scaledGauge = (float) (0.1 * gaugeWidth / application.getGaugeUnits());
		final int splits = (int) Math.floor(gaugeWidth / (0.1 * gaugeWidth / application.getGaugeUnits()));

		for (int c = 1; c < splits && config.developer.displayGaugeRuler; c += 1) {
			final float i = c * scaledGauge;

			final float addedY = c % 10 == 0
				? 1f
				: c % 5 == 0
					? 0.5f
					: 0.25f;

			final float lineWidth = c % 10 == 0
				? 5f
				: 2.5f;

			buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
			buffer
				.vertex(positionMatrix, xOffset + i, 0 - yOffset, -0.0005f)
				.color(0xff000000)
				.normal(0, lineWidth, 0)
				.next();
			buffer
				.vertex(positionMatrix, xOffset + i, addedY - yOffset, -0.0005f)
				.color(0xff000000)
				.normal(xOffset + i, lineWidth, 0)
				.next();

			float prev = RenderSystem.getShaderLineWidth();
			RenderSystem.lineWidth(lineWidth);

			tessellator.draw();

			RenderSystem.lineWidth(prev);
		}

		RenderSystem.enableCull();

		matrixStack.pop();
	}

	@Unique
	private float originsgenshin$getProgress(ElementalApplication application, float tickDelta) {
		return application instanceof final DurationElementalApplication durationApp
			? (float) ((application.getRemainingTicks() - tickDelta) / durationApp.getDuration())
			: (float) (application.getCurrentGauge() / application.getGaugeUnits());
	}

	@Unique
	private void originsgenshin$renderCrystallizeShield(final LivingEntity entity, final MatrixStack matrixStack) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final @Nullable Pair<Element, Double> crystallizeShield = component.getCrystallizeShield();

		if (crystallizeShield == null) return;

		final double lengthY = entity.getBoundingBox().getLengthY();

		matrixStack.push();
		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(dispatcher.camera.getYaw()));
		matrixStack.translate(0, lengthY * 0.6, 0);

		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);

		SphereRenderer.render(
			matrixStack,
			new Vec3d(0, 0, 0),
			(float) (lengthY / 2 * 1.25),
			config.renderers.sphereResolution,
			config.renderers.sphereResolution * 2,
			pos -> crystallizeShield.getLeft().getDamageColor().multiply(1, 1, 1, 0.75 * Math.pow(pos.x, 4)).asARGB()
		);

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		matrixStack.pop();
	}


	@Unique
	private FrozenEffectComponent originsgenshin$getComponent(LivingEntity entity) {
		return FrozenEffectComponent.KEY.get(entity);
	}

	@Unique
	@SuppressWarnings("hiding")
	private <T> T originsgenshin$ifFrozen(LivingEntity entity, Function<FrozenEffectComponent, T> ifFrozen, T ifNotFrozen) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		return component.isFrozen()
			? ifFrozen.apply(component)
			: ifNotFrozen;
	}

	@ModifyExpressionValue(
		method = "getRenderLayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;"
		)
	)
	private Identifier renderFrostedModel(Identifier original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, c -> Identifier.of("minecraft", "textures/block/ice.png"), original);
	}

	@ModifyReturnValue(
		method = "isShaking",
		at = @At("RETURN")
	)
	private boolean isShakingWhenFrozen(boolean original, @Local(argsOnly = true) LivingEntity entity) {
		return original || this.originsgenshin$getComponent(entity).isFrozen();
	}

	@Inject(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At("HEAD")
	)
	private void forceFrozenPose(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		if (component.isFrozen()) livingEntity.setPose(component.getForcePose());
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
			ordinal = 0
		)
	)
	private float forceFrozenBodyYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, FrozenEffectComponent::getForceBodyYaw, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
			ordinal = 1
		)
	)
	private float forceFrozenHeadYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, FrozenEffectComponent::getForceHeadYaw, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F",
			ordinal = 0
		)
	)
	private float forceFrozenPitch(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, FrozenEffectComponent::getForcePitch, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getSpeed(F)F"
		)
	)
	private float forceFrozenLimbDistance(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, FrozenEffectComponent::getForceLimbDistance, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getPos(F)F"
		)
	)
	private float forceFrozenLimbAngle(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.originsgenshin$ifFrozen(entity, FrozenEffectComponent::getForceLimbAngle, original);
	}
}
