package com.salvaginghelper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import com.salvaginghelper.LootManager.SalvageType;
import com.salvaginghelper.LootManager.LootOption;
import com.salvaginghelper.LootItem;



public class SalvagingHelperPanel extends PluginPanel {

    //region Variable Declarations

    private SalvagingHelperPlugin plugin;
    private SalvagingHelperConfig config;
    private boolean active = false;
    public HashMap<SalvageType, JPanel> salvageCategoryMap = new HashMap<>();
    public HashMap<SalvageType, JPanel> salvageLootItemsMap = new HashMap<>();
    public HashMap<JPanel, SalvageType> toSalvageCategory = new HashMap<>();
    public Multimap<Integer, JComboBox<LootOption>> itemToComboBox = ArrayListMultimap.create();

    // https://fonts.google.com/icons (Apache 2.0)
    private final ImageIcon ICON_DROPDOWN_UNCLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_down_20px.png"));
    private final ImageIcon ICON_DROPDOWN_CLICKED = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_up_20px.png"));
    private final ImageIcon ICON_EXPAND_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_expand_20px.png"));
    private final ImageIcon ICON_COLLAPSE_ALL = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_collapse_20px.png"));
    private final ImageIcon ICON_RESET = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_restart_20px.png"));
    private final ImageIcon ICON_IMPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_download_20px.png"));
    private final ImageIcon ICON_EXPORT = new ImageIcon(ImageUtil.loadImageResource(SalvagingHelperPlugin.class, "google_upload_20px.png"));


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

    @Inject
    private ItemManager itemManager;

    //@Inject
    private ConfigManager configManager;

    @Inject
    private Client client;

    @Inject
    private LootManager lootManager;

    //@Inject
    private ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

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

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JScrollPane scrollPane = getScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel tabGroupPanel = new JPanel();
        JPanel displayPanel = new JPanel();
        JPanel generalTabPanel = new JPanel();
        JPanel lootTabPanel = buildLootPanel();
        JPanel debugTabPanel = buildDebugPanel();

        displayPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        displayPanel.setLayout(new BorderLayout(0, 0));

        MaterialTabGroup tabGroup = new MaterialTabGroup(displayPanel);
        tabGroup.setLayout(new BorderLayout(0, 0));
        MaterialTab generalTab = new MaterialTab("General", tabGroup, generalTabPanel);
        MaterialTab lootTab = new MaterialTab("Loot", tabGroup, lootTabPanel);
        MaterialTab debugTab = new MaterialTab("Debug", tabGroup, debugTabPanel);

        generalTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        lootTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        debugTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);


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
        tabGroup.select(lootTab);
        collapseAllLoot();
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
            plugin.sendIdleNotification(); });
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
            plugin.sendChatMessage("Cargo Hold: " + b.getCargoHoldId() + ", cat " + b.getCargoHoldType().toString() + ", cap: " + b.getCargoHoldCapacity(), false);
            plugin.sendChatMessage("Hook (port): " + b.getHookPortId() + ", " + b.getHookPortType().toString(), false);
            plugin.sendChatMessage("Hook (stb): " + b.getHookStarboardId() + ", " + b.getHookStarboardType().toString(), false);
        });
        debugContainer.add(dumpBoatInfo);

        JButton dumpInvItemInfo = new JButton("Item composition info for red topaz");
        dumpInvItemInfo.addActionListener(e -> {
            clientThread.invoke( () -> {
                ItemComposition itemComp = client.getItemDefinition(1629);
                for (String action : itemComp.getInventoryActions()) {
                    plugin.sendChatMessage(action, true);
                }
            });
        });
        debugContainer.add(dumpInvItemInfo);

        return debugContainer;
    }
    //endregion

    //region Build: Loot Panel
    public JPanel buildLootPanel() {
        JPanel lootContainer = new JPanel(new BorderLayout(0, 0));
        JPanel toolbarPanel = new JPanel();
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
            contentPanel.add(buildSalvageTypePanel(salvageType));
        }

        lootContainer.add(toolbarPanel, BorderLayout.NORTH);
        lootContainer.add(contentPanel, BorderLayout.CENTER);
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
        containerPanel.setBorder(new EmptyBorder(0,1,0,1));

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
    //endregion

    //region Build: Misc Loot Panel
    // TODO
    public JPanel buildMiscLootPanel() {
        return null;
    }
    //endregion

/*    public void onSelectEvent(MaterialTab tab) {
        config.setActiveTab((JPanel) tab.getContent());
    }

    public void switchTab(MaterialTab toTab) {
        //tabGroup.select(toTab);
    }*/



    public void setItemCategory(int itemId, LootOption lootCategory) {
        LootItem lootItem = lootManager.lootItemMap.get(itemId);
        if (itemId>0 && lootCategory!=null && lootCategory!=lootItem.getLootCategory()) {
            lootItem.updateLootCategory(lootCategory);
            plugin.sendChatMessage("Item category for item "+itemId+" updated to "+lootCategory.toString(), false);
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

/*    public void createJPanelListener(JPanel panel, String panelName, String subType) {
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                JPanel eventPanel = (JPanel) e.getComponent();
                if (plugin.debug && config.showSidePanelMessages()) {
                    plugin.debugLog(Arrays.asList(
                            panelName,
                            subType,
                            eventPanel.getWidth()+"",
                            eventPanel.getHeight()+"",
                            e.getSource().toString(),
                            eventPanel.getLayout().toString(),
                            eventPanel.getPreferredSize().getWidth()+"",
                            eventPanel.getPreferredSize().getHeight()+"",
                            eventPanel.getMinimumSize().getWidth()+"",
                            eventPanel.getMinimumSize().getHeight()+"",
                            eventPanel.getMaximumSize().getWidth()+"",
                            eventPanel.getMaximumSize().getHeight()+"",
                            eventPanel.getComponentCount()+"", //# Subcomponents
                            eventPanel.getParent().toString(), // Parent
                            String.valueOf(eventPanel.isShowing()),
                            String.valueOf(eventPanel.isVisible())
                    ), plugin);
                }
            }
        });
    }*/


}
