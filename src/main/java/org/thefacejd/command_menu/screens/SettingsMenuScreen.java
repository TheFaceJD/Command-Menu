package org.thefacejd.command_menu.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.thefacejd.command_menu.Command_menu;
import org.thefacejd.command_menu.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class SettingsMenuScreen extends Screen {

    private static final int PANEL_WIDTH_DEFAULT = 640;
    private static final int PANEL_HEIGHT_DEFAULT = 355;

    private static final int HEADER_HEIGHT = 24;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 4;
    private static final int TEX_PAD_X = 25;
    private static final int TEX_PAD_Y = 20;
    private static final int FOOTER_HEIGHT = 36;
    private static final int HEADER_AREA = 34;
    private static final int PAGE_BUTTON_SIZE = 14;
    private static final int PAGE_BUTTON_GAP = 0;

    private static final Identifier PANEL_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    Command_menu.MOD_ID,
                    "textures/gui/command_menu_panel.png"
            );

    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;

    private int nameX;
    private int iconX;
    private int cmdX;

    private final List<RowWidgets> rows = new ArrayList<>();

    private record RowWidgets(
            EditBox nameField,
            EditBox iconField,
            MultiLineEditBox commandField,
            Button upButton,
            Button downButton,
            Button deleteButton
    ) {
    }

    private static final int BASE_PAGE_SIZE = 8;
    private int pageSize;
    private int maxPages;
    private int currentPage = 0;

    private static final Pattern VALID_ID =
            Pattern.compile("^[a-z0-9_.-]+(:[a-z0-9_/.-]+)?$");

    public SettingsMenuScreen() {
        super(Component.translatable("command_menu.screen.settings.title"));
    }

    @Override
    protected void init() {
        super.init();

        clearWidgets();
        rows.clear();

        int margin = Math.max(16, Math.min(64, width / 20));
        panelWidth = Math.min(PANEL_WIDTH_DEFAULT, Math.max(200, width - margin));
        panelHeight = Math.min(PANEL_HEIGHT_DEFAULT, Math.max(120, height - margin));

        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        int innerX = panelX + TEX_PAD_X;
        int innerY = panelY + TEX_PAD_Y;
        int innerW = panelWidth - TEX_PAD_X * 2;
        int innerH = panelHeight - TEX_PAD_Y * 2;

        int btnY = innerY + innerH - FOOTER_HEIGHT + 8;
        int pageButtonsY = innerY + innerH - FOOTER_HEIGHT - PAGE_BUTTON_SIZE - 6;

        addRenderableWidget(
                Button.builder(Component.translatable("command_menu.button.save"), b -> onSave())
                        .bounds(innerX + innerW - 90, btnY, 80, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.translatable("command_menu.button.back"),
                                b -> minecraft.setScreen(new CommandMenuScreen()))
                        .bounds(innerX, btnY, 80, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.translatable("command_menu.button.add"), b -> addNewCommand())
                        .bounds(innerX + 90, btnY, 80, 20)
                        .build()
        );

        int tableTop = innerY + HEADER_AREA;
        int tableBottom = innerY + innerH - FOOTER_HEIGHT - PAGE_BUTTON_SIZE - 6;
        int availableHeight = tableBottom - tableTop;

        pageSize = Math.max(1, availableHeight / (ROW_HEIGHT + ROW_GAP));
        pageSize = Math.min(pageSize, BASE_PAGE_SIZE);
        int realTotal = ConfigManager.config.menuItems.size();
        int realPages = Math.max(1, (realTotal + pageSize - 1) / pageSize);
        maxPages = realPages;
        currentPage = Math.min(currentPage, maxPages - 1);

        if (currentPage >= realPages) {
            currentPage = realPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int totalAll = Math.min(ConfigManager.config.menuItems.size(), maxPages * pageSize);
        int pageStart = currentPage * pageSize;
        int totalOnPage = Math.max(0, Math.min(pageSize, totalAll - pageStart));
        int visibleRows = Math.min(pageSize, totalOnPage);

        final int upDownColsW = 66;
        int nameWidth = Math.max(80, Math.min(160, innerW / 6));
        int iconWidth = Math.max(80, Math.min(160, innerW / 5));

        nameX = innerX + upDownColsW + 8;
        iconX = nameX + nameWidth + 8;
        cmdX = iconX + iconWidth + 8;
        int cmdWidth = innerX + innerW - cmdX;

        for (int i = 0; i < visibleRows; i++) {
            int globalIndex = pageStart + i;
            Command_menu.MenuItem item = ConfigManager.config.menuItems.get(globalIndex);

            int rowY = tableTop + i * (ROW_HEIGHT + ROW_GAP);
            int idx = i;

            Button up = Button.builder(
                            Component.literal("▲"),
                            b -> moveCommand(idx, -1))
                    .bounds(innerX, rowY, 20, ROW_HEIGHT)
                    .build();
            addRenderableWidget(up);

            Button down = Button.builder(
                            Component.literal("▼"),
                            b -> moveCommand(idx, +1))
                    .bounds(innerX + 22, rowY, 20, ROW_HEIGHT)
                    .build();
            addRenderableWidget(down);

            Button delete = Button.builder(
                    Component.literal("✖"),
                    b -> confirmDelete(globalIndex)
            ).bounds(innerX + 44, rowY, 20, ROW_HEIGHT).build();

            addRenderableWidget(delete);

            EditBox nameField = new EditBox(font, nameX, rowY, nameWidth, ROW_HEIGHT, Component.empty());
            nameField.setValue(item.name());
            nameField.setMaxLength(Command_menu.MAX_COMMAND_NAME_LENGTH);
            nameField.setResponder(s -> {
                if (s.length() > Command_menu.MAX_COMMAND_NAME_LENGTH) {
                    nameField.setTextColor(0xFFFF5555);
                } else {
                    nameField.setTextColor(0xFFFFFFFF);
                }
            });

            addRenderableWidget(nameField);

            EditBox iconField = new EditBox(font, iconX, rowY, iconWidth, ROW_HEIGHT, Component.empty());
            iconField.setValue(item.icon());
            iconField.setResponder(s -> {
                boolean invalid = true;
                if (s.isEmpty()) return;

                if (!VALID_ID.matcher(s).matches()) {
                    iconField.setTextColor(0xFFFF5555);
                    return;
                }

                String[] parts = s.split(":", 2);
                Identifier itemId;
                try {
                    if (parts.length == 1) {
                        itemId = Identifier.tryBuild("minecraft", parts[0]);
                    } else {
                        itemId = Identifier.tryBuild(parts[0], parts[1]);
                    }
                } catch (Exception e) {
                    iconField.setTextColor(0xFFFF5555);
                    return;
                }

                assert itemId != null;
                Optional<Holder.Reference<@NotNull Item>> itemFromRegistry = BuiltInRegistries.ITEM.get(itemId);
                if (itemFromRegistry.isPresent() && itemFromRegistry.get().value() != Items.AIR) invalid = false;

                iconField.setTextColor(invalid ? 0xFFFF5555 : 0xFFFFFFFF);
            });

            addRenderableWidget(iconField);

            MultiLineEditBox.Builder cmdFieldBuilder = MultiLineEditBox.builder();
            cmdFieldBuilder.setPlaceholder(Component.translatable("command_menu.field.command.placeholder"));
            MultiLineEditBox cmdField = cmdFieldBuilder.build(font, cmdX, rowY, Component.empty());
            cmdField.setX(cmdX);
            cmdField.setY(rowY);
            cmdField.setWidth(Math.max(50, cmdWidth));
            cmdField.setHeight(ROW_HEIGHT);
            cmdField.setValue(item.command());

            addRenderableWidget(cmdField);

            rows.add(new RowWidgets(nameField, iconField, cmdField, up, down, delete));
        }

        addPageButtons(panelX, pageButtonsY);
    }

    @Override
    public void render(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE,
                panelX, panelY, 0, 0,
                panelWidth, panelHeight, panelWidth, panelHeight
        );

        int titleWidth = font.width(title);
        int titleX = panelX + (panelWidth - titleWidth) / 2;
        int titleY = panelY + TEX_PAD_Y;
        guiGraphics.drawString(font, title, titleX, titleY, 0xFFFFFFFF, false);

        int labelY = panelY + HEADER_HEIGHT + 15;

        Component nameLabel = Component.translatable("command_menu.field.name.label");
        Component iconLabel = Component.translatable("command_menu.field.icon.label");
        Component cmdLabel = Component.translatable("command_menu.field.command.label");

        guiGraphics.drawString(font, nameLabel, nameX, labelY, 0xFFFFFFDD, false);
        guiGraphics.drawString(font, cmdLabel, cmdX, labelY, 0xFFFFFFDD, false);
        guiGraphics.drawString(font, iconLabel, iconX, labelY, 0xFFFFFFDD, false);

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    private void moveCommand(int localIndex, int delta) {
        int global = currentPage * pageSize + localIndex;
        int target = global + delta;

        if (target < 0 || target >= ConfigManager.config.menuItems.size()) return;

        for (int i = 0; i < rows.size(); i++) {
            int idxGlobal = currentPage * pageSize + i;
            RowWidgets rw = rows.get(i);

            String validatedIcon = validateIcon(rw.iconField.getValue(), idxGlobal);
            String finalIcon = (validatedIcon != null) ? validatedIcon : ConfigManager.config.menuItems.get(idxGlobal).icon();
            ConfigManager.config.menuItems.set(idxGlobal,
                    new Command_menu.MenuItem(
                            rw.nameField.getValue(),
                            finalIcon,
                            rw.commandField.getValue()
                    )
            );
        }

        Collections.swap(ConfigManager.config.menuItems, global, target);
        currentPage = target / pageSize;
        init();
    }

    private void onSave() {
        List<Command_menu.MenuItem> original = new ArrayList<>(ConfigManager.config.menuItems);
        int totalAll = Math.min(original.size(), maxPages * pageSize);
        int pageStart = currentPage * pageSize;

        if (original.size() > Command_menu.MAX_COMMANDS) {
            original = original.subList(0, Command_menu.MAX_COMMANDS);
        }

        for (int i = 0; i < rows.size(); i++) {
            int globalIndex = pageStart + i;
            if (globalIndex < 0 || globalIndex >= totalAll) continue;

            RowWidgets rw = rows.get(i);
            String name = rw.nameField.getValue().trim();
            String icon = rw.iconField.getValue().trim();
            String cmd = rw.commandField.getValue().trim();

            String finalName = validateName(name, globalIndex);
            String finalIcon = validateIcon(icon, globalIndex);

            original.set(globalIndex, new Command_menu.MenuItem(finalName, finalIcon, cmd));
        }

        ConfigManager.config.menuItems = new ArrayList<>(original);
        ConfigManager.save();
        minecraft.setScreen(new CommandMenuScreen());
    }

    private String validateName(String name, int index) {
        if (name == null || name.isEmpty()) return ConfigManager.config.menuItems.get(index).name();
        if (name.length() > 16) return name.substring(0, 16);
        return name;
    }

    private String validateIcon(String icon, int index) {
        if (icon.isEmpty()) return null;
        String fullId = icon.contains(":") ? icon : "minecraft:" + icon;
        String[] parts = fullId.split(":", 2);

        if (parts.length != 2) return null;
        if (!parts[0].equals("minecraft")) return null;

        try {
            Identifier id = Identifier.tryBuild(parts[0], parts[1]);
            assert id != null;
            Optional<Holder.Reference<@NotNull Item>> item = BuiltInRegistries.ITEM.get(id);

            if (item.isPresent() && item.get().value() != Items.AIR) return fullId;

        } catch (Exception ignored) {
        }

        return ConfigManager.config.menuItems.get(index).icon();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addPageButtons(int panelXLocal, int pageButtonsY) {
        int total = Math.min(ConfigManager.config.menuItems.size(), maxPages * pageSize);
        int totalPages = (total + pageSize - 1) / pageSize;
        if (totalPages == 0) return;

        int buttonsWidth = totalPages * PAGE_BUTTON_SIZE;

        int baseX = panelXLocal + panelWidth - TEX_PAD_X - buttonsWidth;


        for (int i = 0; i < totalPages; i++) {
            int x = baseX + i * (PAGE_BUTTON_SIZE + PAGE_BUTTON_GAP);
            boolean hasData = true;
            boolean active = i != currentPage;
            int pageIndex = i;

            Button btn = Button.builder(
                            Component.literal(String.valueOf(i + 1)),
                            b -> {
                                currentPage = pageIndex;
                                init();
                            }
                    )
                    .bounds(x, pageButtonsY, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE)
                    .build();

            btn.active = active;
            if (!hasData) {
                btn.visible = false;
            }
            addRenderableWidget(btn);
        }
    }

    private void addNewCommand() {
        if (ConfigManager.config.menuItems.size() >= Command_menu.MAX_COMMANDS) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.translatable("command_menu.error.commandLimit", Command_menu.MAX_COMMANDS)
                                .withStyle(ChatFormatting.RED),
                        false
                );
            }
            return;
        }

        List<Command_menu.MenuItem> list = new ArrayList<>(ConfigManager.config.menuItems);
        list.add(new Command_menu.MenuItem("New Command", "minecraft:stone", ""));

        ConfigManager.config.menuItems = new ArrayList<>(list);

        currentPage = (list.size() - 1) / pageSize;
        init();
    }

    private void confirmDelete(int index) {
        if (index < 0 || index >= ConfigManager.config.menuItems.size()) return;
        Command_menu.MenuItem item = ConfigManager.config.menuItems.get(index);

        String command = item.command();
        if (command.length() > 64) command = item.command().substring(0, 64) + "...";

        Component translatableLabel = command.isEmpty() ?
                Component.translatable("command_menu.screen.deleteCommand.label.withoutCommand", item.name()) :
                Component.translatable("command_menu.screen.deleteCommand.label", item.name(), command);


        minecraft.setScreen(new ConfirmScreen(
                result -> {
                    if (result) {
                        deleteCommand(index);
                    } else {
                        minecraft.setScreen(this);
                    }
                },
                Component.translatable("command_menu.screen.deleteCommand.title"),
                translatableLabel
        ));
    }

    private void deleteCommand(int index) {
        List<Command_menu.MenuItem> list = new ArrayList<>(ConfigManager.config.menuItems);

        if (index < 0 || index >= list.size()) return;
        list.remove(index);
        ConfigManager.config.menuItems = new ArrayList<>(list);

        int maxPage = Math.max(0, (list.size() - 1) / pageSize);
        currentPage = Math.min(currentPage, maxPage);

        minecraft.setScreen(this);
        init();
    }
}
