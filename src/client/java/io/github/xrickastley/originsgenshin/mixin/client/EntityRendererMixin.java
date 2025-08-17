package io.github.xrickastley.originsgenshin.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.DurationElementalApplication;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementEntry;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.SphereRenderer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import me.shedaniel.autoconfig.AutoConfig;

@Debug(export = true)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	@Shadow
	@Final
	protected EntityRenderDispatcher dispatcher;

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Inject(
		method = "render",
		at = @At("HEAD")
	)
	private void addElementRenderer(final Entity entity, final float yaw, final float tickDelta, final MatrixStack matrixStack, final VertexConsumerProvider vertexConsumers, final int light, CallbackInfo ci) {
		if (!(entity instanceof final LivingEntity livingEntity)) return;

		this.renderElementsIfPresent(livingEntity, matrixStack, tickDelta);
		this.renderElementalGauges(livingEntity, matrixStack, tickDelta);
		this.renderCrystallizeShield(livingEntity, matrixStack);
	}

	@Unique
	private void renderElementsIfPresent(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final List<ElementEntry> elementArray = new ArrayList<>();

		if (component.hasValidLastReaction()) {
			final ElementalReaction reaction = component.getLastReaction().getLeft();
			final long reactionAt = component.getLastReaction().getRight();

			elementArray.add(new ElementEntry(reaction.getAuraElement(), 60.0, reactionAt, tickDelta));
			elementArray.add(new ElementEntry(reaction.getTriggeringElement(), 60.0, reactionAt, tickDelta));
		} else {
			if (component.getAppliedElements().length() == 0) return;

			final Optional<Integer> priority = component.getHighestElementPriority();

			if (!priority.isPresent()) return;

			elementArray.addAll(
				component
					.getAppliedElements()
					.filter(application -> application.getElement().getPriority() == priority.get())
					.map(a -> ElementEntry.of(a, tickDelta))
			);
		}

		final Iterator<Vec3d> coords = this
			.generateTexturesUsingCenter(new Vec3d(0, 0, 0), 1, elementArray.size())
			.iterator();

		final Set<Identifier> elementTexs = new HashSet<>();

		elementArray.removeIf(entry -> !elementTexs.add(entry.getElement().getTexture()));
		elementArray.forEach(entry -> entry.render(entity, matrixStack, dispatcher.camera, (float) coords.next().getZ()));
	}

	@Unique
	private ArrayList<Vec3d> generateTexturesUsingCenter(Vec3d center, double length, int amount) {
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
	private void renderElementalGauges(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.developer.displayElementalGauges) return;

		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ArrayList<ElementalApplication> appliedElements = new ArrayList<>();

		component
			.getAppliedElements()
			.sortElements((a, b) -> a.getElement().getPriority() - b.getElement().getPriority())
			.forEach(appliedElements::add);

		final int elementCount = appliedElements.size();
		final Iterator<ElementalApplication> aeIterator = appliedElements.iterator();

		Stream
			.iterate(0.0f, n -> (n / 1.25f) < elementCount, n -> n + 1.25f)
			.forEachOrdered(yOffset ->
				renderElementalGauge(entity, aeIterator.next(), yOffset - 0.5f, matrixStack, tickDelta)
			);
	}

	@Unique
	private void renderElementalGauge(final LivingEntity entity, final ElementalApplication application, final float yOffset, final MatrixStack matrixStack, final float tickDelta) {
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

		final float progress = this.getProgress(application, tickDelta);
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
	private float getProgress(ElementalApplication application, float tickDelta) {
		return application instanceof final DurationElementalApplication durationApp
			? (float) ((application.getRemainingTicks() - tickDelta) / durationApp.getDuration())
			: (float) (application.getCurrentGauge() / application.getGaugeUnits());
	}

	@Unique
	private void renderCrystallizeShield(final LivingEntity entity, final MatrixStack matrixStack) {
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
}
