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
import java.util.*;
import java.util.List;

import com.salvaginghelper.LootManager.SalvageType;
import com.salvaginghelper.LootManager.LootOption;
import com.salvaginghelper.ActionHandler.SalvageMode;


public class SalvagingHelperPanel extends PluginPanel {

    //region Variable Declarations

    private SalvagingHelperPlugin plugin;
    private SalvagingHelperConfig config;
    private ColorPickerManager colorPickerManager;
    private boolean active = false;
    public HashMap<SalvageType, JPanel> salvageCategoryMap = new HashMap<>();
    public HashMap<SalvageType, JPanel> salvageLootItemsMap = new HashMap<>();
    public HashMap<JPanel, SalvageType> toSalvageCategory = new HashMap<>();
    public Multimap<Integer, JComboBox<LootOption>> itemToComboBox = ArrayListMultimap.create();
    public HashMap <JButton, LootContainer> toLootContainer = new HashMap<>();
    public HashMap <LootContainer, JButton> toContainerButton = new HashMap<>();
    //private ConcurrentHashMap<String, JCheckBox> configToCheckboxMap = new ConcurrentHashMap<>();
    //private ConcurrentHashMap<JCheckBox, String> checkboxToConfigMap = new ConcurrentHashMap<>();

    @Getter
    private JComboBox<SalvageMode> salvageModeComboBox;

    // https://fonts.google.com/icons (Apache 2.0)
    private final ImageIcon ICON_DROPDOWN_UNCLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px.png"));
    private final ImageIcon ICON_DROPDOWN_UNCLICKED_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px_gray.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED_HOVER = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px_gray.png"));
    private final ImageIcon ICON_EXPAND_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_20px.png"));
    private final ImageIcon ICON_COLLAPSE_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_collapse_20px.png"));
    private final ImageIcon ICON_RESET = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_restart_20px.png"));
    private final ImageIcon ICON_IMPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_download_20px.png"));
    private final ImageIcon ICON_EXPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_upload_20px.png"));
    private final ImageIcon ICON_PLUGIN = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_settings_ethernet_20px.png"));
    private final ImageIcon ICON_HOOK = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_phishing_20px.png"));
    private final ImageIcon ICON_SWAP = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_swap_horiz_20px.png"));
    private final ImageIcon ICON_BUG = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_bug_report_20px.png"));
    private final ImageIcon ICON_STRATEGY = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_flowchart_20px.png"));
    private final ImageIcon ICON_COLOR = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_palette_20px.png"));


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

        JPanel tabGroupPanel = new JPanel();
        JPanel displayPanel = new JPanel();
        JPanel generalTabPanel = buildGeneralPanel();
        JPanel lootTabPanel = buildLootPanel();
        JPanel debugTabPanel = buildDebugPanel();

        displayPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        displayPanel.setLayout(new BorderLayout(0, 0));

        MaterialTabGroup tabGroup = new MaterialTabGroup(displayPanel);
        tabGroup.setLayout(new BorderLayout(0, 0));
        MaterialTab generalTab = new MaterialTab("General", tabGroup, generalTabPanel);
        MaterialTab lootTab = new MaterialTab("Loot", tabGroup, lootTabPanel);
        MaterialTab debugTab = new MaterialTab(ICON_BUG, tabGroup, debugTabPanel);

        generalTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        generalTab.setFont(FontManager.getRunescapeBoldFont());
        lootTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        lootTab.setFont(FontManager.getRunescapeBoldFont());
        debugTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        debugTab.setFont(FontManager.getRunescapeBoldFont());

        // Material Tabs
        tabGroup.addTab(generalTab);
        tabGroup.addTab(lootTab);
        if (plugin.debug) { tabGroup.addTab(debugTab); }

        tabGroupPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tabGroupPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        tabGroupPanel.setPreferredSize(new Dimension(SIDEBAR_MINUS_SCROLL, TAB_GROUP_PANEL_HEIGHT));
        tabGroupPanel.add(generalTab);
        tabGroupPanel.add(lootTab);
        tabGroupPanel.add(debugTab);

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
        generalSettingsContainer.add(createSettingsCheckbox("drawShipwreckRadius"));
        generalSettingsContainer.add(createSettingsCheckbox("drawHookLocation"));
        generalSettingsContainer.add(createSettingsCheckbox("enableLootOverlays"));
        generalSettingsContainer.add(createSettingsCheckbox("swapInvItemOptions"));
        generalSettingsContainer.add(createSettingsCheckbox("idleAlerts"));
        generalSettingsContainer.add(createSettingsCheckbox("extractorAlerts"));
        generalSettingsContainer.add(createSettingsCheckbox("hideCrewmateOverhead"));
        generalSettingsContainer.add(createSettingsCheckbox("hideOthersCrewmateOverhead"));
        JPanel generalSettingsHeader = createSettingsHeader("Plugin modules", ICON_PLUGIN, generalSettingsContainer, true);

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
                configManager.setConfiguration("salvagingHelper", "salvageMode", selectedOption);
            }
        });
        salvageModeDropdownContainer.add(salvageModeIcon);
        salvageModeDropdownContainer.add(Box.createRigidArea(new Dimension(4, 1)));
        salvageModeDropdownContainer.add(salvageModeComboBox);


        JPanel salvagingModeContainer = new JPanel();
        salvagingModeContainer.setLayout(new BoxLayout(salvagingModeContainer, BoxLayout.Y_AXIS));
        salvagingModeContainer.setBorder(doubleBorder);
        salvagingModeContainer.add(salvageModeDropdownContainer);
        salvagingModeContainer.add(createSettingsCheckbox("dropAllSalvage"));
        salvagingModeContainer.add(createSettingsCheckbox("minMaxHookUptime"));
        salvagingModeContainer.add(createSettingsCheckbox("cargoBeforeSort"));
        salvagingModeContainer.add(createSettingsCheckbox("dockOnFull"));
        JPanel salvagingModeHeader = createSettingsHeader("Salvaging strategy", ICON_HOOK, salvagingModeContainer, true);

        // Overrides
        JPanel overrideSettingsContainer = new JPanel();
        overrideSettingsContainer.setBorder(doubleBorder);
        overrideSettingsContainer.setLayout(new BoxLayout(overrideSettingsContainer, BoxLayout.Y_AXIS));
        overrideSettingsContainer.add(createSettingsCheckbox("hideGroundItems"));
        overrideSettingsContainer.add(createSettingsCheckbox("hideCrewmateLeftClick"));
        overrideSettingsContainer.add(createSettingsCheckbox("hideFacilityLeftClick"));
        overrideSettingsContainer.add(createSettingsCheckbox("hideShipwreckInspect"));
        overrideSettingsContainer.add(createSettingsCheckbox("hideNpcInteract"));
        JPanel overrideSettingsHeader = createSettingsHeader("Overrides", ICON_SWAP, overrideSettingsContainer, true);

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

        JPanel lootColorHeader = createSettingsHeader("Loot underlays", ICON_COLOR, lootColorContainer, true);

        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(generalSettingsHeader);
        generalContainer.add(generalSettingsContainer);
        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(salvagingModeHeader);
        generalContainer.add(salvagingModeContainer);
        generalContainer.add(Box.createRigidArea(new Dimension(1, 4)));
        generalContainer.add(overrideSettingsHeader);
        generalContainer.add(overrideSettingsContainer);
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
                plugin.sendChatMessage(cont.getDefaultName()+": "+configManager.getConfiguration("salvagingHelper", cont.getConfigKey()), true);
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
        contentPanel.setMinimumSize(new Dimension(223, 500));

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
            comboBox.setPreferredSize(new Dimension(86, 26));
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

            Boolean enabled = Boolean.parseBoolean(configManager.getConfiguration("salvagingHelper", configKey));

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

            if (enabled) {
                containerButton.setBackground(CONTAINER_BUTTON_ENABLED);
            } else {
                containerButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }

            containerButton.addActionListener(e -> {
                toggleContainerButton(containerButton);
                lootManager.updateContainerMaps();
            });

            toLootContainer.put(containerButton, container);
            toContainerButton.put(container, containerButton);

            containerSectionIconContainer.add(containerButton);

        }

        //JPanel containerSectionTitle = createSettingsHeader("Loot Container Configuration", null, containerSectionIconContainer, true);

        //lootContainerContainer.add(containerSectionTitle);
        lootContainerContainer.add(containerSectionIconContainer);

        return lootContainerContainer;
    }
    //endregion


    //region Settings Builders

    public JPanel createSettingsCheckbox(String configKey) {
        JPanel settingsContainer = new JPanel(new BorderLayout());
        settingsContainer.setBorder(new EmptyBorder(1, 3, 1, 3));
        //settingsContainer.setBorder(doubleBorder);
        settingsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel settingName = new JLabel();
        JCheckBox checkBox = new JCheckBox();

        Collection<ConfigItemDescriptor> configItems = configManager.getConfigDescriptor(config).getItems();
        for (ConfigItemDescriptor itemDesc : configItems) {
            if (itemDesc.key().equals(configKey)) {
                ConfigItem item = itemDesc.getItem();
                settingName.setText(item.name());
                settingName.setToolTipText(item.description());
                checkBox.setSelected(Boolean.parseBoolean(configManager.getConfiguration("salvagingHelper", configKey)));
            }
        }

        checkBox.addItemListener(e -> {
            if (e.getStateChange()==ItemEvent.SELECTED) {
                configManager.setConfiguration("salvagingHelper", configKey, true);
            } else if (e.getStateChange()==ItemEvent.DESELECTED) {
                configManager.setConfiguration("salvagingHelper", configKey, false);
            }
        });

        settingsContainer.add(settingName, BorderLayout.WEST);
        settingsContainer.add(checkBox, BorderLayout.EAST);

        return settingsContainer;
    }

    public JPanel createSettingsHeader(String label, ImageIcon icon, JPanel childPanel, Boolean shouldCollapse) {

        // settingsHeaderContainer <- (iconTitlePanel, dropdown)
        //                                    |
        //                             iconTitlePanel <- (iconLabel, titleLabel)

        JPanel settingsHeaderContainer = new JPanel(new BorderLayout());
        settingsHeaderContainer.setBorder(new EmptyBorder(4, 7, 4, 4));
        settingsHeaderContainer.setPreferredSize(new Dimension(-1, 28)); // or SIDEBAR_MINUS_SCROLL?
        settingsHeaderContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel iconTitlePanel = new JPanel();
        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        iconTitlePanel.setLayout(new BoxLayout(iconTitlePanel, BoxLayout.X_AXIS));

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
                    dropdown.setIcon( dropdown.getIcon()==ICON_DROPDOWN_CLICKED_HOVER ? ICON_DROPDOWN_CLICKED : ICON_DROPDOWN_UNCLICKED );
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    childPanel.setVisible(!childPanel.isVisible());
                    dropdown.setIcon( dropdown.getIcon()==ICON_DROPDOWN_CLICKED ? ICON_DROPDOWN_UNCLICKED : ICON_DROPDOWN_CLICKED );
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dropdown.setIcon( dropdown.getIcon()==ICON_DROPDOWN_CLICKED ? ICON_DROPDOWN_CLICKED_HOVER : ICON_DROPDOWN_UNCLICKED_HOVER );
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dropdown.setIcon( dropdown.getIcon()==ICON_DROPDOWN_CLICKED_HOVER ? ICON_DROPDOWN_CLICKED : ICON_DROPDOWN_UNCLICKED );
                }
            });
            settingsHeaderContainer.add(dropdown, BorderLayout.EAST);
        }

        settingsHeaderContainer.add(iconTitlePanel, BorderLayout.WEST);

        return settingsHeaderContainer;
    }

    public JPanel createItemColorSelector(LootOption lootOption, ConfigItem configItem){

        String configKey = lootOption.getColorConfigKey();
        String configName = configItem.name();
        String configDesc = configItem.description();
        Color loadColor = getColor(lootOption);
        String loadColorString = getColorString(loadColor);

        JPanel colorSelectorContainer = new JPanel(new BorderLayout());
        colorSelectorContainer.setBorder(new EmptyBorder(2, 2, 2, 2));
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
                    colorButton.setBackground(legibleFontColorFor(newColor));
                    colorButton.setText(getColorString(newColor));
                    plugin.setConfigByKey(configKey, newColor);
                });
                colorPickerPopup.setVisible(true);
            });
        });

        colorSelectorContainer.add(nameLabel, BorderLayout.WEST);
        colorSelectorContainer.add(colorButton, BorderLayout.EAST);

        return colorSelectorContainer;
    }

    public void toggleGeneralPanelCollapsed(JPanel panelToCollapse, JLabel collapseIcon){

    }
    //endregion

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

        return (0.2126*r + 0.7152*g + 0.0722*b);
    }

    public void setItemCategory(int itemId, LootOption lootCategory) {

        LootItem lootItem = lootManager.lootItemMap.get(itemId);
        LootOption oldCategory = lootItem.getLootCategory();

        if (itemId>0 && lootCategory!=null && lootCategory!=oldCategory) {
            lootItem.updateLootCategory(lootCategory);

            // Only update its current underlay if we weren't overriding it this frame
            if (lootManager.toLootColor(itemId).equals(lootManager.lootOptionToColor.get(oldCategory))) {
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
        Boolean isEnabled = Boolean.parseBoolean(configManager.getConfiguration("salvagingHelper", container.getConfigKey()));
        if (isEnabled) {
            configManager.setConfiguration("salvagingHelper", container.getConfigKey(), false);
            button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        } else {
            configManager.setConfiguration("salvagingHelper", container.getConfigKey(), true);
            button.setBackground(CONTAINER_BUTTON_ENABLED);
        }
    }

    private Color getColor(LootOption lootOption) {
        return plugin.getConfigByKey(lootOption.getColorConfigKey(), Color.class);
    }

    private void setColor(LootOption lootOption, Color newColor) {
        plugin.setConfigByKey(lootOption.getColorConfigKey(), newColor);
    }
}
