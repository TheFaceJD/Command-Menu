package org.thefacejd.command_menu.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.thefacejd.command_menu.Command_menu;
import org.thefacejd.command_menu.config.ConfigManager;

import static org.thefacejd.command_menu.handlers.KeyInputHandler.OPEN_MENU;

public class CommandMenuScreen extends Screen {

    private static final int PANEL_WIDTH = 180;
    private static final int PANEL_HEIGHT = 154;

    private static final int HEADER_HEIGHT = 20;

    public static final int SETTINGS_BUTTON_SIZE = 14;
    public static final int SETTINGS_ICON_SIZE = 10;

    private static final int BUTTON_SIZE = 28;
    public static final int BUTTON_ICON_SIZE = 16;
    private static final int BUTTON_SPACING = 9;
    private static final int BUTTONS_PER_ROW = 4;

    private static final int FIRST_BUTTON_Y = 26;

    private static final ResourceLocation PANEL_TEXTURE =
            ResourceLocation.tryBuild(Command_menu.MOD_ID, "textures/gui/command_menu_panel.png");

    private static final ResourceLocation GEAR_TEXTURE =
            ResourceLocation.tryBuild(Command_menu.MOD_ID, "textures/gui/command_menu_gear_icon.png");

    private int currentPage = 0;

    private static final int PAGE_SIZE = 12;
    private static final int MAX_PAGES = Command_menu.MAX_COMMANDS / PAGE_SIZE;

    private static final int PAGE_BUTTON_SIZE = 14;
    private static final int TEX_PAD_X = 12;
    private static final int TEX_PAD_Y = 8;

    public CommandMenuScreen() {
        super(Component.literal("Command Menu"));
    }

    @Override
    protected void init() {

        super.init();
        clearWidgets();

        int centerX = width / 2;
        int centerY = height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        int titleY = panelY + (HEADER_HEIGHT - font.lineHeight) / 2;
        int settingsBtnX = panelX + PANEL_WIDTH - SETTINGS_BUTTON_SIZE - TEX_PAD_X;
        int settingsBtnY = titleY + (font.lineHeight - SETTINGS_BUTTON_SIZE) / 2 + TEX_PAD_Y / 2;

        addRenderableWidget(
                Button.builder(Component.empty(),
                                b -> {
                                    assert minecraft != null;
                                    minecraft.setScreen(new SettingsMenuScreen());
                                })
                        .bounds(
                                settingsBtnX,
                                settingsBtnY,
                                SETTINGS_BUTTON_SIZE,
                                SETTINGS_BUTTON_SIZE
                        )
                        .tooltip(Tooltip.create(
                                Component.translatable("command_menu.tooltip.settings")))
                        .build()
        );

        int total = Math.min(ConfigManager.config.menuItems.size(), MAX_PAGES * PAGE_SIZE);
        int pageStart = currentPage * PAGE_SIZE;
        int pageEnd = Math.min(pageStart + PAGE_SIZE, total);
        int count = Math.max(0, pageEnd - pageStart);
        int rows = (count + BUTTONS_PER_ROW - 1) / BUTTONS_PER_ROW;

        for (int row = 0; row < rows; row++) {
            int startIndex = pageStart + row * BUTTONS_PER_ROW;
            int rowCount = Math.min(BUTTONS_PER_ROW, count - row * BUTTONS_PER_ROW);
            int rowWidth = rowCount * BUTTON_SIZE + (rowCount - 1) * BUTTON_SPACING;
            int rowStartX = panelX + (PANEL_WIDTH - rowWidth) / 2;
            int buttonY = panelY + FIRST_BUTTON_Y + row * (BUTTON_SIZE + BUTTON_SPACING);

            for (int col = 0; col < rowCount; col++) {

                int index = startIndex + col;
                Command_menu.MenuItem item = ConfigManager.config.menuItems.get(index);
                int buttonX = rowStartX + col * (BUTTON_SIZE + BUTTON_SPACING);

                Component name = Component.literal(item.name());
                Component command;

                if (item.command().isEmpty()) {
                    command = Component.translatable("command_menu.error.commandNotIndicated").withStyle(ChatFormatting.RED);
                } else {
                    String commandString = "/" + item.command();
                    if (commandString.length() > 128) commandString = item.command().substring(0, 128) + "...";
                    command = Component.literal(commandString).withStyle(ChatFormatting.GRAY);
                }

                Tooltip tooltip = Tooltip.create(Component.literal("").append(name).append("\n").append(command));

                addRenderableWidget(
                        Button.builder(Component.empty(),
                                        b -> {
                                            assert minecraft != null;
                                            if (minecraft.player != null) {
                                                minecraft.player.connection.sendCommand(item.command());
                                            }
                                        }
                                )
                                .bounds(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE)
                                .tooltip(tooltip)
                                .build()
                );
            }
        }

        addPageButtons(panelX, panelY);
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        guiGraphics.blit(PANEL_TEXTURE, panelX, panelY, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);

        int titleWidth = this.font.width(this.title);
        int titleX = panelX + (PANEL_WIDTH - titleWidth) / 2;
        int titleY = panelY + (HEADER_HEIGHT - font.lineHeight) / 2 + 6;
        guiGraphics.drawString(this.font, this.title, titleX, titleY, 0xFFFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, delta);

        int total = Math.min(
                ConfigManager.config.menuItems.size(),
                MAX_PAGES * PAGE_SIZE
        );

        int pageStart = currentPage * PAGE_SIZE;
        int pageEnd = Math.min(pageStart + PAGE_SIZE, total);
        int count = Math.max(0, pageEnd - pageStart);

        int rows = (count + BUTTONS_PER_ROW - 1) / BUTTONS_PER_ROW;

        for (int row = 0; row < rows; row++) {

            int startIndex = pageStart + row * BUTTONS_PER_ROW;
            int rowCount = Math.min(BUTTONS_PER_ROW, count - row * BUTTONS_PER_ROW);
            int rowWidth = rowCount * BUTTON_SIZE + (rowCount - 1) * BUTTON_SPACING;
            int rowStartX = panelX + (PANEL_WIDTH - rowWidth) / 2;
            int buttonY = panelY + FIRST_BUTTON_Y + row * (BUTTON_SIZE + BUTTON_SPACING);

            for (int col = 0; col < rowCount; col++) {
                int index = startIndex + col;
                Command_menu.MenuItem menuItem = ConfigManager.config.menuItems.get(index);
                if (menuItem.icon().isEmpty()) continue;
                String idString = menuItem.icon();
                String[] parts = idString.split(":", 2);
                if (parts.length != 2) continue;
                ResourceLocation itemId;

                try {
                    itemId = ResourceLocation.tryBuild(parts[0], parts[1]);
                } catch (Exception e) {
                    continue;
                }

                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item == Items.AIR) continue;
                ItemStack stack = new ItemStack(item);

                int buttonX = rowStartX + col * (BUTTON_SIZE + BUTTON_SPACING);
                int iconX = buttonX + (BUTTON_SIZE - BUTTON_ICON_SIZE) / 2;
                int iconY = buttonY + (BUTTON_SIZE - BUTTON_ICON_SIZE) / 2;
                guiGraphics.renderItem(stack, iconX, iconY);
            }
        }

        int settingsBtnX = panelX + PANEL_WIDTH - SETTINGS_BUTTON_SIZE - TEX_PAD_X;
        int settingsBtnY = titleY + (font.lineHeight - SETTINGS_BUTTON_SIZE) / 2;
        int gearX = settingsBtnX + (SETTINGS_BUTTON_SIZE - SETTINGS_ICON_SIZE) / 2;
        int gearY = settingsBtnY + (SETTINGS_BUTTON_SIZE - SETTINGS_ICON_SIZE) / 2 - 2;

        guiGraphics.blit(GEAR_TEXTURE, gearX, gearY, 0, 0,
                SETTINGS_ICON_SIZE, SETTINGS_ICON_SIZE, SETTINGS_ICON_SIZE, SETTINGS_ICON_SIZE);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (OPEN_MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addPageButtons(int panelX, int panelY) {
        int total = Math.min(ConfigManager.config.menuItems.size(), MAX_PAGES * PAGE_SIZE);
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int baseX = panelX + PANEL_WIDTH - PAGE_BUTTON_SIZE * totalPages - TEX_PAD_X;
        int y = panelY + PANEL_HEIGHT - PAGE_BUTTON_SIZE - TEX_PAD_Y;

        for (int i = 0; i < MAX_PAGES; i++) {

            int x = baseX + i * PAGE_BUTTON_SIZE;
            boolean hasData = i < totalPages;
            boolean active = i != currentPage && hasData;
            int pageIndex = i;

            Button btn = Button.builder(
                            Component.literal(String.valueOf(i + 1)),
                            b -> {
                                currentPage = pageIndex;
                                init();
                            }
                    )
                    .bounds(x, y, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE)
                    .build();

            btn.active = active;
            if (!hasData) {
                btn.visible = false;
            }
            addRenderableWidget(btn);
        }
    }
}