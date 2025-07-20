package io.github.xrickastley.originsgenshin.mixin;

import com.mojang.serialization.Lifecycle;
import io.github.xrickastley.originsgenshin.interfaces.SimpleRegistryAccess;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.registry.SimpleRegistry;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements Registry<T>, SimpleRegistryAccess {
	@Shadow
	@Final
	private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

	@Shadow
	@Final
	private Object2IntMap<T> entryToRawId;

	@Shadow
	@Final
	private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

	@Shadow
	@Final
	private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

	@Shadow
	@Final
	private Map<T, RegistryEntry.Reference<T>> valueToEntry;

	@Shadow
	@Final
	private Map<T, Lifecycle> entryToLifecycle;

	@Shadow
	private boolean frozen;


	@Shadow
	private int nextId;

	@Final
	@Inject(
		method = "freeze",
		at = @At("HEAD"),
		cancellable = true
	)
	private void noFreezeIfReloadable(CallbackInfoReturnable<Registry<T>> cir) {
		if (!this.frozen && OriginsGenshinReloadListener.hasReloadableRegistry(this)) cir.setReturnValue((Registry<T>) this);
	}

	@Unique
	public void originsgenshin$clearEntries() {
		this.keyToEntry.clear();
        this.idToEntry.clear();
        this.valueToEntry.clear();
        this.rawIdToEntry.clear();
        this.entryToRawId.clear();
        this.entryToLifecycle.clear();
		this.nextId = 0;
	}
}
