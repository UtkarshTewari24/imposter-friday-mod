package com.impostorfridays.net;

import com.impostorfridays.ImpostorFridays;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Asks the client to open the shared player-picker screen.
 *
 * <p>The server sends the candidate list so the client never has to derive who is
 * selectable. Used by both the Tracking Compass and {@code /sniff}.
 */
public record OpenPickerS2C(int mode, List<Entry> entries) implements CustomPayload {

	/** One selectable player. */
	public record Entry(UUID id, String name) {
	}

	public static final CustomPayload.Id<OpenPickerS2C> ID =
			new CustomPayload.Id<>(Identifier.of(ImpostorFridays.MOD_ID, "open_picker"));

	public static final PacketCodec<RegistryByteBuf, OpenPickerS2C> CODEC =
			PacketCodec.of(OpenPickerS2C::write, OpenPickerS2C::read);

	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(mode);
		buf.writeVarInt(entries.size());
		for (Entry e : entries) {
			buf.writeUuid(e.id());
			buf.writeString(e.name());
		}
	}

	private static OpenPickerS2C read(RegistryByteBuf buf) {
		int mode = buf.readVarInt();
		int count = buf.readVarInt();
		List<Entry> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			UUID id = buf.readUuid();
			String name = buf.readString();
			list.add(new Entry(id, name));
		}
		return new OpenPickerS2C(mode, list);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
