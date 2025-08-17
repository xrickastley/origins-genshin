package io.github.xrickastley.originsgenshin.particle;
import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class TextBillboardParticle extends BillboardParticle {
	protected static final Identifier GENSHIN_FONT = OriginsGenshin.identifier("genshin");

	protected int fadeAge;
	protected int scaleAge;
	protected int color;
	protected OrderedText text;

	protected TextBillboardParticle(ClientWorld clientWorld, double x, double y, double z, double color) {
		super(clientWorld, x, y, z);

		this.collidesWithWorld = false;
		this.gravityStrength = 0f;
		this.velocityY = 0d;
		this.maxAge = 75;
		this.fadeAge = maxAge - 25;
		this.scaleAge = 5;
		this.color = MathHelper.floor(color);
	}

	protected TextBillboardParticle setText(String text) {
		return this.setText(text, Style.EMPTY);
	}

	protected TextBillboardParticle setText(String text, Style style) {
		return this.setText(Text.literal(text).setStyle(style));
	}

	protected TextBillboardParticle setText(MutableText text) {
		this.text = text.asOrderedText();

		return this;
	}

	@Override
	protected float getMinU() {
		return 0;
	}

	@Override
	protected float getMaxU() {
		return 1;
	}

	@Override
	protected float getMinV() {
		return 0;
	}

	@Override
	protected float getMaxV() {
		return 1;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.CUSTOM;
	}

	@Override
	public void tick() {
		super.tick();
		if (!dead && age >= maxAge - fadeAge) {
			float ageDiff = Math.max(maxAge - age, 0f);
			alpha = ageDiff / fadeAge;
		}
	}

	public abstract void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta);
}