package com.impostorfridays.client;

import com.impostorfridays.ImpostorFridays;
import com.impostorfridays.game.Ability;
import com.impostorfridays.net.AbilityUseC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Optional keybinds for the abilities.
 *
 * <p>Registered as real Minecraft keybindings under their own "Impostor Fridays" section in
 * Options → Controls → Key Binds, so they can be rebound like any vanilla control.
 *
 * <p>All of them are UNBOUND by default. Binding a key is opt-in, nothing is taken away from
 * the player's existing controls, and every ability remains usable by command either way — the
 * key just sends the same request the command would.
 */
public final class AbilityKeybinds {

	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of(ImpostorFridays.MOD_ID, "abilities"));

	/** Ability -> its keybinding. Insertion order decides the order shown in Controls. */
	private static final Map<Ability, KeyBinding> BINDINGS = new LinkedHashMap<>();

	private AbilityKeybinds() {
	}

	public static void register() {
		bind(Ability.HUNT, "key.impostorfridays.hunt");
		bind(Ability.BLIND, "key.impostorfridays.blind");
		bind(Ability.GRAVITY, "key.impostorfridays.gravity");
		bind(Ability.STEAL, "key.impostorfridays.steal");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			for (Map.Entry<Ability, KeyBinding> entry : BINDINGS.entrySet()) {
				// wasPressed() drains the queue, so a held key fires once per press.
				while (entry.getValue().wasPressed()) {
					ClientPlayNetworking.send(new AbilityUseC2S(entry.getKey().getId()));
				}
			}
		});
	}

	private static void bind(Ability ability, String translationKey) {
		KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				translationKey, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
		BINDINGS.put(ability, binding);
	}
}
