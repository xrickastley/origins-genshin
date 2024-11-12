package io.github.xrickastley.originsgenshin.mixin.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplication.Type;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Colors;
import me.shedaniel.autoconfig.AutoConfig;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Debug(export = true)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	@Unique
	protected ConcurrentHashMap<String, Long> logCooldowns = new ConcurrentHashMap<>();

	@Unique
	private final DecimalFormat df = new DecimalFormat("0.0");

	@Shadow
	@Final
	protected EntityRenderDispatcher dispatcher;

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Inject(
		method = "render",
		at = @At("HEAD")
	)
	protected void addElementRenderer(final Entity entity, final float yaw, final float tickDelta, final MatrixStack matrixStack, final VertexConsumerProvider vertexConsumers, final int light, CallbackInfo ci) {
		// TODO: Reaction rendering: if MULTIPLE reactions are triggered, the FIRST reaction triggered will be shown, for 0.5s.
		
		if (!(entity instanceof final LivingEntity livingEntity)) return;

		this.renderElementsIfPresent(livingEntity, matrixStack, tickDelta);
		this.renderElementalGauges(livingEntity, matrixStack, tickDelta);
	}

	protected void renderElementsIfPresent(final LivingEntity livingEntity, final MatrixStack matrixStack, final float tickDelta) {
		if (!(livingEntity.getWorld() instanceof ClientWorld)) return;

		if (!livingEntity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);

		if (component.getAppliedElements().count() == 0) return;

		final int priority = component
			.getAppliedElements()
			.filter(application -> application.getElement().hasTexture())
			.sorted(Comparator.comparingDouble(application -> application.getElement().getPriority()))
			.findFirst()
			.map(application -> application.getElement().getPriority())
			.orElse(-1);
		
		if (priority == -1) return;
		
		final ArrayList<ElementalApplication> elementArray = new ArrayList<>();
		
		component
			.getAppliedElements()
			.filter(application -> application.getElement().getPriority() == priority)
			.forEach(elementArray::add);

		final int elementCount = elementArray.size();

		final Iterator<Vec3d> coords = this.generateTexturesUsingCenter(new Vec3d(0, 0, 0), 1, elementCount).iterator();

		/*
		if (logCooldowns.getOrDefault(livingEntity.getUuidAsString(), -1L) < Util.getMeasuringTimeMs()) {
			component
				.getAppliedElements()
				.forEach(application ->
					OriginsGenshin
						.sublogger(this)
						.info("Application: {} GU {}", df.format(application.getCurrentGauge()), application.getElement())
				);
		
			logCooldowns.put(livingEntity.getUuidAsString(), Util.getMeasuringTimeMs() + 10_000);
		}
		
		// System.out.println(elementsToRender.count());
		*/
		
		elementArray
			.forEach(application -> {	
				try {
					this.renderElement(
						livingEntity,
						matrixStack, 
						(float) coords.next().getZ(), 
						application.getElement().getTexture(), 
						(application.getRemainingTicks() - tickDelta) / 20.0f
					);
				} catch (Exception e) {
					if (logCooldowns.getOrDefault("c12dd02b-7f89-4dc6-a1f0-ad56007bb56e", -1L) > Util.getMeasuringTimeMs()) return;
				
					OriginsGenshin
						.sublogger(this)
						.error("An error occured while trying to render elements: ", e);

					logCooldowns.put("c12dd02b-7f89-4dc6-a1f0-ad56007bb56e", Util.getMeasuringTimeMs() + 25_000);
				}
			});
	}

	protected void renderElement(final LivingEntity livingEntity, final MatrixStack matrixStack, float xOffset, Identifier texture) {
		this.renderElement(livingEntity, matrixStack, xOffset, texture, Integer.MAX_VALUE);
	}

	protected void renderElement(final LivingEntity livingEntity, final MatrixStack matrixStack, float xOffset, Identifier texture, double secondsLeft) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		matrixStack.push();
		matrixStack.translate(0, livingEntity.getBoundingBox().getLengthY(), 0);
		matrixStack.multiply(dispatcher.getRotation());
		matrixStack.scale(-0.50F, 0.50F, 0.50F);

        float finalXOffset = -0.5f + xOffset;
        float yOffset = 0.5f;

		Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, 0 + finalXOffset, 0 + yOffset, 0).texture(0f, 1f).next();
        buffer.vertex(positionMatrix, 1 + finalXOffset, 0 + yOffset, 0).texture(1f, 1f).next();
        buffer.vertex(positionMatrix, 1 + finalXOffset, 1 + yOffset, 0).texture(1f, 0f).next();
        buffer.vertex(positionMatrix, 0 + finalXOffset, 1 + yOffset, 0).texture(0f, 0f).next();

		float blinkSeconds = 1.5f;
		float blinkCount = 3;
		float blinkInterval = blinkSeconds / blinkCount;
		float intervalSplit = blinkInterval / 2f;

		float alpha = (double) secondsLeft <= (blinkSeconds + intervalSplit)
			? secondsLeft % blinkInterval <= intervalSplit
				? (float) MathHelper.lerp((secondsLeft % blinkInterval) / intervalSplit, 0f, 1f)
				: (float) MathHelper.lerp(((secondsLeft % blinkInterval) - 0.25) / intervalSplit, 1f, 0f)
			: 1f;

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();

		tessellator.draw();

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		matrixStack.pop();
	}

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

	protected void renderElementalGauges(final LivingEntity livingEntity, final MatrixStack matrixStack, final float tickDelta) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.developer.displayElementalGauges) return;

		if (!(livingEntity.getWorld() instanceof ClientWorld)) return;

		if (!livingEntity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);
		final ArrayList<ElementalApplication> appliedElements = new ArrayList<>();
		
		component
			.getAppliedElements()
			.sorted(Comparator.comparingDouble(application -> application.getElement().getPriority()))
			.forEachOrdered(appliedElements::add);

		final int elementCount = appliedElements.size();
		final Iterator<ElementalApplication> aeIterator = appliedElements.iterator();
		
		Stream
			.iterate(0.0f, n -> (n / 1.5f) < elementCount, n -> n + 1.5f)
			.forEachOrdered(yOffset -> {		
				try {
					renderElementalGauge(livingEntity, aeIterator.next(), yOffset, matrixStack, tickDelta);
				} catch (Exception e) {
					System.out.println(e);
				}
			});
	}

	protected void renderElementalGauge(final LivingEntity livingEntity, final ElementalApplication application, final float yOffset, final MatrixStack matrixStack, final float tickDelta) {
		if (application.shouldBeRemoved()) return;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		float scale = 0.35f;

		matrixStack.push();
		matrixStack.translate(
			0f, 
			livingEntity.getBoundingBox().getLengthY() * 1.15,
			0f
		);
		matrixStack.multiply(dispatcher.getRotation());
		matrixStack.scale(-scale, scale, scale);

		float finalWidth = application.isUsingGaugeUnits()
			? (float) (2.5 * application.getGaugeUnits())
			: 5f;
		float xOffset = (float) (livingEntity.getBoundingBox().getLengthX() * 0.85f) / scale;

		Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, 0 + xOffset, 0 - yOffset, 0).color(Colors.PHYSICAL.asARGB()).next();
        buffer.vertex(positionMatrix, finalWidth + xOffset, 0 - yOffset, 0).color(Colors.PHYSICAL.asARGB()).next();
        buffer.vertex(positionMatrix, finalWidth + xOffset, 1 - yOffset, 0).color(Colors.PHYSICAL.asARGB()).next();
        buffer.vertex(positionMatrix, 0 + xOffset, 1 - yOffset, 0).color(Colors.PHYSICAL.asARGB()).next();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		tessellator.draw();

		final float progress = this.getProgress(application, tickDelta);
		final int color = application.getElement().getDamageColor().asARGB();
		
		buffer = tessellator.getBuffer();
		
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, xOffset, 0 - yOffset, -0.0001f).color(color).next();
        buffer.vertex(positionMatrix, (finalWidth * progress) + xOffset, 0 - yOffset, -0.0001f).color(color).next();
        buffer.vertex(positionMatrix, (finalWidth * progress) + xOffset, 1 - yOffset, -0.0001f).color(color).next();
        buffer.vertex(positionMatrix, xOffset, 1 - yOffset, -0.0001f).color(color).next(); 
		
		tessellator.draw();

		matrixStack.pop();
	}

	@Unique
	protected float getProgress(ElementalApplication application, float tickDelta) {
		if (application.getType() == Type.GAUGE_UNITS) {
			// System.out.printf("Current gauge: %.2f, Gauge units: %.2f\n", application.getCurrentGauge(), application.getGaugeUnits());

			return (float) (application.getCurrentGauge() / application.getGaugeUnits());
		} else {
			// System.out.printf("Remaining ticks: %.2f, Duration: %.2f\n", application.getRemainingTicks() - tickDelta, application.getDuration());

			return (float) ((application.getRemainingTicks() - tickDelta) / application.getDuration());
		}
	}
}
