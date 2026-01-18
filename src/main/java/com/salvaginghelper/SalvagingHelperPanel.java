package com.salvaginghelper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import com.salvaginghelper.LootManager.SalvageType;
import com.salvaginghelper.LootManager.LootOption;
import com.salvaginghelper.ActionHandler.SalvageMode;


public class SalvagingHelperPanel extends PluginPanel {

    //region Variable Declarations

    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    private final ColorPickerManager colorPickerManager;
    public HashMap<SalvageType, JPanel> salvageCategoryMap = new HashMap<>();
    public HashMap<SalvageType, JPanel> salvageLootItemsMap = new HashMap<>();
    public HashMap<JPanel, SalvageType> toSalvageCategory = new HashMap<>();
    public Multimap<Integer, JComboBox<LootOption>> itemToComboBox = ArrayListMultimap.create();
    public HashMap <JButton, LootContainer> toLootContainer = new HashMap<>();
    public HashMap <LootContainer, JButton> toContainerButton = new HashMap<>();
    private HashMap<LootOption, JButton> lootColorButtonMap = new HashMap<>();
    private HashMap<String, JComponent> configWidgetMap = new HashMap<>();
    private HashMap<String, Class<?>> configTypeMap = new HashMap<>();

    @Getter
    private JComboBox<SalvageMode> salvageModeComboBox;

    // https://fonts.google.com/icons (Apache 2.0)
    private final ImageIcon ICON_DROPDOWN_UNCLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px.png"));
    private final ImageIcon ICON_DROPDOWN_UNCLICKED_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px_gray.png"));
    private final ImageIcon ICON_DROPDOWN_UNCLICKED_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px_darkgray.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px_gray.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px_darkgray.png"));
    private final ImageIcon ICON_EXPAND_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_20px.png"));
    private final ImageIcon ICON_COLLAPSE_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_collapse_20px.png"));
    private final ImageIcon ICON_RESET = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_restart_20px.png"));
    private final ImageIcon ICON_IMPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_download_20px.png"));
    private final ImageIcon ICON_EXPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_upload_20px.png"));
    private final ImageIcon ICON_PLUGIN = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_settings_ethernet_20px.png"));
    private final ImageIcon ICON_HOOK = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_phishing_20px.png"));
    private final ImageIcon ICON_SWAP = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_swap_horiz_20px.png"));
    private final ImageIcon ICON_BUG = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_bug_report_20px.png"));
    private final ImageIcon ICON_BUG_INACTIVE = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_bug_report_gray_20px.png"));
    private final ImageIcon ICON_STRATEGY = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_flowchart_20px.png"));
    private final ImageIcon ICON_COLOR = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_palette_20px.png"));
    private final ImageIcon ICON_SETTINGS = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_settings_16px.png"));
    private final ImageIcon ICON_SETTINGS_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_settings_gray_16px.png")); // Color 999999
    private final ImageIcon ICON_SETTINGS_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_settings_darkgray_16px.png")); // Color #434343
    private final ImageIcon ICON_BACK = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_arrow_left_alt_20px.png"));
    private final ImageIcon ICON_BACK_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_arrow_left_alt_gray_20px.png"));
    private final ImageIcon ICON_BACK_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_arrow_left_alt_darkgray_20px.png"));
    private final ImageIcon ICON_TEST = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_play_arrow_20px.png"));
    private final ImageIcon ICON_TEST_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_play_arrow_dark_20px.png"));
    private final ImageIcon ICON_TEST_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_play_arrow_darker_20px.png"));
    private final ImageIcon ICON_UNSET = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_history_20px.png"));
    private final ImageIcon ICON_UNSET_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_history_gray_20px.png"));
    private final ImageIcon ICON_UNSET_PRESSED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_history_darkgray_20px.png"));


    private final int SIDEBAR_WIDTH = 249;
    private final int SIDEBAR_INCLUDING_SCROLL = 230;
    private final int SIDEBAR_MINUS_SCROLL = 223;
    private final int TOOLBAR_ICON_WIDTH = 26;
    private final int TAB_GROUP_PANEL_HEIGHT = 37;
    private final int CATEGORY_PANEL_X_PADDING = 4;
    private final int CATEGORY_PANEL_WIDTH = 223;
    private final int CATEGORY_PANEL_HEIGHT = 45;
    private final int ITEM_PANEL_HEIGHT = 26;
    private final int ITEM_PANEL_PADDING = 2;

    private final Color SALVAGE_PANEL_COLOR = new Color(46, 46, 46);
    private final Color CONTAINER_BUTTON_ENABLED = new Color(0, 179, 0);

    private final static Border doubleBorder = BorderFactory.createCompoundBorder(
            new LineBorder(ColorScheme.DARKER_GRAY_COLOR, 1),
            new EmptyBorder(2, 2, 2, 2));


    @Inject
    private final ItemManager itemManager;

    //@Inject
    private final ConfigManager configManager;

    @Inject
    private Client client;

    @Inject
    private final LootManager lootManager;

    //@Inject
    private final ClientThread clientThread;

    //@Inject
    //private ClientToolbar clientToolbar;

    private final JPanel tabGroupPanel;
    private final JPanel displayPanel;
    private final JPanel generalTabPanel;
    private final JPanel lootTabPanel;
    private final JPanel debugTabPanel;

    private final MaterialTabGroup tabGroup;
    private final MaterialTab generalTab;
    public final MaterialTab debugTab;
    private final MaterialTab extractorSettingsTab;
    private final MaterialTab idleSettingsTab;
    private final JPanel extractorSettingsPanel;
    private final JPanel idleSettingsPanel;
    //private final JPanel boostSettingsPanel;

    //endregion

    //region Constructor
    public SalvagingHelperPanel(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, Client client,
                                ItemManager itemManager, ConfigManager configManager, LootManager lootManager,
                                ClientThread clientThread) {
        super(true);

        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.itemManager = itemManager;
        this.configManager = configManager;
        this.lootManager = lootManager;
        this.clientThread = clientThread;
        this.colorPickerManager = new ColorPickerManager(configManager);

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JScrollPane scrollPane = getScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tabGroupPanel = new JPanel();
        displayPanel = new JPanel();
        generalTabPanel = buildGeneralPanel();
        lootTabPanel = buildLootPanel();
        debugTabPanel = buildDebugPanel();

        displayPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        displayPanel.setLayout(new BorderLayout(0, 0));

        tabGroup = new MaterialTabGroup(displayPanel);
        tabGroup.setLayout(new BorderLayout(0, 0));
        generalTab = new MaterialTab("General", tabGroup, generalTabPanel);
        MaterialTab lootTab = new MaterialTab("Loot", tabGroup, lootTabPanel);
        debugTab = new MaterialTab(ICON_BUG_INACTIVE, tabGroup, debugTabPanel);

        generalTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        generalTab.setFont(FontManager.getRunescapeBoldFont());
        lootTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        lootTab.setFont(FontManager.getRunescapeBoldFont());
        debugTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        debugTab.setFont(FontManager.getRunescapeBoldFont());

        // Material Tabs
        tabGroup.addTab(generalTab);
        tabGroup.addTab(lootTab);
        tabGroup.addTab(debugTab);

        // Don't show as navigation options, but allow linking
        extractorSettingsPanel = createExtractorSubpanel();
        extractorSettingsTab = new MaterialTab("Extractor Notifications", tabGroup, extractorSettingsPanel);
        tabGroup.addTab(extractorSettingsTab);

        idleSettingsPanel = createIdleSubpanel();
        idleSettingsTab = new MaterialTab("Idle Notifications", tabGroup, idleSettingsPanel);
        tabGroup.addTab(idleSettingsTab);

        tabGroupPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tabGroupPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        tabGroupPanel.setPreferredSize(new Dimension(SIDEBAR_MINUS_SCROLL, TAB_GROUP_PANEL_HEIGHT));
        tabGroupPanel.add(generalTab);
        tabGroupPanel.add(lootTab);
        tabGroupPanel.add(debugTab);
        debugTab.setVisible(config.debugModeEnabled());


        debugTab.setOnSelectEvent(() -> {
            debugTab.setIcon(ICON_BUG);
            return true;
        });
        generalTab.setOnSelectEvent(() -> {
            debugTab.setIcon(ICON_BUG_INACTIVE);
            return true;
        });
        lootTab.setOnSelectEvent(() -> {
            debugTab.setIcon(ICON_BUG_INACTIVE);
            return true;
        });

        container.add(tabGroupPanel, BorderLayout.NORTH);
        container.add(displayPanel, BorderLayout.CENTER);

        add(container);
        tabGroup.select(generalTab);
        collapseAllLoot();
    }
    //endregion

    //region Build: General Panel
    public JPanel buildGeneralPanel() {
        JPanel generalContainer = new JPanel();
        generalContainer.setLayout(new BoxLayout(generalContainer, BoxLayout.Y_AXIS));

        // General settings
        JPanel generalSettingsContainer = new JPanel();
        generalSettingsContainer.setBorder(doubleBorder);
        generalSettingsContainer.setLayout(new BoxLayout(generalSettingsContainer, BoxLayout.Y_AXIS));
        generalSettingsContainer.add(createSettingsCheckbox("drawShipwreckRadius", null));
        generalSettingsContainer.add(createSettingsCheckbox("drawHookLocation", null));
        generalSettingsContainer.add(createSettingsCheckbox("enableLootOverlays", null));
        generalSettingsContainer.add(createSettingsCheckbox("swapInvItemOptions", null));
        generalSettingsContainer.add(createSettingsCheckbox("idleAlerts", createSettingsLinkButton(4)));
        generalSettingsContainer.add(createSettingsCheckbox("extractorAlerts", createSettingsLinkButton(3)));
        generalSettingsContainer.add(createSettingsCheckbox("hideCrewmateOverhead", null));
        generalSettingsContainer.add(createSettingsCheckbox("hideOthersCrewmateOverhead", null));
        JPanel generalSettingsHeader = createSettingsHeader("Plugin modules", ICON_PLUGIN, generalSettingsContainer,
                true, null, null, null);

        JPanel salvageModeDropdownContainer = new JPanel();
        salvageModeDropdownContainer.setBorder(new EmptyBorder(1, 1, 1, 1));
        salvageModeDropdownContainer.setLayout(new BoxLayout(salvageModeDropdownContainer, BoxLayout.X_AXIS));

        // Salvaging mode
        DefaultComboBoxModel<SalvageMode> model = new DefaultComboBoxModel<>();
        model.addElement(SalvageMode.SALVAGE_AND_SORT);
        model.addElement(SalvageMode.SALVAGE_ONLY);
        model.addElement(SalvageMode.SORT_ONLY);
        JLabel salvageModeIcon = new JLabel(ICON_STRATEGY);
        salvageModeComboBox = new JComboBox<>(model);
        salvageModeComboBox.setSelectedItem(config.salvageMode());
        salvageModeComboBox.setMaximumSize(new Dimension(120, 26));
        salvageModeComboBox.addActionListener(event -> {
            SalvageMode selectedOption = (SalvageMode) salvageModeComboBox.getSelectedItem();
            if (selectedOption != config.salvageMode()) {
                plugin.setConfigByKey("salvageMode", selectedOption);
            }
        });
        salvageModeDropdownContainer.add(salvageModeIcon);
        salvageModeDropdownContainer.add(Box.createRigidArea(new Dimension(4, 1)));
        salvageModeDropdownContainer.add(salvageModeComboBox);
        configWidgetMap.put("salvageMode", salvageModeComboBox);
        configTypeMap.put("salvageMode", SalvageMode.class);

        JPanel salvagingModeContainer = new JPanel();
        salvagingModeContainer.setLayout(new BoxLayout(salvagingModeContainer, BoxLayout.Y_AXIS));
        salvagingModeContainer.setBorder(doubleBorder);
        salvagingModeContainer.add(salvageModeDropdownContainer);
        salvagingModeContainer.add(createSettingsCheckbox("dropAllSalvage", null));
        salvagingModeContainer.add(createSettingsCheckbox("minMaxHookUptime", null));
        salvagingModeContainer.add(createSettingsCheckbox("cargoBeforeSort", null));
        //salvagingModeContainer.add(createSettingsCheckbox("dockOnFull", null));
        JPanel salvagingModeHeader = createSettingsHeader("Salvaging strategy", ICON_HOOK, salvagingModeContainer,
                true, null, null, null);

        // Overrides
        JPanel overrideSettingsContainer = new JPanel();
        overrideSettingsContainer.setBorder(doubleBorder);
        overrideSettingsContainer.setLayout(new BoxLayout(overrideSettingsContainer, BoxLayout.Y_AXIS));
        overrideSettingsContainer.add(createSettingsCheckbox("hideGroundItems", null));
        overrideSettingsContainer.add(createSettingsCheckbox("hideCrewmateLeftClick", null));
        overrideSettingsContainer.add(createSettingsCheckbox("hideFacilityLeftClick", null));
        overrideSettingsContainer.add(createSettingsCheckbox("hideShipwreckInspect", null));
        overrideSettingsContainer.add(createSettingsCheckbox("hideNpcInteract", null));
        JPanel overrideSettingsHeader = createSettingsHeader("Overrides", ICON_SWAP, overrideSettingsContainer,
                true, null, null, null);

        // Loot colors
        JPanel lootColorContainer = new JPanel();
        lootColorContainer.setLayout(new BoxLayout(lootColorContainer, BoxLayout.Y_AXIS));
        lootColorContainer.setBorder(doubleBorder);

        Collection<ConfigItemDescriptor> itemDescriptors = configManager.getConfigDescriptor(config).getItems();
        for (ConfigItemDescriptor desc : itemDescriptors){
            for (LootOption opt : LootOption.values()) {
                ConfigItem item = desc.getItem();
                if (item.section().equals("lootSection") && item.keyName().equals(opt.getColorConfigKey())){
                    lootColorContainer.add(createItemColorSelector(opt, item));
                }
            }
        }
        lootColorContainer.add(createLootColorResetButtonPanel());

        JPanel lootColorHeader = createSettingsHeader("Loot underlays", ICON_COLOR, lootColorContainer,
                true, null, null, null);

        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(generalSettingsHeader);
        generalContainer.add(generalSettingsContainer);
        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(salvagingModeHeader);
        generalContainer.add(salvagingModeContainer);
        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(overrideSettingsHeader);
        generalContainer.add(overrideSettingsContainer);
        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(lootColorHeader);
        generalContainer.add(lootColorContainer);

        return generalContainer;
    }
    //endregion


    //region Build: Debug Panel
    public JPanel buildDebugPanel() {
        JPanel debugContainer = new JPanel();
        debugContainer.setLayout(new BoxLayout(debugContainer, BoxLayout.Y_AXIS));

        JButton dumpShipwrecksButton = new JButton("Dump tracked shipwrecks");
        dumpShipwrecksButton.addActionListener(e -> {
            for (GameObject wreck : plugin.actionHandler.activeShipwrecks) {
                plugin.sendChatMessage("Active: " + wreck.getLocalLocation().toString() + ", " + plugin.actionHandler.getObjectAnimation(wreck), false);
            }
            for (GameObject wreck : plugin.actionHandler.inactiveShipwrecks) {
                plugin.sendChatMessage("Inactive: " + wreck.getLocalLocation().toString() + ", " + plugin.actionHandler.getObjectAnimation(wreck), false);
            }
            });
        debugContainer.add(dumpShipwrecksButton);

        JButton idleNotifyButton = new JButton("Send idle notification");
        idleNotifyButton.addActionListener(e -> {
            plugin.sendIdleNotification("test"); });
        debugContainer.add(idleNotifyButton);

        JButton dumpLogicButton = new JButton("Dump logic engine vars");
        dumpLogicButton.addActionListener(e -> {
            plugin.actionHandler.dumpActionHandlerVars();
        });
        debugContainer.add(dumpLogicButton);

        JButton rebuildShipwreckButton = new JButton("Rebuild list of local shipwrecks");
        rebuildShipwreckButton.addActionListener(e -> {
            ActionHandler ah = plugin.actionHandler;
            for (GameObject wreck : ah.inactiveShipwrecks) {
                ah.inactiveShipwrecks.remove(wreck);
                ah.objectHighlightMap.remove(wreck);
            }
            for (GameObject wreck : ah.activeShipwrecks) {
                ah.activeShipwrecks.remove(wreck);
                ah.objectHighlightMap.remove(wreck);
            }
            ah.collectShipwrecks(client, true); });
        debugContainer.add(rebuildShipwreckButton);

        JButton dumpObjMapButton = new JButton("Dump object left click map");
        dumpObjMapButton.addActionListener(e -> {
            plugin.sendChatMessage(plugin.leftClickManager.deprioObjMap.toString(), true); });
        debugContainer.add(dumpObjMapButton);

        JButton dumpNPCMapButton = new JButton("Dump npc left click map");
        dumpNPCMapButton.addActionListener(e -> {
            plugin.sendChatMessage(plugin.leftClickManager.deprioNPCMap.toString(), true); });
        debugContainer.add(dumpNPCMapButton);

        JButton dumpBoatInfo = new JButton("Dump boat facility info");
        dumpBoatInfo.addActionListener(e -> {
            Boat b = plugin.boat;
            plugin.sendChatMessage("Helm: " + b.getHelmId() + ", ", false);
            plugin.sendChatMessage("Cargo Hold: " + b.getCargoHoldId() + ", cat " + b.getCargoHoldType().toString() +
                    ", cap: " + b.getCargoHoldCapacity(), false);
            plugin.sendChatMessage("Hook (port): " + b.getHookPortId() + ", " + b.getHookPortType().toString(), false);
            plugin.sendChatMessage("Hook (stb): " + b.getHookStarboardId() + ", " + b.getHookStarboardType().toString(), false);
            plugin.sendChatMessage("Extractor: " + b.getCrystalExtractor().getId() + ", " + b.getCrystalExtractor().getWorldView().getId() + ", "+
                    b.getCrystalExtractor().getLocalLocation().toString(), false);
        });
        debugContainer.add(dumpBoatInfo);

        JButton dumpInvItemInfo = new JButton("Dump loot container configs");
        dumpInvItemInfo.addActionListener(e -> {
            for (LootContainer cont : LootContainer.values()) {
                plugin.sendChatMessage(cont.getDefaultName()+": "+plugin.getConfigByKey(cont.getConfigKey(), Boolean.class), false);
            }

        });
        debugContainer.add(dumpInvItemInfo);

        JButton dumpConfigInfo = new JButton("Dump misc config info");
        dumpConfigInfo.addActionListener(e -> {
            plugin.sendChatMessage("getConfig: "+configManager.getConfig(SalvagingHelperConfig.class).toString(), false);
            plugin.sendChatMessage("Config descriptor: "+configManager.getConfigDescriptor(config).toString(), false);
            plugin.sendChatMessage("Config group: "+configManager.getConfigDescriptor(config).getGroup().toString(), false);
            plugin.sendChatMessage("Config sections: "+configManager.getConfigDescriptor(config).getSections().toString(), false);
            plugin.sendChatMessage("Config items: "+configManager.getConfigDescriptor(config).getItems().toString(), false);
        });
        debugContainer.add(dumpConfigInfo);

        JButton extractorReloadInfo = new JButton("Debug extractor detach when reboarding");
        extractorReloadInfo.addActionListener(e -> {
            WorldEntity wE = plugin.boat.getBoatEntity();
            Player p = client.getLocalPlayer();
            GameObject ex = plugin.boat.getCrystalExtractor();

            Tile[][] salvagingTiles = client.getTopLevelWorldView().getScene().getExtendedTiles()[0];
            for (Tile[] rowOfTiles : salvagingTiles) {
                if (rowOfTiles == null) {
                    continue;
                } // java is a Perfect Language with No Flaws
                for (Tile singleTile : rowOfTiles) {
                    if (singleTile == null) {
                        continue;
                    }
                    for (GameObject gameObject : singleTile.getGameObjects()) {
                        if (gameObject == null) {
                            continue;
                        }
                        if (gameObject.getId() == 59702 || gameObject.getId() == 59703) {
                            GameObject newEx = gameObject;
                            plugin.sendChatMessage("|Extractor (env)|" + newEx.getWorldView().getId() + "|" + newEx.getLocalLocation().toString() + "|"
                                    //+wE.transformToMainWorld(newEx.getLocalLocation()).toString()
                                    , true);
                        }
                    }
                }
            }
            clientThread.invoke(() -> {
                plugin.sendChatMessage("|Player|"+p.getWorldView().getId()+"|"+p.getLocalLocation().toString()+
                        "|"+wE.transformToMainWorld(p.getLocalLocation()).toString(), false);
                plugin.sendChatMessage("|BoatEntity|"+wE.getWorldView().getId()+"|"+wE.getLocalLocation().toString()+
                        "|"+wE.transformToMainWorld(wE.getLocalLocation()).toString(), false);
                plugin.sendChatMessage("|Extractor (stored)|"+ex.getWorldView().getId()+"|"+ex.getLocalLocation().toString()+
                        "|"+wE.transformToMainWorld(ex.getLocalLocation()).toString(), false);
            });
        });
        debugContainer.add(extractorReloadInfo);

        JButton configDescDump = new JButton("Dump config desc");
        configDescDump.addActionListener(e -> {
            clientThread.invokeLater(() -> {
                plugin.sendChatMessage(configManager.getConfigDescriptor(config).getItems().toString(), false);
                //plugin.sendChatMessage(configManager.toString(), false);
                //plugin.sendChatMessage(configManager.getConfig(SalvagingHelperConfig) , false);




            });
        });
        debugContainer.add(configDescDump);

        JButton dumpLootContainers = new JButton("Dump loot container enabled/disabled");
        dumpLootContainers.addActionListener(e -> {
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "logBasketEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "plankSackEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "gemBagEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "herbSackEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "fishBarrelEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "soulbearerEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "runePouchEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "seedBoxEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "coalBagEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "tackleBoxEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "huntsmanKitEnabled"), false);
            plugin.sendChatMessage(configManager.getConfiguration("salvagingHelper", "reagentPouchEnabled"), false);
        });
        debugContainer.add(dumpLootContainers);

        JButton dumpSettingsRows = new JButton("Dump color gui stats");
        dumpSettingsRows.addActionListener(e -> {
            clientThread.invokeLater(() -> {
                for (Component c : extractorSettingsPanel.getComponents()) {
                    plugin.sendChatMessage(c.getBounds().toString()+", "+c.toString(), false);
                    if (c instanceof JPanel) {
                        for (Component m : ((JPanel) c).getComponents()){
                            plugin.sendChatMessage(m.getBounds().toString()+", "+m.toString(), false);
                        }
                    }
                }
            });
        });
        debugContainer.add(dumpSettingsRows);





        return debugContainer;
    }
    //endregion

    //region Build: Loot Panel
    public JPanel buildLootPanel() {
        JPanel lootContainer = new JPanel();
        lootContainer.setLayout(new BoxLayout(lootContainer, BoxLayout.Y_AXIS));
        JPanel toolbarPanel = new JPanel();
        JPanel containerPanel = createLootContainerPanel();
        JPanel contentPanel = new JPanel();

        //region Toolbar and Buttons
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
        toolbarPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        toolbarPanel.add(Box.createRigidArea(new Dimension(SIDEBAR_MINUS_SCROLL - 5*TOOLBAR_ICON_WIDTH, 20)));

        JButton expandAllButton = new JButton();
        expandAllButton.setIcon(ICON_EXPAND_ALL);
        expandAllButton.setToolTipText("Expand all sections");
        expandAllButton.addActionListener(e -> {
            expandAllLoot();
        });

        JButton collapseAllButton = new JButton();
        collapseAllButton.setIcon(ICON_COLLAPSE_ALL);
        collapseAllButton.setToolTipText("Collapse all sections");
        collapseAllButton.addActionListener(e -> {
            collapseAllLoot();
        });

        JButton importButton = new JButton();
        importButton.setIcon(ICON_IMPORT);
        importButton.setToolTipText("Import loot settings from clipboard [TODO]");
        importButton.addActionListener(e -> {
            // TODO
        });

        JButton exportButton = new JButton();
        exportButton.setIcon(ICON_EXPORT);
        exportButton.setToolTipText("Copy loot settings to clipboard [TODO]");
        exportButton.addActionListener(e -> {
            lootContainer.revalidate();
            lootContainer.repaint();
        });

        JButton resetButton = new JButton();
        resetButton.setIcon(ICON_RESET);
        resetButton.setToolTipText("Reset loot settings to plugin default [TODO]");
        resetButton.addActionListener(e -> {
            // TODO
        });

        toolbarPanel.add(expandAllButton);
        toolbarPanel.add(collapseAllButton);
        toolbarPanel.add(importButton);
        toolbarPanel.add(exportButton);
        toolbarPanel.add(resetButton);
        //endregion

        contentPanel.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        //contentPanel.setMinimumSize(new Dimension(223, 500));

        //region Call "Create Salvage Panels" and Add
        for (SalvageType salvageType : SalvageType.values()) {
            contentPanel.add(Box.createRigidArea(new Dimension(1, 2)));
            contentPanel.add(buildSalvageTypePanel(salvageType));
        }

        lootContainer.add(toolbarPanel);
        lootContainer.add(containerPanel);
        lootContainer.add(contentPanel);
        //endregion

        return lootContainer;
    }
    //endregion

    //region Build: Salvage Panel
    public JPanel buildSalvageTypePanel(SalvageType salvageType) {
        JPanel containerPanel = new JPanel();
        JPanel categoryPanel = new JPanel();
        JPanel lootItemsPanel = new JPanel();
        JPanel categorySubPanel = new JPanel();
        JLabel categoryIcon = new JLabel();
        JLabel categoryTitle = new JLabel();
        JLabel categoryDropdown = new JLabel(ICON_DROPDOWN_CLICKED);

        //region Category Panel
        containerPanel.setLayout(new BorderLayout(0, 0));
        //containerPanel.setBorder(new EmptyBorder(0,1,0,1));
        containerPanel.setBorder(new LineBorder(Color.BLACK, 1));

        categorySubPanel.setLayout(new BoxLayout(categorySubPanel, BoxLayout.X_AXIS));

        itemManager.getImage(salvageType.getItemId()).addTo(categoryIcon);
        categoryTitle.setText(salvageType.getName()+
                ((salvageType.getLevelReq()>0) ? " (lv"+salvageType.getLevelReq()+")" : ""));

        categoryPanel.setLayout(new BorderLayout(5, 0));
        categoryPanel.setBorder(new EmptyBorder(0, CATEGORY_PANEL_X_PADDING, 0, CATEGORY_PANEL_X_PADDING));
        categoryPanel.setPreferredSize(new Dimension(223, 45));
        categoryPanel.setMinimumSize(new Dimension(223, 45));

        categorySubPanel.add(categoryIcon);
        categorySubPanel.add(categoryTitle);

        categoryPanel.add(categorySubPanel, BorderLayout.WEST);
        categoryPanel.add(categoryDropdown, BorderLayout.EAST);
        //endregion

        //region Category Panel MouseEvents
        categoryPanel.addMouseListener(new MouseAdapter() {
               @Override
               public void mousePressed(MouseEvent mouseEvent) {
                   categoryPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                   categorySubPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
               }

               @Override
               public void mouseReleased(MouseEvent e) {
                   lootItemsPanel.setVisible(!(lootItemsPanel.isVisible()));
                   toggleDropdownIcon(categoryDropdown);
                   categoryPanel.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                   categorySubPanel.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
               }

               @Override
               public void mouseEntered(MouseEvent e) {
                   categoryPanel.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                   categorySubPanel.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
                   categoryPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
               }

               @Override
               public void mouseExited(MouseEvent e) {
                   categoryPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
                   categorySubPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
                   categoryPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
               }
        });
        //endregion


        //region Loot Items Panel (Iterate over Salvage)
        lootItemsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        lootItemsPanel.setLayout(new BoxLayout(lootItemsPanel, BoxLayout.Y_AXIS));

        int[] itemIds = salvageType.getLootItems();
        for (int itemId : itemIds) {
            JPanel itemPanel = new JPanel(new BorderLayout(0, 0));

            LootItem lootItem = lootManager.lootItemMap.get(itemId);

            itemPanel.setBorder(new EmptyBorder(ITEM_PANEL_PADDING, ITEM_PANEL_PADDING, ITEM_PANEL_PADDING,
                    ITEM_PANEL_PADDING));
            itemPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            itemPanel.setFont(FontManager.getRunescapeSmallFont());

            JPanel itemInfo = new JPanel(new BorderLayout(0, 0));
            itemInfo.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            JLabel itemIcon = new JLabel();

            // Client thread isn't available to fetch icons when we are building panel, so queue it
            AsyncBufferedImage itemSprite = itemManager.getImage(itemId);
            itemSprite.onLoaded( () -> {
                ImageIcon scaledSprite = new ImageIcon(itemSprite.getScaledInstance(-1,
                        ITEM_PANEL_HEIGHT - ITEM_PANEL_PADDING, Image.SCALE_SMOOTH));
                itemIcon.setIcon(scaledSprite);
            });

            JLabel itemNameLabel = new JLabel();
            itemNameLabel.setText(lootItem.getName());

            itemNameLabel.setFont(FontManager.getRunescapeSmallFont());
            itemPanel.setPreferredSize(new Dimension(SIDEBAR_MINUS_SCROLL - 12, ITEM_PANEL_HEIGHT));

            // LinkedHashSet preserves insertion order and ignores duplicate entries, making it
            // more attractive than other collections
            LinkedHashSet<LootOption> optionSet = new LinkedHashSet<>();
            optionSet.add(lootItem.getLootCategory());
            optionSet.addAll(lootItem.getAllowedOptions());
            optionSet.addAll(List.of(LootManager.defaultLootOptions));

            DefaultComboBoxModel<LootOption> model = new DefaultComboBoxModel<>();
            model.addAll(optionSet);
            JComboBox<LootOption> comboBox = new JComboBox<>(model);
            comboBox.setSelectedItem(lootItem.getLootCategory());
            comboBox.setPreferredSize(new Dimension(90, 26));
            comboBox.setFont(FontManager.getRunescapeSmallFont());

            itemToComboBox.put(itemId, comboBox);
            comboBox.addActionListener(event -> {
                LootOption selectedOption = (LootOption) comboBox.getSelectedItem();
                if (selectedOption != lootItem.getLootCategory()) {
                    setItemCategory(itemId, selectedOption);
                    lootItem.updateLootCategory(selectedOption);
                }
            });

            itemInfo.add(itemIcon, BorderLayout.WEST);
            itemInfo.add(itemNameLabel, BorderLayout.CENTER);

            itemPanel.add(itemInfo, BorderLayout.WEST);
            itemPanel.add(comboBox, BorderLayout.EAST);

            lootItemsPanel.add(Box.createRigidArea(new Dimension(0, 1)));
            lootItemsPanel.add(itemPanel);
        }
        //endregion

        //region Containerization and Mapping
        containerPanel.add(categoryPanel, BorderLayout.NORTH);
        containerPanel.add(lootItemsPanel, BorderLayout.CENTER);

        salvageCategoryMap.put(salvageType, categoryPanel);
        salvageLootItemsMap.put(salvageType, lootItemsPanel);
        toSalvageCategory.put(categoryPanel, salvageType);
        toSalvageCategory.put(lootItemsPanel, salvageType);
        //endregion

        return containerPanel;
    }
    //endregion

    //region Build: Loot Container Panel
    public JPanel createLootContainerPanel() {
        JPanel lootContainerContainer = new JPanel();
        lootContainerContainer.setLayout(new BoxLayout(lootContainerContainer, BoxLayout.Y_AXIS));

        JPanel containerSectionIconContainer = new JPanel(new GridLayout(2, 6, 5, 5));
        containerSectionIconContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        containerSectionIconContainer.setBorder(new EmptyBorder(8, 15, 8, 15));

        for (LootContainer container : LootContainer.values()) {
            String configKey = container.getConfigKey();
            String name = container.getDefaultName();
            int firstItemId = container.getItemIds().get(0);

            JButton containerButton = new JButton();
            containerButton.setToolTipText("Enable/disable "+name);
            containerButton.setPreferredSize(new Dimension(32, 32));

            Boolean enabled = Boolean.parseBoolean(plugin.getConfigByKey(configKey, String.class));

            if (enabled) {
                containerButton.setBackground(CONTAINER_BUTTON_ENABLED);
            } else {
                containerButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }

            // Client thread isn't available to fetch icons when we are building panel, so queue it
            AsyncBufferedImage itemSprite = itemManager.getImage(firstItemId);
            itemSprite.onLoaded( () -> {
                // This magic function means the icon doesn't hug a random corner of its box - thank you time tracker!
                BufferedImage subIcon = itemSprite.getSubimage(0, 0, 32, 32);
                ImageIcon scaledSprite = new ImageIcon(subIcon.getScaledInstance(26,26, Image.SCALE_SMOOTH));
                containerButton.setIcon(scaledSprite);
                containerButton.setHorizontalAlignment(SwingConstants.CENTER);
                containerButton.setVerticalAlignment(SwingConstants.CENTER);
            });

            containerButton.addActionListener(e -> {
                toggleContainerButton(containerButton);
                lootManager.updateContainerMaps();
            });

            toLootContainer.put(containerButton, container);
            toContainerButton.put(container, containerButton);

            containerSectionIconContainer.add(containerButton);

        }
        lootContainerContainer.add(containerSectionIconContainer);
        return lootContainerContainer;
    }
    //endregion


    //region Settings - Checkbox

    public JPanel createSettingsCheckbox(String configKey, JLabel settingsLinkButton) {
        JPanel settingsContainer = new JPanel(new BorderLayout());
        settingsContainer.setBorder(new EmptyBorder(1, 3, 1, 3));
        settingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel settingName = new JLabel();
        JPanel checkBoxContainer = new JPanel();
        checkBoxContainer.setLayout(new BoxLayout(checkBoxContainer, BoxLayout.X_AXIS));
        if (settingsLinkButton != null) {
            checkBoxContainer.add(settingsLinkButton);
            checkBoxContainer.add(Box.createRigidArea(new Dimension(2, 2)));
        }
        JCheckBox checkBox = new JCheckBox();

        ConfigItem item = getConfigItem(configKey);
        settingName.setText(item != null ? item.name() : "");
        settingName.setToolTipText(item != null ? item.description() : "");
        checkBox.setSelected(Boolean.parseBoolean(configManager.getConfiguration("salvagingHelper", configKey)));

        checkBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                configManager.setConfiguration("salvagingHelper", configKey, true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                configManager.setConfiguration("salvagingHelper", configKey, false);
            }
        });

        checkBoxContainer.add(checkBox);

        settingsContainer.add(settingName, BorderLayout.WEST);
        settingsContainer.add(checkBoxContainer, BorderLayout.EAST);

        configWidgetMap.put(configKey, checkBox);
        configTypeMap.put(configKey, Boolean.class);

        return settingsContainer;
    }

    //endregion

    //region Settings - Enum Combobox
    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> JPanel createSettingsCombobox(String configKey, Class<T> enumType) {
        JPanel comboBoxSettingsContainer = new JPanel(new BorderLayout());
        comboBoxSettingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        comboBoxSettingsContainer.setBorder(new EmptyBorder(1, 3, 1, 3));

        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T enumValue : enumType.getEnumConstants()) {
            model.addElement(enumValue);
        }

        JComboBox<T> settingComboBox = new JComboBox<>(model);
        settingComboBox.setSelectedItem(plugin.getConfigByKey(configKey, enumType));
        settingComboBox.setMinimumSize(new Dimension(90, 22));

        settingComboBox.addActionListener(event -> {
            T selectedOption = (T) settingComboBox.getSelectedItem();
            if (selectedOption != plugin.getConfigByKey(configKey, enumType)) {
                plugin.setConfigByKey(configKey, selectedOption);
            }
        });

        Collection<ConfigItemDescriptor> itemDescriptors = configManager.getConfigDescriptor(config).getItems();
        ConfigItem configItem = itemDescriptors.stream().map(ConfigItemDescriptor::getItem).filter(k ->
                k.keyName().equals(configKey)).findFirst().orElse(null);

        if (configItem != null) {
            JLabel comboSettingsBoxTitle = new JLabel(configItem.name());
            comboSettingsBoxTitle.setToolTipText(configItem.description());
            comboBoxSettingsContainer.add(comboSettingsBoxTitle, BorderLayout.WEST);
        }

        // ComboBox won't size correctly unless we put it in its own unnecessary BoxLayout container, as BorderLayout
        // doesn't respect preferredSize or minSize
        JPanel comboBoxContainer = new JPanel();
        comboBoxContainer.setLayout(new BoxLayout(comboBoxContainer, BoxLayout.X_AXIS));
        comboBoxContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        if (enumType != FlashNotification.class) {
            comboBoxContainer.setPreferredSize(new Dimension(90, 22));
        }
        comboBoxContainer.add(settingComboBox);

        comboBoxSettingsContainer.add(comboBoxContainer, BorderLayout.EAST);

        configWidgetMap.put(configKey, settingComboBox);
        configTypeMap.put(configKey, enumType);

        return comboBoxSettingsContainer;
    }
    //endregion

    //region Settings - Integer Spinner

    // ConfigItemDescriptor:
    //      units=@net.runelite.client.config.Units(value="%") [%]
    //      type=int
    //      range=@net.runelite.client.config.Range(max=100, min=0)
    //      range=@net.runelite.client.config.Range(max=2147483647, min=-1)
    private JPanel createRangeSpinnerField(String configKey) {
        JPanel settingsContainer = new JPanel(new BorderLayout());
        settingsContainer.setBorder(new EmptyBorder(1, 3, 1, 3));
        settingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        ConfigItemDescriptor configDesc = getConfigItemDescriptor(configKey);
        ConfigItem item = (configDesc != null) ? configDesc.getItem() : null;
        Range range = (configDesc != null) ? configDesc.getRange() : null;

        JLabel settingName = new JLabel(item != null ? item.name() : "");
        settingName.setToolTipText(item != null ? item.description() : "");

        JPanel settingsFieldContainer = new JPanel();
        settingsFieldContainer.setLayout(new BoxLayout(settingsFieldContainer, BoxLayout.X_AXIS));
        settingsFieldContainer.setPreferredSize(new Dimension(90, 22));


        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                (int) plugin.getConfigByKey(configKey, Integer.class),
                (range != null) ? range.min() : 0,
                (range != null) ? range.max() : Integer.MAX_VALUE,
                1
        );

        JSpinner spinner = new JSpinner(spinnerModel);
        DecimalFormat fmt;
        if (configDesc != null && configDesc.getUnits() != null && configDesc.getUnits().value().equals("%")) {
            fmt = new DecimalFormat("###'%'");
            fmt.setMultiplier(1); // Don't nuke our values if we want to show a percentage
        } else {
            fmt = new DecimalFormat("###");
        }

        JSpinner.NumberEditor fieldEditor = new JSpinner.NumberEditor(spinner, fmt.toPattern());
        spinner.setEditor(fieldEditor);

        spinner.addChangeListener(e -> {
            plugin.setConfigByKey(configKey, spinner.getValue());
        });

        settingsFieldContainer.add(spinner);

        settingsContainer.add(settingName, BorderLayout.WEST);
        settingsContainer.add(settingsFieldContainer, BorderLayout.EAST);

        configWidgetMap.put(configKey, spinner);
        configTypeMap.put(configKey, Integer.class);

        return settingsContainer;
    }

    //endregion

    //region Settings - Header
    private JPanel createSettingsHeader(String label, ImageIcon icon, JPanel childPanel, Boolean shouldCollapse,
                                        JButton backButton, JButton testNotificationButton, JButton resetButton) {

        // settingsHeaderContainer <- (iconTitlePanel, dropdown)
        //                                    |
        //                             iconTitlePanel <- (iconLabel, titleLabel)

        JPanel settingsHeaderContainer = new JPanel(new BorderLayout());
        settingsHeaderContainer.setBorder(new EmptyBorder(4, 7, 4, 4));
        settingsHeaderContainer.setPreferredSize(new Dimension(-1, 28));
        settingsHeaderContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel iconTitlePanel = new JPanel();
        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        iconTitlePanel.setLayout(new BoxLayout(iconTitlePanel, BoxLayout.X_AXIS));
        iconTitlePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel configButtonsContainer = new JPanel();
        configButtonsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        configButtonsContainer.setLayout(new BoxLayout(configButtonsContainer, BoxLayout.X_AXIS));

        if (backButton != null) {
            iconTitlePanel.add(backButton);
            iconTitlePanel.add(Box.createRigidArea(new Dimension(5, 1)));
        }
        if (resetButton != null) {
            configButtonsContainer.add(resetButton);
            configButtonsContainer.add(Box.createRigidArea(new Dimension(3, 1)));
        }
        if (testNotificationButton != null) {
            configButtonsContainer.add(testNotificationButton);
            configButtonsContainer.add(Box.createRigidArea(new Dimension(3, 1)));
        }
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconTitlePanel.add(iconLabel);
            iconTitlePanel.add(Box.createRigidArea(new Dimension(5, 1)));
        }

        iconTitlePanel.add(titleLabel);

        if (shouldCollapse) {
            JLabel dropdown = new JLabel(ICON_DROPDOWN_CLICKED);
            dropdown.setToolTipText("Expand or collapse section");
            dropdown.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    dropdown.setIcon(childPanel.isVisible() ? ICON_DROPDOWN_CLICKED_PRESSED : ICON_DROPDOWN_UNCLICKED_PRESSED);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    childPanel.setVisible(!childPanel.isVisible());
                    dropdown.setIcon(childPanel.isVisible() ? ICON_DROPDOWN_CLICKED_HOVER : ICON_DROPDOWN_UNCLICKED_HOVER);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dropdown.setIcon(childPanel.isVisible() ? ICON_DROPDOWN_CLICKED_HOVER : ICON_DROPDOWN_UNCLICKED_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dropdown.setIcon(childPanel.isVisible() ? ICON_DROPDOWN_CLICKED : ICON_DROPDOWN_UNCLICKED);

                }
            });
            configButtonsContainer.add(dropdown);
        }

        settingsHeaderContainer.add(configButtonsContainer, BorderLayout.EAST);
        settingsHeaderContainer.add(iconTitlePanel, BorderLayout.WEST);

        return settingsHeaderContainer;
    }
    //endregion

    //region Settings - Subpanels

    private JPanel createExtractorSubpanel() {
        //JPanel extractorSettingsContainer = extendedSettingsPanel(generalTab, "Extractor Notifications");
        JPanel extractorSettingsContainer = new JPanel();
        extractorSettingsContainer.setLayout(new BoxLayout(extractorSettingsContainer, BoxLayout.Y_AXIS));
        extractorSettingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        extractorSettingsContainer.setBorder(doubleBorder);
        extractorSettingsContainer.add(createSettingsCombobox("extractorScreenFlashType", FlashNotification.class));
        extractorSettingsContainer.add(createItemColorSelector("extractorFlashColor"));
        extractorSettingsContainer.add(createSettingsCombobox("extractorTrayType", TrayIcon.MessageType.class));
        extractorSettingsContainer.add(createSettingsCombobox("extractorFocusType", RequestFocusType.class));
        extractorSettingsContainer.add(createSettingsCombobox("extractorAlertSound", NotificationSound.class));
        extractorSettingsContainer.add(createRangeSpinnerField("extractorCustomSound"));
        extractorSettingsContainer.add(createRangeSpinnerField("extractorAlertVolume"));
        extractorSettingsContainer.add(createSettingsCheckbox("extractorAlertWhileFocused", null));

        List<String> configKeys = List.of("extractorScreenFlashType", "extractorFlashColor", "extractorTrayType", "extractorFocusType",
                "extractorAlertSound", "extractorCustomSound", "extractorAlertVolume", "extractorAlertWhileFocused");

        JPanel topLevelContainer = new JPanel();
        topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
        topLevelContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        //topLevelContainer.setBorder(doubleBorder);

        JButton backButton = createBackButton(generalTab);
        JButton testButton = createTestNotificationButton("extractor");
        JPanel header = createSettingsHeader("Extractor Alerts", null, extractorSettingsContainer,
                false, backButton, testButton, createResetSettingsButton(configKeys));

        topLevelContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        topLevelContainer.add(header);
        topLevelContainer.add(extractorSettingsContainer);

        return topLevelContainer;
    }

    private JPanel createIdleSubpanel() {
        JPanel idleSettingsContainer = new JPanel();
        idleSettingsContainer.setLayout(new BoxLayout(idleSettingsContainer, BoxLayout.Y_AXIS));
        idleSettingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        idleSettingsContainer.setBorder(doubleBorder);

        idleSettingsContainer.add(createSettingsCombobox("idleScreenFlashType", FlashNotification.class));
        idleSettingsContainer.add(createItemColorSelector("idleFlashColor"));
        idleSettingsContainer.add(createSettingsCombobox("idleTrayType", TrayIcon.MessageType.class));
        idleSettingsContainer.add(createSettingsCombobox("idleFocusType", RequestFocusType.class));
        idleSettingsContainer.add(createSettingsCombobox("idleAlertSound", NotificationSound.class));
        idleSettingsContainer.add(createRangeSpinnerField("idleCustomSound"));
        idleSettingsContainer.add(createRangeSpinnerField("idleAlertVolume"));
        idleSettingsContainer.add(createSettingsCheckbox("idleAlertWhileFocused", null));

        List<String> configKeys = List.of("idleScreenFlashType", "idleFlashColor", "idleTrayType", "idleFocusType",
                "idleAlertSound", "idleCustomSound", "idleAlertVolume", "idleAlertWhileFocused");

        JPanel topLevelContainer = new JPanel();
        topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
        topLevelContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JButton backButton = createBackButton(generalTab);
        JButton testButton = createTestNotificationButton("idle");
        JPanel header = createSettingsHeader("Idle Alerts", null, idleSettingsContainer, false,
                backButton, testButton, createResetSettingsButton(configKeys));

        topLevelContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        topLevelContainer.add(header);
        topLevelContainer.add(idleSettingsContainer);

        return topLevelContainer;
    }



    //endregion

    //region Settings Buttons
    private JButton createTestNotificationButton(String type) {
        JButton containerButton = new JButton(ICON_TEST);
        containerButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        containerButton.setToolTipText("Fire a notification to test your changes.");

        containerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                containerButton.setIcon(ICON_TEST_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                containerButton.setIcon(ICON_TEST);
                if (type.equals("extractor")){
                    plugin.sendExtractorNotification();
                } else if (type.equals("idle")){
                    plugin.sendIdleNotification("Test");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                containerButton.setIcon(ICON_TEST_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                containerButton.setIcon(ICON_TEST);
            }
        });


        return containerButton;
    }

    private JButton createBackButton(MaterialTab targetPanel) {
        JButton backButton = new JButton(ICON_BACK);
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                backButton.setIcon(ICON_BACK_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                backButton.setIcon(ICON_BACK);
                tabGroup.select(targetPanel);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setIcon(ICON_BACK_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setIcon(ICON_BACK);
            }
        });
        return backButton;
    }

    private JLabel createSettingsLinkButton(int tabGroupIndex) {
        JLabel linkButton = new JLabel(ICON_SETTINGS);

        linkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                linkButton.setIcon(ICON_SETTINGS_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                linkButton.setIcon(ICON_SETTINGS);
                // TODO - fix this eventually, but right now i cba to fix load order issues
                tabGroup.select(tabGroup.getTab(tabGroupIndex));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                linkButton.setIcon(ICON_SETTINGS_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linkButton.setIcon(ICON_SETTINGS);
            }
        });

        return linkButton;
    }

    private JButton createResetSettingsButton(List<String> settingsToReset) {
        JButton resetButton = new JButton(ICON_UNSET);
        resetButton.setToolTipText("Reset these settings to plugin default");

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                resetButton.setIcon(ICON_UNSET_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resetButton.setIcon(ICON_UNSET_HOVER);
                int clickedOption = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(resetButton),
                        "Are you sure you want to reset these settings to plugin defaults? This is not reversible.",
                        "Confirm reset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (clickedOption==JOptionPane.OK_OPTION) {
                    for (String configKey : settingsToReset) {
                        //plugin.sendChatMessage("Resetting via button: "+configKey, false);
                        reset(configKey);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                resetButton.setIcon(ICON_UNSET_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                resetButton.setIcon(ICON_UNSET);
            }
        });

        return resetButton;
    }
    //endregion


    //region Settings - Loot Colors and Pickers
    public JPanel createLootColorResetButtonPanel(){
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(1, 3, 1, 3));
        JButton resetButton = new JButton("Reset colors to default");
        resetButton.setFont(FontManager.getRunescapeFont());
        resetButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        resetButton.setForeground(Color.WHITE);

        resetButton.addActionListener(e -> {
            int clickedOption = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), "Are you sure you want to " +
                    "reset to plugin default colors? This is not reversible.", "Confirm reset",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (clickedOption==JOptionPane.OK_OPTION) {
                resetLootColors();
            }
        });

        container.add(resetButton, BorderLayout.CENTER);
        return container;
    }

    public JPanel createItemColorSelector(LootOption lootOption, ConfigItem configItem){

        String configKey = lootOption.getColorConfigKey();
        String configName = configItem.name();
        String configDesc = configItem.description();
        Color loadColor = getColor(lootOption);
        String loadColorString = getColorString(loadColor);

        JPanel colorSelectorContainer = new JPanel(new BorderLayout());
        colorSelectorContainer.setBorder(new EmptyBorder(1, 3, 1, 3));
        colorSelectorContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        colorSelectorContainer.setFont(FontManager.getRunescapeFont());

        JLabel nameLabel = new JLabel(configName);
        nameLabel.setToolTipText(configDesc);

        JButton colorButton = new JButton(loadColorString);
        colorButton.setBackground(loadColor);
        colorButton.setForeground(legibleFontColorFor(loadColor));
        colorButton.setToolTipText(configDesc);
        colorButton.setPreferredSize(new Dimension(108, 22));

        colorButton.addActionListener(e -> {
            SwingUtilities.invokeLater( () -> {
                RuneliteColorPicker colorPickerPopup = colorPickerManager.create(client, colorButton.getBackground(),
                        "Choose new color", false);
                colorPickerPopup.setOnClose(newColor -> {
                    colorButton.setBackground(newColor);
                    colorButton.setForeground(legibleFontColorFor(newColor));
                    colorButton.setText(getColorString(newColor));
                    plugin.setConfigByKey(configKey, newColor);
                });
                colorPickerPopup.setVisible(true);
            });
        });

        lootColorButtonMap.put(lootOption, colorButton);

        colorSelectorContainer.add(nameLabel, BorderLayout.WEST);
        colorSelectorContainer.add(colorButton, BorderLayout.EAST);

        configWidgetMap.put(configKey, colorButton);
        configTypeMap.put(configKey, Color.class);

        return colorSelectorContainer;
    }

    public JPanel createItemColorSelector(String configKey){

        ConfigItem configItem = getConfigItem(configKey);
        String configName = configItem.name();
        String configDesc = configItem.description();
        Color loadColor = plugin.getConfigByKey(configKey, Color.class);
        String loadColorString = getColorString(loadColor);

        JPanel colorSelectorContainer = new JPanel(new BorderLayout());
        colorSelectorContainer.setBorder(new EmptyBorder(1, 3, 1, 3));
        colorSelectorContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        colorSelectorContainer.setFont(FontManager.getRunescapeFont());

        JLabel nameLabel = new JLabel(configName);
        nameLabel.setToolTipText(configDesc);

        JButton colorButton = new JButton(loadColorString);

        colorButton.setBackground(loadColor);
        colorButton.setForeground(legibleFontColorFor(loadColor));
        colorButton.setToolTipText(configDesc);
        colorButton.setPreferredSize(new Dimension(108, 22));

        colorButton.addActionListener(e -> {
            SwingUtilities.invokeLater( () -> {
                RuneliteColorPicker colorPickerPopup = colorPickerManager.create(client, colorButton.getBackground(),
                        "Choose new color", false);
                colorPickerPopup.setOnClose(newColor -> {
                    colorButton.setBackground(newColor);
                    colorButton.setForeground(legibleFontColorFor(newColor));
                    colorButton.setText(getColorString(newColor));
                    plugin.setConfigByKey(configKey, newColor);
                });
                colorPickerPopup.setVisible(true);
            });
        });

        configWidgetMap.put(configKey, colorButton);
        configTypeMap.put(configKey, Color.class);

        colorSelectorContainer.add(nameLabel, BorderLayout.WEST);
        colorSelectorContainer.add(colorButton, BorderLayout.EAST);

        return colorSelectorContainer;
    }

    public String getColorString(Color color){
        return String.format("#%02X%02X%02X%02X", color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha());
    }

    // https://stackoverflow.com/a/3943023
    public Color legibleFontColorFor(Color backgroundColor) {
        double L = calculateLuminance(backgroundColor);
        return (L>0.179) ? Color.BLACK : Color.WHITE;
    }

    public double calculateLuminance(Color color) {
        double r = (double) color.getRed() / 255;
        double g = (double) color.getGreen() / 255;
        double b = (double) color.getBlue() / 255;

        double rC = (r < 0.04045001) ? (r / 12.92) : Math.pow(((r+0.055)/1.055), 2.4);
        double gC = (g < 0.04045001) ? (g / 12.92) : Math.pow(((g+0.055)/1.055), 2.4);
        double bC = (b < 0.04045001) ? (b / 12.92) : Math.pow(((b+0.055)/1.055), 2.4);

        return (0.2126*rC + 0.7152*gC + 0.0722*bC);
    }



    private Color getLootColorByOption(LootOption lootOption) {
        if (lootOption==LootOption.KEEP) {
            return config.keepColor();
        } else if (lootOption==LootOption.DROP) {
            return config.dropColor();
        } else if (lootOption==LootOption.CONTAINER) {
            return config.containerColor();
        } else if (lootOption==LootOption.ALCH) {
            return config.alchColor();
        } else if (lootOption==LootOption.CONSUME) {
            return config.consumeColor();
        } else if (lootOption==LootOption.EQUIP) {
            return config.equipColor();
        } else if (lootOption==LootOption.PROCESS) {
            return config.processColor();
        } else if (lootOption==LootOption.CARGO_HOLD) {
            return config.cargoHoldColor();
        } else if (lootOption==LootOption.OTHER) {
            return config.otherColor();
        } else {
            return null;
        }
    }

    private Color getColor(LootOption lootOption) {
        return plugin.getConfigByKey(lootOption.getColorConfigKey(), Color.class);
    }

    private void setColor(LootOption lootOption, Color newColor) {
        plugin.setConfigByKey(lootOption.getColorConfigKey(), newColor);
    }
    //endregion

    //region Config Helpers

    private ConfigItem getConfigItem(String configKey) {
        Collection<ConfigItemDescriptor> configItems = configManager.getConfigDescriptor(config).getItems();
        for (ConfigItemDescriptor itemDesc : configItems) {
            if (itemDesc.key().equals(configKey)) {
                return itemDesc.getItem();
            }
        }
        return null;
    }

    private ConfigItemDescriptor getConfigItemDescriptor(String configKey) {
        Collection<ConfigItemDescriptor> configItems = configManager.getConfigDescriptor(config).getItems();
        for (ConfigItemDescriptor itemDesc : configItems) {
            if (itemDesc.key().equals(configKey)) {
                return itemDesc;
            }
        }
        return null;
    }

    private void reset(String configKey) {
        ConfigItemDescriptor configDesc = getConfigItemDescriptor(configKey);
        Type settingType = (configDesc != null) ? configDesc.getType(): null;

        if (settingType == Color.class) {
            Color oldColor = plugin.getConfigByKey(configKey, Color.class);
            Color defaultColor = getDefaultAndClear(configKey, Color.class);
            if (defaultColor != null && !defaultColor.equals(oldColor) && configWidgetMap.get(configKey) instanceof JButton){
                JButton associatedButton = (JButton) configWidgetMap.get(configKey);
                associatedButton.setBackground(defaultColor);
                associatedButton.setForeground(legibleFontColorFor(defaultColor));
                associatedButton.setText(getColorString(defaultColor));
            }
        }

        else if (settingType == Integer.class || settingType == int.class) {
            int oldInt = plugin.getConfigByKey(configKey, Integer.class);
            int defaultInt = getDefaultAndClear(configKey, Integer.class);
            if (defaultInt != oldInt && configWidgetMap.get(configKey) instanceof JSpinner) {
                JSpinner spinner = (JSpinner) configWidgetMap.get(configKey);
                spinner.setValue(defaultInt);
            }
        }

        else if (settingType == Boolean.class || settingType == boolean.class) {
            Boolean oldValue = plugin.getConfigByKey(configKey, Boolean.class);
            Boolean defaultValue = getDefaultAndClear(configKey, Boolean.class);
            if (!oldValue.equals(defaultValue) && configWidgetMap.get(configKey) instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) configWidgetMap.get(configKey);
                checkBox.setSelected(defaultValue);
            }
        }

        else if (settingType == FlashNotification.class || settingType == RequestFocusType.class ||
                settingType == NotificationSound.class || settingType == TrayIcon.MessageType.class) {
            Enum oldValue = plugin.getConfigByKey(configKey, settingType);
            Enum defaultValue = getDefaultAndClear(configKey, settingType);
            if (!oldValue.equals(defaultValue) && configWidgetMap.get(configKey) instanceof JComboBox) {
                JComboBox<Enum<?>> comboBox = (JComboBox) configWidgetMap.get(configKey);
                comboBox.setSelectedItem(defaultValue);
            }
        }

        else {
            plugin.sendChatMessage("Uncaught type to reset: "+settingType.toString(), false);
            configManager.unsetConfiguration("salvagingHelper", configKey);
        }
    }

    private void resetLootColors() {
        for (LootOption lootOption : LootOption.values()) {
            reset(lootOption.getColorConfigKey());
        }
        plugin.actionHandler.setInventoryWasUpdated(true);
    }

    // Must unset config before calling
    @SuppressWarnings("unchecked")
    private <T> T getDefaultAndClear(String configKey, Type T) {
        configManager.unsetConfiguration("salvagingHelper", configKey);
        try {
            return (T) SalvagingHelperConfig.class.getDeclaredMethod(configKey, (Class<?>[]) null).invoke(config, (Object[]) null);
        } catch (NoSuchMethodException e) {
            plugin.sendChatMessage("Failed config method lookup for: "+configKey, false);
        } catch (IllegalAccessException e) {
            plugin.sendChatMessage("No config method access for: "+configKey, false);
        } catch (IllegalArgumentException e) {
            plugin.sendChatMessage("Illegal 'invoke' args for: "+configKey, false);
        } catch (InvocationTargetException e) {
            plugin.sendChatMessage("Invocation target exception for: "+configKey, false);
        }
        return null;
    }

    //endregion

    public void setItemCategory(int itemId, LootOption lootCategory) {

        LootItem lootItem = lootManager.lootItemMap.get(itemId);
        LootOption oldCategory = lootItem.getLootCategory();

        if (itemId>0 && lootCategory!=null && lootCategory!=oldCategory) {
            lootItem.updateLootCategory(lootCategory);

            // Only update its current underlay if we weren't overriding it this frame
            if (lootManager.getColor(itemId).equals(lootManager.lootOptionToColor.get(oldCategory))) {
                lootManager.setColor(itemId, plugin.getConfigByKey(lootCategory.getColorConfigKey(), Color.class));
            }

            // Item might be dropped by multiple salvage types
            Collection<JComboBox<LootOption>> itemBoxes = itemToComboBox.get(itemId);
            for (JComboBox<LootOption> box : itemBoxes) {
                if (box.getSelectedItem() != lootCategory) {
                    box.setSelectedItem(lootCategory);
                }
            }
        }
    }

    public void expandLootType(SalvageType salvageType) {
        JPanel lootPanel = salvageLootItemsMap.get(salvageType);
        if (lootPanel != null) {
            lootPanel.setVisible(true);
            JPanel lootItemsPanel = (JPanel) lootPanel.getParent();
            lootItemsPanel.revalidate();
            lootItemsPanel.repaint();
            JPanel contentPanel = (JPanel) lootItemsPanel.getParent();
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void expandAllLoot() {
        for (SalvageType salvageType : salvageLootItemsMap.keySet()) {
            toggleDropdownIcon((JLabel) salvageCategoryMap.get(salvageType).getComponents()[1]);
            expandLootType(salvageType);
        }
    }

    public void collapseLootType(SalvageType salvageType) {
        JPanel lootPanel = salvageLootItemsMap.get(salvageType);
        if (lootPanel != null) {
            lootPanel.setVisible(false);
            JPanel lootItemsPanel = (JPanel) lootPanel.getParent();
            lootItemsPanel.revalidate();
            lootItemsPanel.repaint();
            JPanel contentPanel = (JPanel) lootItemsPanel.getParent();
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void collapseAllLoot() {
        for (SalvageType salvageType : salvageLootItemsMap.keySet()) {
            toggleDropdownIcon((JLabel) salvageCategoryMap.get(salvageType).getComponents()[1]);
            collapseLootType(salvageType);
        }
    }

    public void toggleDropdownIcon(JLabel dropdown) {
        if (dropdown.getIcon().equals(ICON_DROPDOWN_UNCLICKED)) {
            dropdown.setIcon(ICON_DROPDOWN_CLICKED);
        } else {
            dropdown.setIcon(ICON_DROPDOWN_UNCLICKED);
        }
    }

    public void toggleContainerButton(JButton button) {
        // TODO
        LootContainer container = toLootContainer.get(button);
        //Boolean isEnabled = Boolean.parseBoolean(configManager.getConfiguration("salvagingHelper", container.getConfigKey()));
        Boolean isEnabled = Boolean.parseBoolean(plugin.getConfigByKey(container.getConfigKey(), String.class));

        if (isEnabled) {
            plugin.setConfigByKey(container.getConfigKey(), false);
            button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        } else {
            plugin.setConfigByKey(container.getConfigKey(), true);
            button.setBackground(CONTAINER_BUTTON_ENABLED);
        }
    }


}
