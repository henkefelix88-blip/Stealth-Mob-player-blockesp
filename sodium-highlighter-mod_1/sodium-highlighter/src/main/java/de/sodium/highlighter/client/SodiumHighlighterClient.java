package de.sodium.highlighter.client;

import de.sodium.highlighter.config.HighlightConfig;
import de.sodium.highlighter.config.KeybindConfig;
import de.sodium.highlighter.gui.HighlighterScreen;
import de.sodium.highlighter.gui.KeybindConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SodiumHighlighterClient implements ClientModInitializer {

    private boolean prevOpenGui        = false;
    private boolean prevToggle         = false;
    private boolean prevOpenKeybindGui = false;

    @Override
    public void onInitializeClient() {
        HighlightConfig.load();
        KeybindConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) {
                prevOpenGui = false;
                prevToggle = false;
                prevOpenKeybindGui = false;
                return;
            }

            long window = client.getWindow().getHandle();
            KeybindConfig kb = KeybindConfig.get();

            boolean curOpenGui        = isKeyDown(window, kb.getKey("open_gui"));
            boolean curToggle         = isKeyDown(window, kb.getKey("toggle"));
            boolean curOpenKeybindGui = isKeyDown(window, kb.getKey("open_keybind_gui"));

            if (curOpenGui && !prevOpenGui) {
                client.setScreen(new HighlighterScreen());
            }
            if (curToggle && !prevToggle) {
                HighlightConfig cfg = HighlightConfig.get();
                cfg.globalEnabled = !cfg.globalEnabled;
                cfg.save();
                client.player.sendMessage(
                    Text.literal("§bSodium Highlighter §r" +
                        (cfg.globalEnabled ? "§aEnabled" : "§cDisabled")), true);
            }
            if (curOpenKeybindGui && !prevOpenKeybindGui) {
                client.setScreen(new KeybindConfigScreen());
            }

            prevOpenGui        = curOpenGui;
            prevToggle         = curToggle;
            prevOpenKeybindGui = curOpenKeybindGui;
        });
    }

    private static boolean isKeyDown(long window, int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        return GLFW.glfwGetKey(window, glfwKey) == GLFW.GLFW_PRESS;
    }
}
