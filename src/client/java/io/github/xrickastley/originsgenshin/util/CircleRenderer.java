package io.github.xrickastley.originsgenshin.util;

import java.util.ArrayList;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class that renders circles around a certain point.
 */
public class CircleRenderer {
	protected double x;
	protected double y;
	protected double z;
	protected double scaleFactor = 1 / MinecraftClient.getInstance().getWindow().getScaleFactor();
	protected ArrayList<Circle> dataArray = new ArrayList<>();

	public CircleRenderer(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	/**
	 * Sets the scale factor between the source resolution and the target resolution.
	 * @param scaleFactor
	 * @return
	 */
	public CircleRenderer setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;

		return this;
	}

	/**
	 * Adds a circle with the set point as the center.
	 * @param radius The radius of this circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer add(double radius, double percentFilled, Color color) {
		this.dataArray.add(
			new Circle(radius, percentFilled, color.asARGB())
		);

		return this;
	}

	/**
	 * Adds a circle with the set point as the center.
	 * @param radius The radius of this circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer add(double radius, double percentFilled, int color) {
		this.dataArray.add(
			new Circle(radius, percentFilled, color)
		);

		return this;
	}

	/**
	 * Adds a circle outline with the set point as the center.
	 * @param radius The radius of the inner circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param length The length of the outline. The resulting circle's radius, including the inner circle will be {@code radius + length}.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer addOutline(double radius, double length, double percentFilled, Color color) {
		this.dataArray.add(
			new CircleOutline(radius, length, percentFilled, color.asARGB())
		);

		return this;
	}

	/**
	 * Adds a circle outline with the set point as the center.
	 * @param radius The radius of the inner circle. This is measured in pixels using the Minecraft window's resolution.
	 * @param length The length of the outline. The resulting circle's radius, including the inner circle will be {@code radius + length}.
	 * @param percentFilled A percentage in decimal form indicating how much of the circle is filled in. Circles are rendered starting from Quadrant 2 (90°) back to Quadrant 2 (90° + 360°).
	 * @param color The color of this circle.
	 * @return This {@code CircleRenderer}
	 */
	public CircleRenderer addOutline(double radius, double length, double percentFilled, int color) {
		this.dataArray.add(
			new CircleOutline(radius, length, percentFilled, color)
		);

		return this;
	}

	/**
	 * Draws all the data added to this {@code CircleRenderer}, then clears all the previous data. This {@code CircleRenderer} can still be used even after {@code draw()} has been invoked, but the data provided before {@code draw()} will be cleared.
	 * @param tessellator The {@code Tessellator} instance. Obtained from {@code Tessellator.getInstance()}.
	 * @param posMatrix The position matrix. Normally obtained from {@code <MatrixStack>.peek().getPositionMatrix()}.
	 */
	public void draw(Tessellator tessellator, Matrix4f posMatrix) {
		Vec3d center = new Vec3d(x, y, 0)
			.multiply(scaleFactor);

		dataArray.forEach(circle -> {
			if (circle instanceof CircleOutline circleOutline) {
				drawCircleOutline(center, circleOutline, tessellator, posMatrix);
			} else {
				drawCircle(center, circle, tessellator, posMatrix);
			}
		});

		dataArray.clear();
	}

	private void drawCircle(Vec3d center, Circle circle, Tessellator tessellator, Matrix4f posMatrix) {
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder
			.vertex(posMatrix, (float) center.getX(), (float) center.getY(), (float) center.getZ())
			.color(circle.getColor())
			.next();

		double subdivisions = Math.ceil(360.0 * circle.getPercentFilled());
			
		for (int i = 0; i <= subdivisions; i++) {
			float x = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * (circle.getRadius() * scaleFactor)));
			float y = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * (circle.getRadius() * scaleFactor)));
		
			bufferBuilder
				.vertex(posMatrix, x, y, (float) z)
				.color(circle.getColor())
				.next();
		}
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		tessellator.draw();
	}

	private void drawCircleOutline(Vec3d center, CircleOutline circleOutline, Tessellator tessellator, Matrix4f posMatrix) {
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		float innerRadius = (float) (circleOutline.getRadius() * scaleFactor);
		float totalRadius = (float) ((circleOutline.getRadius() + circleOutline.getLength()) * scaleFactor);

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

		double subdivisions = Math.ceil(360.0 * circleOutline.getPercentFilled());

		float x = (float) (center.getX() + (Math.cos((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		float y = (float) (center.getY() - (Math.sin((0 * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
		
		bufferBuilder
			.vertex(posMatrix, x, y, (float) center.getZ())
			.color(circleOutline.getColor())
			.next();

		for (int i = 0; i <= subdivisions; i++) {
			float outerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			float outerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * totalRadius));
			bufferBuilder
				.vertex(posMatrix, outerX, outerY, (float) center.getZ())
				.color(circleOutline.getColor())
				.next();
			
			// Add vertices for the inner circle
			float innerX = (float) (center.getX() + (Math.cos((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			float innerY = (float) (center.getY() - (Math.sin((i * (Math.PI / 180)) + (Math.PI / 2)) * innerRadius));
			bufferBuilder
				.vertex(posMatrix, innerX, innerY, (float) center.getZ())
				.color(circleOutline.getColor())
				.next();
		}
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		tessellator.draw();
	}

	private class Circle {
		protected double radius;
		protected double percentFilled;
		protected int color;

		protected Circle(double radius, double percentFilled, int color) {
			this.radius = radius;
			this.percentFilled = percentFilled;
			this.color = color;
		}

		public double getRadius() {
			return radius;
		}

		public int getColor() {
			return color;
		}

		public double getPercentFilled() {
			return percentFilled;
		}
	}

	private class CircleOutline extends Circle {
		protected double length;

		protected CircleOutline(double radius, double length, double percentFilled, int color) {
			super(radius, percentFilled, color);

			this.length = length;
		}

		public double getLength() {
			return length;
		}
	}
}