package io.github.xrickastley.originsgenshin.elements;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

public class ElementalDamageSource extends DamageSource {
	private final ElementalApplication application;
	private final String sourceTag;

	/**
	 * Creates an {@link ElementalDamageSource} from an already existing {@link DamageSource}.
	 * @param source The {@code DamageSource} to turn into an {@code ElementalDamageSource}, using it's source and attacker values. For positions, use {@link #ElementalDamageSource(RegistryEntry, Vec3d, ElementalApplication, String)} instead.
	 * @param application The Elemental Application of this {@code ElementalDamageSource}. This is the Elemental Application that will be applied to the target entity, if possible.
	 * @param sourceTag The source tag of this {@code DamageSource}. A source tag controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different between source tags.
	 */
	public ElementalDamageSource(final DamageSource source, final ElementalApplication application, final String sourceTag) {
		super(source.getTypeRegistryEntry(), source.getSource(), source.getAttacker());
		
		this.application = application;
		this.sourceTag = sourceTag;
	}

	/**
	 * Creates an {@link ElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param source The source entity of this {@code DamageSource}. This is the entity that dealt the DMG. (ex. arrow, fireball)
	 * @param attacker The attacker this {@code DamageSource} originated from. This is the entity that attacked. (ex. Zombie, Creeper)
	 * @param application The Elemental Application of this {@code ElementalDamageSource}. This is the Elemental Application that will be applied to the target entity, if possible.
	 * @param sourceTag The source tag of this {@code DamageSource}. A source tag controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different between source tags.
	 */
	public ElementalDamageSource(final RegistryEntry<DamageType> type, @Nullable final Entity source, @Nullable final Entity attacker, final ElementalApplication application, final String sourceTag) {
		super(type, source, attacker);

		this.application = application;
		this.sourceTag = sourceTag;
	}
	
	/**
	 * Creates an {@link ElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param attacker The position this {@code DamageSource} originated from.
	 * @param application The Elemental Application of this {@code ElementalDamageSource}. This is the Elemental Application that will be applied to the target entity, if possible.
	 * @param sourceTag The source tag of this {@code DamageSource}. A source tag controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different between source tags.
	 */
	public ElementalDamageSource(final RegistryEntry<DamageType> type, final Vec3d position, final ElementalApplication application, final String sourceTag) {
		super(type, position);

		this.application = application;
		this.sourceTag = sourceTag;
	}
	
	/**
	 * Creates an {@link ElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param attacker The attacker this {@code DamageSource} originated from. This is the entity that attacked. (ex. Zombie, Creeper)
	 * @param application The Elemental Application of this {@code ElementalDamageSource}. This is the Elemental Application that will be applied to the target entity, if possible.
	 * @param sourceTag The source tag of this {@code DamageSource}. A source tag controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different between source tags.
	 */
	public ElementalDamageSource(final RegistryEntry<DamageType> type, @Nullable final Entity attacker, final ElementalApplication application, final String sourceTag) {
		super(type, attacker, attacker);

		this.application = application;
		this.sourceTag = sourceTag;
	}
	
	/**
	 * Creates an {@link ElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param application The Elemental Application of this {@code ElementalDamageSource}. This is the Elemental Application that will be applied to the target entity, if possible.
	 * @param sourceTag The source tag of this {@code DamageSource}. A source tag controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different between source tags.
	 */
	public ElementalDamageSource(final RegistryEntry<DamageType> type, final ElementalApplication application, final String sourceTag) {
		super(type);

		this.application = application;
		this.sourceTag = sourceTag;
	}

	public ElementalApplication getElementalApplication() {
		return this.application;
	}

	public String getSourceTag() {
		return this.sourceTag;
	}
}
