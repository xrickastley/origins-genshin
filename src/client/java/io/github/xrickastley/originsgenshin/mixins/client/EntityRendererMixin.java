package io.github.xrickastley.originsgenshin.mixins.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

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
import io.github.xrickastley.originsgenshin.components.ElementComponent;

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
		
		if (!(entity instanceof LivingEntity livingEntity)) return;

		// if (!renderReaction(livingEntity, matrixStack)) 
		this.renderElementsIfPresent(livingEntity, matrixStack);
	}

	/*
	protected boolean renderReaction(final LivingEntity livingEntity, final MatrixStack matrixStack) {
		if (!(livingEntity.getWorld() instanceof ClientWorld)) return false;

		if (!livingEntity.isAlive()) return false;

		ILivingEntity livingEntityMixin = ((ILivingEntity) livingEntity);
		
		if (!livingEntityMixin.hasLastTriggeredReaction()) return false;

		if (livingEntity.age > livingEntityMixin.getTriggeredReactionAt() + 10) return false;

		final Iterator<Vec3d> coords = generateTexturesUsingCenter(new Vec3d(0, 0, 0), 0.75, 2).iterator();
		
		renderElement(
			matrixStack,
			(float) coords.next().getZ(),
			livingEntityMixin.getLastTriggeredReaction().getTriggeringElement().getTexture()
		);

		renderElement(
			matrixStack,
			(float) coords.next().getZ(),
			livingEntityMixin.getLastTriggeredReaction().getAuraElement().getTexture()
		);

		return true;
	}
	*/

	protected void renderElement(final MatrixStack matrixStack, float xOffset, Identifier texture) {
		this.renderElement(matrixStack, xOffset, texture, Integer.MAX_VALUE);
	}

	protected void renderElement(final MatrixStack matrixStack, float xOffset, Identifier texture, double secondsLeft) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		matrixStack.push();
		matrixStack.translate(0f, 2f, 0f);
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
		RenderSystem.disableCull();

        tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		matrixStack.pop();
	}

	protected void renderElementsIfPresent(final LivingEntity livingEntity, final MatrixStack matrixStack) {
		try {
			if (!(livingEntity.getWorld() instanceof ClientWorld)) return;

			if (!livingEntity.isAlive()) return;
	
			final ElementComponent component = ElementComponent.KEY.get(livingEntity);
			final Iterator<Vec3d> coords = generateTexturesUsingCenter(new Vec3d(0, 0, 0), 1, (int) component.getAppliedElements().count()).iterator();

			if (logCooldowns.getOrDefault(livingEntity.getUuidAsString(), -1L) < Util.getMeasuringTimeMs()) {
				component
					.getAppliedElements()
					.forEach(application ->
						OriginsGenshin
							.sublogger(this)
							.info("Application: {} GU {}", df.format(application.getCurrentGauge()), application.getElement())
					);

				logCooldowns.put(livingEntity.getUuidAsString(), Util.getMeasuringTimeMs() + 2500);
			}

			component
				.getAppliedElements()
				.forEach(application -> 
					renderElement(matrixStack, (float) coords.next().getZ(), application.getElement().getTexture(), application.getRemainingTicks() / 20.0f)
				);
		} catch (Exception e) {
			if (logCooldowns.getOrDefault("c12dd02b-7f89-4dc6-a1f0-ad56007bb56e", -1L) > Util.getMeasuringTimeMs()) return;

			OriginsGenshin
				.sublogger(this)
				.error("An error occured while trying to render elements: ", e);

			logCooldowns.put("c12dd02b-7f89-4dc6-a1f0-ad56007bb56e", Util.getMeasuringTimeMs() + 2500);
		}
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

	/*
	private void drawCircleOutline(Vec3d center, Tessellator tessellator, Matrix4f posMatrix) {
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		float innerRadius = 20f;
		float totalRadius = 20f;

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

		double subdivisions = 360;

		float x = (float) (center.getX() + (Math.cos((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		float y = (float) (center.getY() - (Math.sin((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		
		bufferBuilder
			.vertex(posMatrix, x, y, (float) center.getZ())
			.color(0xffffffff)
			.next();

		for (int i = 0; i <= subdivisions; i++) {
			float outerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			float outerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			bufferBuilder
				.vertex(posMatrix, outerX, outerY, (float) center.getZ())
				.color(0xffffffff)
				.next();
			
			// Add vertices for the inner circle
			float innerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			float innerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			bufferBuilder
				.vertex(posMatrix, innerX, innerY, (float) center.getZ())
				.color(0xffffffff)
				.next();
		}

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

        tessellator.draw();

		RenderSystem.enableCull();
	}

	private void drawCircle(Vec3d center, Tessellator tessellator, Matrix4f posMatrix) {
		/*
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder
			.vertex(posMatrix, (float) center.getX(), (float) center.getY(), (float) center.getZ())
			.color(0xffffffff)
			.next();

		double radius = 10;
		double subdivisions = 360;
			
		for (int i = 0; i <= subdivisions; i++) {
			float x = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * radius));
			float y = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * radius));
		
			bufferBuilder
				.vertex(posMatrix, x, y, (float) center.getZ())
				.color(0xffffffff)
				.next();
		}

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

        tessellator.draw();

		RenderSystem.enableCull();
		// *./
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		float innerRadius = 0.45f;
		float totalRadius = 0.50f;

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

		double subdivisions = 360;

		float x = (float) (center.getX() + (Math.cos((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		float y = (float) (center.getY() - (Math.sin((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		
		bufferBuilder
			.vertex(posMatrix, x, y, (float) center.getZ())
			.color(0xffffffff)
			.next();

		for (int i = 0; i <= subdivisions; i++) {
			float outerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			float outerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			bufferBuilder
				.vertex(posMatrix, outerX, outerY, (float) center.getZ())
				.color(0xffffffff)
				.next();
			
			// Add vertices for the inner circle
			float innerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			float innerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			bufferBuilder
				.vertex(posMatrix, innerX, innerY, (float) center.getZ())
				.color(0xffffffff)
				.next();
		}

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

        tessellator.draw();

		RenderSystem.enableCull();
	}
	*/
}
