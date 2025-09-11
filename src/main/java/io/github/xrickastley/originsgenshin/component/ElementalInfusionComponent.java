package io.github.xrickastley.originsgenshin.component;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;

public final class ElementalInfusionComponent extends ItemComponent {
	public static final ComponentKey<ElementalInfusionComponent> KEY = ComponentRegistry.getOrCreate(OriginsGenshin.identifier("elemental_infusion"), ElementalInfusionComponent.class);

	public ElementalInfusionComponent(ItemStack stack) {
		super(stack);
	}

	public boolean hasElementalInfusion() {
		return this.hasTag("elemental_infusion", NbtElement.COMPOUND_TYPE);
	}

	public @Nullable ElementalApplication getElementalInfusion(LivingEntity target) {
		return this.hasElementalInfusion()
			? ElementalApplication.Builder.CODEC
				.parse(NbtOps.INSTANCE, this.getCompound("elemental_infusion"))
				.resultOrPartial(OriginsGenshin.sublogger()::error)
				.orElseThrow()
				.build(target)
			: null;
	}

	public @Nullable Element getElement() {
		if (!this.hasElementalInfusion()) return null;

		final NbtCompound infusion = this.getCompound("elemental_infusion");

		return Element.valueOf(infusion.getString("element"));
	}

	public double getGaugeUnits() {
		if (!this.hasElementalInfusion()) return 0;

		final NbtCompound infusion = this.getCompound("elemental_infusion");

		return infusion.getDouble("gauge_units");
	}

	public void setElementalInfusion(ElementalApplication.Builder builder) {
		final NbtElement element = ElementalApplication.Builder.CODEC
			.encodeStart(NbtOps.INSTANCE, builder)
			.resultOrPartial(OriginsGenshin.sublogger()::error)
			.orElseThrow();

		this.putCompound("elemental_infusion", (NbtCompound) element);
	}

	public InternalCooldownContext.Builder getInternalCooldown() {
		return this.hasTag("internal_cooldown", NbtElement.COMPOUND_TYPE)
			? InternalCooldownContext.Builder.CODEC
				.parse(NbtOps.INSTANCE, this.getCompound("internal_cooldown"))
				.resultOrPartial(OriginsGenshin.sublogger()::error)
				.orElseThrow()
			: InternalCooldownContext.Builder.ofNone();
	}

	public void setInternalCooldown(InternalCooldownContext.Builder builder) {
		final NbtElement element = InternalCooldownContext.Builder.CODEC
			.encodeStart(NbtOps.INSTANCE, builder)
			.resultOrPartial(OriginsGenshin.sublogger()::error)
			.orElseThrow();

		this.putCompound("internal_cooldown", (NbtCompound) element);
	}

	public static Optional<ElementalDamageSource> applyToDamageSource(DamageSource source, Entity target) {
		try {
			if (!(target instanceof final LivingEntity livingTarget) || !(source.getAttacker() instanceof final LivingEntity attacker)) return Optional.empty();

			if (!(attacker.getMainHandStack().getItem() instanceof ToolItem)) return Optional.empty();

			final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(attacker.getMainHandStack());

			if (!component.hasElementalInfusion()) return Optional.empty();

			return Optional.of(
				new ElementalDamageSource(
					source,
					component.getElementalInfusion(livingTarget),
					component.getInternalCooldown().build(attacker)
				)
			);
		} catch (Exception e) {
			OriginsGenshin.sublogger().error("Error on DS apply; supressing and skipping...", e);

			return Optional.empty();
		}
	}

	public static boolean applyInfusion(ItemStack stack, ElementalApplication.Builder applicationBuilder, InternalCooldownContext.Builder icdBuilder) {
		final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(stack);

		if (component == null) return false;

		component.setElementalInfusion(applicationBuilder);
		component.setInternalCooldown(icdBuilder);

		ElementalInfusionComponent.KEY.sync(stack);

		return true;
	}

	public static boolean removeInfusion(ItemStack stack) {
		final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(stack);

		if (component == null || !component.hasElementalInfusion()) return false;

		component.remove("elemental_infusion");
		component.remove("internal_cooldown");

		ElementalInfusionComponent.KEY.sync(stack);

		return true;
	}
}
