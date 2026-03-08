package de.sodium.highlighter.gui;

import de.sodium.highlighter.config.KeybindConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeybindConfigScreen extends Screen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 260;
    private int panelX, panelY;

    // Which slot is being re-bound right now (-1 = none)
    private int listeningIndex = -1;

    // Ordered map: action key → display label
    private static final String[] ACTION_KEYS = {
            "open_gui",
            "toggle",
            "open_keybind_gui"
    };
    private static final String[] ACTION_LABELS = {
            "Open Highlighter GUI",
            "Toggle Highlighter On/Off",
            "Open Keybind Settings"
    };

    private ButtonWidget[] bindButtons;

    public KeybindConfigScreen() {
        super(Text.literal("Keybind Settings"));
    }

    @Override
    protected void init() {
        panelX = (width  - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;

        KeybindConfig cfg = KeybindConfig.get();
        bindButtons = new ButtonWidget[ACTION_KEYS.length];

        for (int i = 0; i < ACTION_KEYS.length; i++) {
            final int idx = i;
            final String key = ACTION_KEYS[i];

            bindButtons[i] = ButtonWidget.builder(
                    Text.literal(keyName(cfg.getKey(key))),
                    btn -> startListening(idx))
                    .dimensions(panelX + PANEL_W - 130, panelY + 60 + i * 44, 118, 20)
                    .build();
            addDrawableChild(bindButtons[i]);
        }

        // Reset to defaults
        ButtonWidget resetBtn = ButtonWidget.builder(
                Text.literal("§eReset Defaults"),
                btn -> {
                    KeybindConfig.get().resetDefaults();
                    KeybindConfig.get().save();
                    listeningIndex = -1;
                    rebuildWidgets();
                })
                .dimensions(panelX + 8, panelY + PANEL_H - 28, 120, 18)
                .build();
        addDrawableChild(resetBtn);

        // Back button
        ButtonWidget backBtn = ButtonWidget.builder(
                Text.literal("§fBack"),
                btn -> {
                    MinecraftClient.getInstance().setScreen(new HighlighterScreen());
                })
                .dimensions(panelX + PANEL_W - 70, panelY + PANEL_H - 28, 62, 18)
                .build();
        addDrawableChild(backBtn);
    }

    private void startListening(int idx) {
        listeningIndex = idx;
        bindButtons[idx].setMessage(Text.literal("§e> Press a key..."));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningIndex >= 0) {
            // ESC cancels
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningIndex = -1;
                rebuildWidgets();
                return true;
            }
            String actionKey = ACTION_KEYS[listeningIndex];
            KeybindConfig.get().setKey(actionKey, keyCode);
            KeybindConfig.get().save();
            listeningIndex = -1;
            rebuildWidgets();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dark backdrop
        ctx.fillGradient(0, 0, width, height, 0xAA000000, 0xCC000000);

        // Panel
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFF1A1A2E);
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFFFFAA00);
        ctx.drawBorder(panelX, panelY, PANEL_W, PANEL_H, 0xFFFFAA00);

        // Title
        ctx.drawText(textRenderer, "§6§lKeybind §fSettings",
                panelX + 10, panelY + 10, 0xFFFFFF, true);
        ctx.drawText(textRenderer, "§7Click a button, then press the desired key. ESC to cancel.",
                panelX + 10, panelY + 24, 0xAAAAAA, false);

        KeybindConfig cfg = KeybindConfig.get();

        for (int i = 0; i < ACTION_KEYS.length; i++) {
            int rowY = panelY + 58 + i * 44;
            int rowBg = (i % 2 == 0) ? 0xFF16213E : 0xFF0F3460;
            ctx.fill(panelX + 8, rowY - 4, panelX + PANEL_W - 8, rowY + 24, rowBg);

            // Action label
            ctx.drawText(textRenderer, ACTION_LABELS[i],
                    panelX + 16, rowY + 4, 0xFFEEEEEE, false);

            // Current key
            boolean listening = listeningIndex == i;
            if (!listening) {
                String kn = keyName(cfg.getKey(ACTION_KEYS[i]));
                if (bindButtons[i] != null)
                    bindButtons[i].setMessage(Text.literal("§b" + kn));
            }
        }

        // Hint
        ctx.drawText(textRenderer, "§7Opens with: §eRight Shift",
                panelX + 10, panelY + PANEL_H - 42, 0xAAAAAA, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static String keyName(int glfwKey) {
        return switch (glfwKey) {
            case GLFW.GLFW_KEY_UNKNOWN    -> "NONE";
            case GLFW.GLFW_KEY_SPACE      -> "Space";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA      -> ",";
            case GLFW.GLFW_KEY_MINUS      -> "-";
            case GLFW.GLFW_KEY_PERIOD     -> ".";
            case GLFW.GLFW_KEY_SLASH      -> "/";
            case GLFW.GLFW_KEY_SEMICOLON  -> ";";
            case GLFW.GLFW_KEY_EQUAL      -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET  -> "[";
            case GLFW.GLFW_KEY_BACKSLASH  -> "\\";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_GRAVE_ACCENT  -> "`";
            case GLFW.GLFW_KEY_ESCAPE     -> "ESC";
            case GLFW.GLFW_KEY_ENTER      -> "Enter";
            case GLFW.GLFW_KEY_TAB        -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE  -> "Backspace";
            case GLFW.GLFW_KEY_INSERT     -> "Insert";
            case GLFW.GLFW_KEY_DELETE     -> "Delete";
            case GLFW.GLFW_KEY_RIGHT      -> "→";
            case GLFW.GLFW_KEY_LEFT       -> "←";
            case GLFW.GLFW_KEY_DOWN       -> "↓";
            case GLFW.GLFW_KEY_UP         -> "↑";
            case GLFW.GLFW_KEY_PAGE_UP    -> "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN  -> "PgDn";
            case GLFW.GLFW_KEY_HOME       -> "Home";
            case GLFW.GLFW_KEY_END        -> "End";
            case GLFW.GLFW_KEY_CAPS_LOCK  -> "CapsLock";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT   -> "LAlt";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_RIGHT_ALT  -> "RAlt";
            case GLFW.GLFW_KEY_F1  -> "F1";
            case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";
            case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";
            case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";
            case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            default -> {
                // Letter / digit keys
                String name = GLFW.glfwGetKeyName(glfwKey, 0);
                yield name != null ? name.toUpperCase() : "Key " + glfwKey;
            }
        };
    }
}
