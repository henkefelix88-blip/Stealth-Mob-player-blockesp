package de.sodium.highlighter.gui;

import de.sodium.highlighter.config.HighlightConfig;
import de.sodium.highlighter.config.HighlightConfig.HighlightEntry;
import de.sodium.highlighter.config.HighlightConfig.HighlightType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.List;

public class HighlighterScreen extends Screen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 280;
    private static final int[] TAB_COLORS = {0xFF4FC3F7, 0xFF81C784, 0xFFE57373};
    private static final String[] TAB_LABELS = {"Entities", "Blocks", "Players"};
    private static final HighlightType[] TAB_TYPES = {
            HighlightType.ENTITY, HighlightType.BLOCK, HighlightType.PLAYER
    };

    private int selectedTab = 0;
    private int panelX, panelY;
    private int scrollOffset = 0;

    // Add entry state
    private TextFieldWidget nameField;
    private boolean addingEntry = false;
    private int pendingColor = 0xFF00FF00;

    // Preset colors
    private static final int[] PRESET_COLORS = {
            0xFFFF3333, // Red
            0xFF33FF33, // Green
            0xFF3399FF, // Blue
            0xFFFFFF33, // Yellow
            0xFFFF33FF, // Magenta
            0xFF33FFFF, // Cyan
            0xFFFF8800, // Orange
            0xFFFFFFFF, // White
            0xFFAA00FF, // Purple
            0xFF00FFAA, // Teal
    };

    public HighlighterScreen() {
        super(Text.literal("Sodium Highlighter"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;

        // Global toggle
        ButtonWidget toggleBtn = ButtonWidget.builder(
                Text.literal(HighlightConfig.get().globalEnabled ? "§aEnabled" : "§cDisabled"),
                btn -> {
                    HighlightConfig.get().globalEnabled = !HighlightConfig.get().globalEnabled;
                    HighlightConfig.get().save();
                    btn.setMessage(Text.literal(HighlightConfig.get().globalEnabled ? "§aEnabled" : "§cDisabled"));
                })
                .dimensions(panelX + PANEL_W - 110, panelY + 8, 100, 18)
                .build();
        addDrawableChild(toggleBtn);

        // Tab buttons
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            ButtonWidget tab = ButtonWidget.builder(
                    Text.literal(TAB_LABELS[i]),
                    btn -> { selectedTab = idx; scrollOffset = 0; rebuildWidgets(); })
                    .dimensions(panelX + 8 + i * 88, panelY + 30, 84, 18)
                    .build();
            addDrawableChild(tab);
        }

        // Scroll buttons
        ButtonWidget scrollUp = ButtonWidget.builder(Text.literal("▲"),
                btn -> { if (scrollOffset > 0) scrollOffset--; })
                .dimensions(panelX + PANEL_W - 20, panelY + 56, 16, 16)
                .build();
        addDrawableChild(scrollUp);

        ButtonWidget scrollDown = ButtonWidget.builder(Text.literal("▼"),
                btn -> scrollOffset++)
                .dimensions(panelX + PANEL_W - 20, panelY + PANEL_H - 50, 16, 16)
                .build();
        addDrawableChild(scrollDown);

        // Add Entry button
        ButtonWidget addBtn = ButtonWidget.builder(Text.literal("§a+ Add Entry"),
                btn -> { addingEntry = !addingEntry; rebuildWidgets(); })
                .dimensions(panelX + 8, panelY + PANEL_H - 28, 120, 18)
                .build();
        addDrawableChild(addBtn);

        // Keybind settings button
        ButtonWidget keybindBtn = ButtonWidget.builder(Text.literal("§6⌨ Keybinds"),
                btn -> client.setScreen(new KeybindConfigScreen()))
                .dimensions(panelX + PANEL_W - 150, panelY + PANEL_H - 28, 72, 18)
                .build();
        addDrawableChild(keybindBtn);

        // Close button
        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Close"),
                btn -> close())
                .dimensions(panelX + PANEL_W - 70, panelY + PANEL_H - 28, 62, 18)
                .build();
        addDrawableChild(closeBtn);

        // Add entry form
        if (addingEntry) {
            nameField = new TextFieldWidget(textRenderer,
                    panelX + 8, panelY + PANEL_H - 52, 160, 16,
                    Text.literal("ID / Name"));
            nameField.setPlaceholder(Text.literal("e.g. minecraft:creeper"));
            addDrawableChild(nameField);

            // Color presets
            for (int i = 0; i < PRESET_COLORS.length; i++) {
                final int color = PRESET_COLORS[i];
                final int ci = i;
                ButtonWidget colorBtn = ButtonWidget.builder(Text.literal(" "),
                        btn -> { pendingColor = color; })
                        .dimensions(panelX + 175 + i * 14, panelY + PANEL_H - 52, 12, 16)
                        .build();
                addDrawableChild(colorBtn);
            }

            ButtonWidget confirmAdd = ButtonWidget.builder(Text.literal("§aAdd"),
                    btn -> {
                        if (nameField != null && !nameField.getText().isBlank()) {
                            HighlightConfig.get().addEntry(new HighlightEntry(
                                    nameField.getText().trim(),
                                    TAB_TYPES[selectedTab],
                                    pendingColor, 0.8f, false));
                            addingEntry = false;
                            rebuildWidgets();
                        }
                    })
                    .dimensions(panelX + 136, panelY + PANEL_H - 52, 34, 16)
                    .build();
            addDrawableChild(confirmAdd);
        }

        // Entry rows
        List<HighlightEntry> entries = HighlightConfig.get().entries.stream()
                .filter(e -> e.type == TAB_TYPES[selectedTab]).toList();

        int rowY = panelY + 56;
        int visibleRows = 6;
        for (int i = scrollOffset; i < entries.size() && i < scrollOffset + visibleRows; i++) {
            final HighlightEntry entry = entries.get(i);
            int ry = rowY + (i - scrollOffset) * 28;

            // Toggle enable
            ButtonWidget enableBtn = ButtonWidget.builder(
                    Text.literal(entry.enabled ? "§a●" : "§c●"),
                    btn -> {
                        entry.enabled = !entry.enabled;
                        HighlightConfig.get().save();
                        btn.setMessage(Text.literal(entry.enabled ? "§a●" : "§c●"));
                    })
                    .dimensions(panelX + 8, ry, 16, 16)
                    .build();
            addDrawableChild(enableBtn);

            // Through-walls toggle
            ButtonWidget wallBtn = ButtonWidget.builder(
                    Text.literal(entry.throughWalls ? "§eW" : "§7W"),
                    btn -> {
                        entry.throughWalls = !entry.throughWalls;
                        HighlightConfig.get().save();
                        btn.setMessage(Text.literal(entry.throughWalls ? "§eW" : "§7W"));
                    })
                    .dimensions(panelX + 28, ry, 16, 16)
                    .build();
            addDrawableChild(wallBtn);

            // Color cycle
            ButtonWidget colorBtn = ButtonWidget.builder(Text.literal("C"),
                    btn -> {
                        int idx = 0;
                        for (int ci = 0; ci < PRESET_COLORS.length; ci++) {
                            if (PRESET_COLORS[ci] == entry.color) { idx = ci; break; }
                        }
                        entry.color = PRESET_COLORS[(idx + 1) % PRESET_COLORS.length];
                        HighlightConfig.get().save();
                    })
                    .dimensions(panelX + 48, ry, 16, 16)
                    .build();
            addDrawableChild(colorBtn);

            // Remove
            ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("§c✕"),
                    btn -> {
                        HighlightConfig.get().removeEntry(entry);
                        rebuildWidgets();
                    })
                    .dimensions(panelX + PANEL_W - 38, ry, 16, 16)
                    .build();
            addDrawableChild(removeBtn);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Backdrop
        ctx.fillGradient(0, 0, width, height, 0xAA000000, 0xCC000000);

        // Panel background
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFF1A1A2E);
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFF4FC3F7); // top accent
        ctx.drawBorder(panelX, panelY, PANEL_W, PANEL_H, 0xFF4FC3F7);

        // Title
        ctx.drawText(textRenderer, "§b§lSodium §fHighlighter",
                panelX + 10, panelY + 10, 0xFFFFFF, true);

        // Tab underline
        int tabColor = TAB_COLORS[selectedTab];
        ctx.fill(panelX + 8 + selectedTab * 88, panelY + 48,
                panelX + 8 + selectedTab * 88 + 84, panelY + 50, tabColor);

        // Entry rows
        List<HighlightEntry> entries = HighlightConfig.get().entries.stream()
                .filter(e -> e.type == TAB_TYPES[selectedTab]).toList();

        int rowY = panelY + 56;
        int visibleRows = 6;
        for (int i = scrollOffset; i < entries.size() && i < scrollOffset + visibleRows; i++) {
            HighlightEntry entry = entries.get(i);
            int ry = rowY + (i - scrollOffset) * 28;

            // Row background
            int rowBg = (i % 2 == 0) ? 0xFF16213E : 0xFF0F3460;
            ctx.fill(panelX + 8, ry - 2, panelX + PANEL_W - 8, ry + 18, rowBg);

            // Color swatch
            int swatchColor = entry.color | 0xFF000000;
            ctx.fill(panelX + 68, ry + 2, panelX + 82, ry + 14, swatchColor);
            ctx.drawBorder(panelX + 68, ry + 2, 14, 12, 0xFFFFFFFF);

            // ID label
            String label = entry.id.length() > 28 ? entry.id.substring(0, 26) + ".." : entry.id;
            ctx.drawText(textRenderer, label, panelX + 90, ry + 4, 0xFFEEEEEE, false);

            // Opacity label
            ctx.drawText(textRenderer,
                    String.format("%.0f%%", entry.opacity * 100),
                    panelX + PANEL_W - 70, ry + 4, 0xFFAAAAAA, false);
        }

        if (entries.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7No entries. Click §a+ Add Entry§7 to add one."),
                    panelX + PANEL_W / 2, panelY + 130, 0xFFFFFF);
        }

        // Add form header
        if (addingEntry) {
            ctx.fill(panelX + 8, panelY + PANEL_H - 58, panelX + PANEL_W - 8, panelY + PANEL_H - 33, 0xFF0D1B2A);
            ctx.drawText(textRenderer, "§eNew Entry:", panelX + 8, panelY + PANEL_H - 68, 0xFFFFAA, false);

            // Draw color preview circles
            for (int i = 0; i < PRESET_COLORS.length; i++) {
                int cx = panelX + 175 + i * 14 + 1;
                int cy = panelY + PANEL_H - 52 + 2;
                ctx.fill(cx, cy, cx + 10, cy + 12, PRESET_COLORS[i] | 0xFF000000);
                if (PRESET_COLORS[i] == pendingColor) {
                    ctx.drawBorder(cx - 1, cy - 1, 12, 14, 0xFFFFFFFF);
                }
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) scrollOffset++;
        else if (scrollOffset > 0) scrollOffset--;
        rebuildWidgets();
        return true;
    }
}
