package com.impostorfridays.client.screen;

import com.impostorfridays.net.OpenPickerS2C;
import com.impostorfridays.net.PickerMode;
import com.impostorfridays.net.PickerSelectC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The shared player picker, used by both the Tracking Compass and {@code /sniff}.
 *
 * <p>One screen serves both so the two never drift apart visually. Styled to sit alongside
 * vanilla menus: a dark panel, crisp-bordered heads, and a clear hover state.
 */
public class PlayerPickerScreen extends Screen {

	private static final int PANEL_WIDTH = 220;
	private static final int ROW_HEIGHT = 24;
	private static final int MAX_VISIBLE_ROWS = 8;
	private static final int HEAD_SIZE = 16;

	private static final int PANEL_BG = 0xE0101014;
	private static final int PANEL_BORDER = 0xFF3A3A42;
	private static final int ROW_BG = 0x40FFFFFF;
	private static final int ROW_HOVER = 0x60FFFFFF;
	private static final int ACCENT = 0xFF4FC3F7;

	/** The all-zero UUID stands in for the "Nearest Player" pseudo-entry. */
	private static final UUID NEAREST_SENTINEL = new UUID(0L, 0L);

	private final PickerMode mode;
	private final List<Row> rows = new ArrayList<>();

	private int scrollOffset;
	private int panelLeft;
	private int panelTop;
	private int listTop;

	/** One selectable line: either a real player or the "nearest" pseudo-entry. */
	private record Row(UUID id, String name, boolean nearest) {
	}

	public PlayerPickerScreen(PickerMode mode, List<OpenPickerS2C.Entry> entries) {
		super(Text.literal(mode == PickerMode.SNIFF ? "Sniff a Player" : "Track a Player"));
		this.mode = mode;

		// "Nearest Player" only makes sense for tracking; sniffing needs a deliberate choice.
		if (mode == PickerMode.TRACK) {
			rows.add(new Row(NEAREST_SENTINEL, "Nearest Player", true));
		}
		for (OpenPickerS2C.Entry e : entries) {
			rows.add(new Row(e.id(), e.name(), false));
		}
	}

	@Override
	protected void init() {
		int visibleRows = Math.min(rows.size(), MAX_VISIBLE_ROWS);
		int panelHeight = 34 + visibleRows * ROW_HEIGHT + 8;
		panelLeft = (this.width - PANEL_WIDTH) / 2;
		panelTop = (this.height - panelHeight) / 2;
		listTop = panelTop + 30;
	}

	@Override
	public boolean shouldPause() {
		// Never pause: the match clock keeps running and this must not feel like a break.
		return false;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderBackground(ctx, mouseX, mouseY, delta);

		int visibleRows = Math.min(rows.size(), MAX_VISIBLE_ROWS);
		int panelHeight = 34 + visibleRows * ROW_HEIGHT + 8;

		// Panel with a 1px border.
		ctx.fill(panelLeft - 1, panelTop - 1, panelLeft + PANEL_WIDTH + 1, panelTop + panelHeight + 1,
				PANEL_BORDER);
		ctx.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, PANEL_BG);

		ctx.drawCenteredTextWithShadow(this.textRenderer, this.title,
				panelLeft + PANEL_WIDTH / 2, panelTop + 9, 0xFFFFFFFF);
		ctx.fill(panelLeft + 8, panelTop + 22, panelLeft + PANEL_WIDTH - 8, panelTop + 23, PANEL_BORDER);

		if (rows.isEmpty()) {
			ctx.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("No other players online").formatted(Formatting.GRAY),
					panelLeft + PANEL_WIDTH / 2, listTop + 8, 0xFFAAAAAA);
			super.render(ctx, mouseX, mouseY, delta);
			return;
		}

		for (int i = 0; i < visibleRows; i++) {
			int index = i + scrollOffset;
			if (index >= rows.size()) {
				break;
			}
			renderRow(ctx, rows.get(index), listTop + i * ROW_HEIGHT, mouseX, mouseY);
		}

		if (rows.size() > MAX_VISIBLE_ROWS) {
			ctx.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("Scroll for more").formatted(Formatting.DARK_GRAY),
					panelLeft + PANEL_WIDTH / 2, panelTop + panelHeight - 10, 0xFF808080);
		}

		super.render(ctx, mouseX, mouseY, delta);
	}

	private void renderRow(DrawContext ctx, Row row, int y, int mouseX, int mouseY) {
		int left = panelLeft + 8;
		int right = panelLeft + PANEL_WIDTH - 8;
		boolean hovered = mouseX >= left && mouseX <= right && mouseY >= y && mouseY < y + ROW_HEIGHT - 2;

		ctx.fill(left, y, right, y + ROW_HEIGHT - 2, hovered ? ROW_HOVER : ROW_BG);
		if (hovered) {
			// Accent bar on the left edge to make the hover state unmistakable.
			ctx.fill(left, y, left + 2, y + ROW_HEIGHT - 2, ACCENT);
		}

		int headX = left + 6;
		int headY = y + 3;

		// Crisp 1px border around the head.
		ctx.fill(headX - 1, headY - 1, headX + HEAD_SIZE + 1, headY + HEAD_SIZE + 1, PANEL_BORDER);

		if (row.nearest()) {
			// Simple target glyph rather than a head.
			ctx.fill(headX, headY, headX + HEAD_SIZE, headY + HEAD_SIZE, 0xFF202028);
			ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("◎"),
					headX + HEAD_SIZE / 2, headY + 4, ACCENT);
		} else {
			SkinTextures skin = skinFor(row.id());
			if (skin != null) {
				PlayerSkinDrawer.draw(ctx, skin, headX, headY, HEAD_SIZE);
			} else {
				ctx.fill(headX, headY, headX + HEAD_SIZE, headY + HEAD_SIZE, 0xFF404048);
			}
		}

		Text name = row.nearest()
				? Text.literal(row.name()).formatted(Formatting.AQUA, Formatting.ITALIC)
				: Text.literal(row.name()).formatted(Formatting.WHITE);
		ctx.drawTextWithShadow(this.textRenderer, name, headX + HEAD_SIZE + 8, y + 7, 0xFFFFFFFF);
	}

	private SkinTextures skinFor(UUID id) {
		if (this.client == null || this.client.getNetworkHandler() == null) {
			return null;
		}
		PlayerListEntry entry = this.client.getNetworkHandler().getPlayerListEntry(id);
		return entry == null ? null : entry.getSkinTextures();
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		if (click.button() == 0 && !rows.isEmpty()) {
			int left = panelLeft + 8;
			int right = panelLeft + PANEL_WIDTH - 8;
			int visibleRows = Math.min(rows.size(), MAX_VISIBLE_ROWS);
			for (int i = 0; i < visibleRows; i++) {
				int index = i + scrollOffset;
				if (index >= rows.size()) {
					break;
				}
				int y = listTop + i * ROW_HEIGHT;
				if (mouseX >= left && mouseX <= right && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) {
					select(rows.get(index));
					return true;
				}
			}
		}
		return super.mouseClicked(click, doubled);
	}

	private void select(Row row) {
		ClientPlayNetworking.send(new PickerSelectC2S(mode.ordinal(), row.id(), row.nearest()));
		this.close();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (rows.size() > MAX_VISIBLE_ROWS) {
			int max = rows.size() - MAX_VISIBLE_ROWS;
			scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(vertical)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
}
