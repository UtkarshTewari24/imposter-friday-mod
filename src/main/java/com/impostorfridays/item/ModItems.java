package com.impostorfridays.item;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/** Item registration. */
public final class ModItems {

	public static final Item TRACKING_COMPASS = register(
			"tracking_compass", TrackingCompassItem::new, new Item.Settings().maxCount(1));

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
		// 1.21.11 requires the registry key to be baked into the settings before construction.
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ImpostorFridays.MOD_ID, name));
		Item item = factory.apply(settings.registryKey(key));
		return Registry.register(Registries.ITEM, key, item);
	}

	/** Forces class-loading so the static field above runs during mod init. */
	public static void register() {
		ImpostorFridays.LOGGER.info("Registered items");
	}
}
