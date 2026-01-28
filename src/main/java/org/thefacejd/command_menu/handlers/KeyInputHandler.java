package org.thefacejd.command_menu.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.thefacejd.command_menu.screens.CommandMenuScreen;

import org.thefacejd.command_menu.Command_menu;
import org.thefacejd.command_menu.screens.SettingsMenuScreen;

public class KeyInputHandler {
    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key." + Command_menu.MOD_ID + ".open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            Command_menu.MOD_CATEGORY
    );

    public static final KeyMapping OPEN_SETTINGS_MENU = new KeyMapping(
            "key." + Command_menu.MOD_ID + ".open_settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_BACKSLASH,
            Command_menu.MOD_CATEGORY
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_MENU);
        KeyBindingHelper.registerKeyBinding(OPEN_SETTINGS_MENU);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CommandMenuScreen());
                }
            }
            while (OPEN_SETTINGS_MENU.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new SettingsMenuScreen());
                }
            }
        });
    }
}