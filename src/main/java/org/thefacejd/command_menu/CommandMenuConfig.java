package org.thefacejd.command_menu;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandMenuConfig {
    public ArrayList<Command_menu.MenuItem> menuItems = new ArrayList<>();

    public static CommandMenuConfig createDefault() {
        return new CommandMenuConfig();
    }

    public void validate() {
        if (menuItems == null) {
            menuItems = new ArrayList<>();
            return;
        }

        if (menuItems.size() > Command_menu.MAX_COMMANDS)
            menuItems = new ArrayList<>(menuItems.subList(0, Command_menu.MAX_COMMANDS));
        List<Command_menu.MenuItem> validated = new ArrayList<>(menuItems.size());

        for (Command_menu.MenuItem item : menuItems) {
            if (item == null) continue;

            String validName = item.name();
            if (validName == null) validName = "Command";
            if (validName.length() > Command_menu.MAX_COMMAND_NAME_LENGTH)
                validName = validName.substring(0, Command_menu.MAX_COMMAND_NAME_LENGTH);

            String validIcon = item.icon();
            if (validIcon == null || validIcon.isEmpty()) {
                validIcon = "minecraft:stone";
            } else {
                ResourceLocation itemId = ResourceLocation.tryParse(validIcon);
                if (itemId == null) {
                    validIcon = "minecraft:stone";
                } else {
                    Optional<Holder.Reference<Item>> registryItem = BuiltInRegistries.ITEM.get(itemId);
                    if (registryItem.isPresent() && registryItem.get().value() != Items.AIR) {
                        validIcon = itemId.toString();
                        Command_menu.LOGGER.info(registryItem.toString());
                    } else {
                        validIcon = "minecraft:stone";
                    }
                }
            }

            String validCommand = item.command();

            validated.add(new Command_menu.MenuItem(validName, validIcon, validCommand));
        }

        menuItems = new ArrayList<>(validated);
    }
}