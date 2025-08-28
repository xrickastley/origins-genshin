package io.github.xrickastley.originsgenshin.renderer.genshin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.networking.PacketHandler;
import io.github.xrickastley.originsgenshin.networking.ShowElectroChargeS2CPacket;
import io.github.xrickastley.originsgenshin.util.BoxUtil;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.Ease;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class SpecialEffectsRenderer implements PacketHandler<ShowElectroChargeS2CPacket> {
	private static final int MAX_TICKS = 10;
	private static final double POISSON_DENSITY = 1.5;
	private static final Random RANDOM = Random.create();
	private static final int QUICKEN_ITERATIONS = 3;
	private final List<Entry> entries = new ArrayList<>();

	@Override
	public PacketType<ShowElectroChargeS2CPacket> getType() {
		return ShowElectroChargeS2CPacket.TYPE;
	}

	@Override
	public void receive(ShowElectroChargeS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		OriginsGenshin.sublogger().info("Handling packet: ShowElectroChargeS2CPacket");

		final World world = player.getWorld();
		final Entity mainEntity = world.getEntityById(packet.mainEntity());

		if (mainEntity == null) {
			OriginsGenshin.sublogger().warn("Received packet for unknown main Electro-Charged entity, ignoring!");

            return;
		}

		entries.add(
			new ElectroChargedEffect(
				mainEntity,
				IntStream
					.of(packet.otherEntities())
					.mapToObj(world::getEntityById)
					.filter(e -> e != null)
					.toList()
			)
		);
	}

	public void render(WorldRenderContext context) {
		entries.forEach(entry -> entry.render(context, this));
		
		this.renderEffects(context);
	}

	public void tick(ClientWorld world) {
		entries.removeIf(Entry::shouldRemove);
	}

	private void renderQuickenAura(WorldRenderContext context, LivingEntity entity) {
		final Box box = BoxUtil.multiply(entity.getBoundingBox(), 0.75);

		for (int i = 0; i < SpecialEffectsRenderer.QUICKEN_ITERATIONS; i++) {
			final Vec3d initialPos = this.randomInBox(box);
			final Vec3d finalPos = this.randomInBox(box);

			this.renderChargeLine(context, initialPos, finalPos, Colors.DENDRO, Colors.PHYSICAL);
		}
	}

	private Vec3d randomInBox(final Box box) {
		return new Vec3d(
			MathHelper.nextDouble(RANDOM, box.minX, box.maxX),
			MathHelper.nextDouble(RANDOM, box.minY, box.maxY),
			MathHelper.nextDouble(RANDOM, box.minZ, box.maxZ)
		);
	}

	private void renderEffects(WorldRenderContext context) {
		for (final Entity entity : context.world().getEntities()) {
			if (!(entity instanceof final LivingEntity livingEntity)) continue;

			final ElementComponent component = ElementComponent.KEY.get(livingEntity);

			if (!component.hasElementalApplication(Element.QUICKEN)) continue;

			this.renderQuickenAura(context, livingEntity);
		}
	}

	private void renderChargeLine(WorldRenderContext context, Vec3d initialPos, Vec3d finalPos, Color outerColor, Color innerColor) {
	    final Camera camera = context.camera();
	    final Vec3d camPos = camera.getPos();
		
	    final MatrixStack matrices = new MatrixStack();
	    matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
	    matrices.translate(initialPos.x - camPos.x, initialPos.y - camPos.y, initialPos.z - camPos.z);

	    final Tessellator tess = Tessellator.getInstance();
	    final BufferBuilder buf = tess.getBuffer();
	    final Matrix4f posMat = matrices.peek().getPositionMatrix();
	    final Matrix3f nrmMat = matrices.peek().getNormalMatrix();

	    buf.begin(DrawMode.LINES, VertexFormats.LINES);

		final List<Vec3d> positions = this.generatePositions(Vec3d.ZERO, initialPos.subtract(finalPos));

		Vec3d randomVec = Vec3d.ZERO;

		for (int i = 0; i < positions.size(); i++) {
			randomVec = new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

			positions.set(i, positions.get(i).add(randomVec));
		}

		positions.add(0, Vec3d.ZERO);
		positions.add(finalPos.subtract(initialPos));

		for (int i = 1; i < positions.size(); i++) {
			final Vec3d start = positions.get(i - 1);
			final Vec3d end = positions.get(i);
			Vec3d normal = end.normalize();

		    buf.vertex(posMat, (float) start.x, (float) start.y, (float) start.z)
		       .color(outerColor.asARGB())
		       .normal(nrmMat, (float) normal.x, (float) normal.y, (float) normal.z)
		       .next();

		    buf.vertex(posMat, (float) end.x, (float) end.y, (float) end.z)
		       .color(outerColor.asARGB())
		       .normal(nrmMat, (float) normal.x, (float) normal.y, (float) normal.z)
		       .next();
		}

	    RenderSystem.enableBlend();
	    RenderSystem.defaultBlendFunc();
	    RenderSystem.disableCull();
	    RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
	    RenderSystem.setShaderColor(1, 1, 1, 1);

	    RenderSystem.lineWidth(6.0f);

	    tess.draw();

	    buf.begin(DrawMode.LINES, VertexFormats.LINES);
	
		for (int i = 1; i < positions.size(); i++) {
			final Vec3d start = positions.get(i - 1);
			final Vec3d end = positions.get(i);
			Vec3d normal = end.normalize();

		    buf.vertex(posMat, (float) start.x, (float) start.y, (float) start.z)
		       .color(innerColor.asARGB())
		       .normal(nrmMat, (float) normal.x, (float) normal.y, (float) normal.z)
		       .next();

		    buf.vertex(posMat, (float) end.x, (float) end.y, (float) end.z)
		       .color(innerColor.asARGB())
		       .normal(nrmMat, (float) normal.x, (float) normal.y, (float) normal.z)
		       .next();
		}

	    RenderSystem.lineWidth(2.0f);

	    tess.draw();

	    matrices.pop();
	}

	private List<Vec3d> generatePositions(final Vec3d initialPos, final Vec3d finalPos) {
		final Vec3d norm = initialPos.subtract(finalPos);
		final double length = norm.length();
	
		final int n = Math.max(1, this.poisson(SpecialEffectsRenderer.POISSON_DENSITY * length));
		final List<Double> doubles = new ArrayList<>();
	
		for (int i = 0; i < n; i++) doubles.add(RANDOM.nextDouble());
	
		return doubles
			.stream()
			.sorted()
			.map(t -> initialPos.add(norm.multiply(t)).add(new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5)))
			.collect(Collectors.toList());
	}

	private int poisson(double lambda) {
		final double L = Math.exp(-lambda);
	
		int k = 0;
		double p = 1.0;
		do {
			k++;
			p *= SpecialEffectsRenderer.RANDOM.nextDouble();
		} while (p > L);
	
		return k - 1;
	}

	private static abstract class Entry {
		abstract boolean shouldRemove();
		abstract void render(final WorldRenderContext context, final SpecialEffectsRenderer renderer);
	}

	private static class ElectroChargedEffect extends Entry {
		private final long time;
		private final Entity mainEntity;
		private final List<Entity> otherEntities;

		ElectroChargedEffect(Entity mainEntity, List<Entity> otherEntities) {
			this.time = MinecraftClient.getInstance().world.getTime();
			this.mainEntity = mainEntity;
			this.otherEntities = otherEntities;
		}

		boolean shouldRemove() {
			return !mainEntity.isAlive() || otherEntities.isEmpty() || MinecraftClient.getInstance().world.getTime() > this.time + MAX_TICKS;
		}

		void render(final WorldRenderContext context, final SpecialEffectsRenderer renderer) {
			final double gradientStep = MathHelper.clamp(MathHelper.getLerpProgress(MinecraftClient.getInstance().world.getTime() - this.time + context.tickDelta(), 0, 10), 0, 1);
			final Color outerColor = Color.gradientStep(Colors.ELECTRO, Colors.HYDRO, gradientStep, Ease.IN_SINE);
			final Color innerColor = Colors.PHYSICAL;

			for (final Entity other : this.otherEntities) {
				if (other == this.mainEntity) continue;

				renderer.renderChargeLine(context, entityPos(this.mainEntity), entityPos(other), outerColor, innerColor);
			}
		}

		private Vec3d entityPos(Entity entity) {
			return entity.getPos().add(0, entity.getHeight() * 0.5, 0);
		}
	}
}
