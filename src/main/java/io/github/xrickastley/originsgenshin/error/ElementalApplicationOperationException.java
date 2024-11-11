package io.github.xrickastley.originsgenshin.error;

import java.util.function.BiFunction;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;

public class ElementalApplicationOperationException extends RuntimeException {
	public ElementalApplicationOperationException(Operation operation, ElementalApplication target, ElementalApplication cause) {
		super();		
	}

	public static enum Operation {
		INVALID_NBT_DATA					((_target, _reapplied) -> String.format("Cannot create Elemental Application from the provided NBT!")),
		REAPPLICATION_INVALID_ELEMENT		((target, reapplied) -> String.format("Attempted to use Elemental Application with Element: %s as re-application target for Elemental Application with Element: %s", target.getType().toString(), reapplied.getType().toString())),
		REAPPLICATION_INVALID_TYPES			((target, reapplied) -> String.format("Attempted to use Elemental Application with type: %s as re-application target for Elemental Application with type: %s", target.getType().toString(), reapplied.getType().toString())),
		INVALID_UUID_VALUES					((target, reapplied) -> String.format("Attempted to use Elemental Application with UUID: %s to update Elemental Application with UUID: %s", target.getUuid().toString(), reapplied.getUuid().toString()));

		private final BiFunction<ElementalApplication, ElementalApplication, String> messageSupplier;

		private Operation(BiFunction<ElementalApplication, ElementalApplication, String> messageSupplier) {
			this.messageSupplier = messageSupplier;
		}

		public String getMessage(ElementalApplication target, ElementalApplication cause) {
			return messageSupplier.apply(target, cause);
		}
	} 
}
