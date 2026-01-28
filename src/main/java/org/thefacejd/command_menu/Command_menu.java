package org.thefacejd.command_menu;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thefacejd.command_menu.config.ConfigManager;
import org.thefacejd.command_menu.handlers.KeyInputHandler;

public class Command_menu implements ClientModInitializer {
    public static final String MOD_ID = "command_menu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int MAX_COMMANDS = 36;
    public static final int MAX_COMMAND_NAME_LENGTH = 22;

    public record MenuItem(String name, String icon, String command) {
    }

    public static KeyMapping.Category MOD_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("category", MOD_ID));

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        LOGGER.info(MOD_ID + " initialized!");
        KeyInputHandler.register();
    }
}
