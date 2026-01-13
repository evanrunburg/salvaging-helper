package com.salvaginghelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Item;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LootManager {

    public ConcurrentHashMap<Integer, Color> underlayColorMap;
    public ConcurrentHashMap<Integer, LootItem> lootItemMap;
    public ConcurrentHashMap<LootOption, Color> lootOptionToColor = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, LootContainer> toContainer = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, LootContainer> itemToItsContainer = new ConcurrentHashMap<>();
    private ConcurrentHashMap<LootContainer, Boolean> toEnabled = new ConcurrentHashMap<>();

    final private ConfigManager configManager;
    final private SalvagingHelperConfig config;
    final private SalvagingHelperPlugin plugin;

    private final static String ITEM_FILE_PATH = "src/main/resources/LootItemData.txt";
    private final static String ITEM_DELIM = ",";

    //region Items by Salvage Type
    // https://oldschool.runescape.wiki/w/Salvage
    private static final int[] opulentSalvageItems = { 31406, 32869, 32115, 11334, 31979, 31914, 31916, 5296, 5297, 5281,
            5106, 5298, 5280, 22873, 5299, 5300, 5301, 5302, 22879, 5303, 5304, 211, 207, 3051, 219, 213, 217, 215, 2485,
            1625, 1627, 1629, 442, 2355, 5525, 1993, 2007, 958, 950, 995, 13204 };
    private static final int[] fremennikSalvageItems = { 32398, 32870, 331, 359, 377, 7944, 3749, 3751, 3755, 3753, 3748,
            447, 10810, 4823, 4824, 8782, 31545, 31549, 31515, 31551, 9075, 13483, 31914 };
    private static final int[] martialSalvageItems = { 32398, 32868, 31916, 32099, 1299, 1301, 1317, 1135, 1303, 43, 822,
            823, 890, 9380, 892, 31912, 31914, 31515, 31547, 31549, 1731, 6332, 4823 };
    private static final int[] plunderedSalvageItems = { 32398, 32867, 31916, 28896, 31910, 31912, 31973, 31914, 1329,
            1333, 413, 1635, 1637, 11076, 11085, 1639, 405, 11092, 1643 };
    private static final int[] largeSalvageItems = { 32398, 32866, 1539, 6333, 8778, 4822, 31511, 31543, 31545, 31513,
            31547, 1635, 1637, 1639, 1643, 411, 413, 405, 21504, 31910, 31912 };
    private static final int[] barracudaSalvageItems = { 31989, 32865, 32398, 31541, 31543, 1941, 1521, 6333, 1539,
            31970, 954, 8778, 436, 8780, 31716, 371, 383, 32349, 28896, 1963, 2 };
    private static final int[] fishySalvageItems = { 31989, 32398, 32864, 1521, 8778, 32085, 1511, 2351, 4820, 1539, 960,
            2353, 314, 313, 377, 353, 371, 307, 311, 301, 401, 31906, 31908 };
    private static final int[] smallSalvageItems = { 31989, 32398, 32863, 2349, 4819, 1511, 4820, 960, 1521, 32083, 2351,
            8778, 32085, 2353, 556, 555, 31906, 31908, 886, 995, 526 };
    public static final int[] otherLootItems = { 1623, 1621, 1619, 1617, 1462, 830, 987, 985, 1247, 2366, 1249 };

    //endregion

    @RequiredArgsConstructor @Getter
    public enum SalvageType {
        SMALL("Small Salvage", 15, ItemID.SAILING_SMALL_SHIPWRECK_SALVAGE, 10, 5.5, smallSalvageItems),
        FISHY("Fishy Salvage", 26, ItemID.SAILING_FISHERMAN_SHIPWRECK_SALVAGE, 17, 9, fishySalvageItems),
        BARRACUDA("Barracuda Salvage", 35, ItemID.SAILING_BARRACUDA_SHIPWRECK_SALVAGE, 31, 15.5, barracudaSalvageItems),
        LARGE("Large Salvage", 53, ItemID.SAILING_LARGE_SHIPWRECK_SALVAGE, 48, 24, largeSalvageItems),
        PLUNDERED("Plundered Salvage", 64, ItemID.SAILING_PIRATE_SHIPWRECK_SALVAGE, 76, 31.5, plunderedSalvageItems),
        MARTIAL("Martial Salvage", 73, ItemID.SAILING_MERCENARY_SHIPWRECK_SALVAGE, 138, 63.5, martialSalvageItems),
        FREMENNIK("Fremennik Salvage", 80, ItemID.SAILING_FREMENNIK_SHIPWRECK_SALVAGE, 162, 75, fremennikSalvageItems),
        OPULENT("Opulent Salvage", 87, ItemID.SAILING_MERCHANT_SHIPWRECK_SALVAGE, 200, 95, opulentSalvageItems),
        OTHER("Gem Drop Table and Other", 0, ItemID.SAPPHIRE, 999999, 999999, otherLootItems);

        private final String name;
        private final int levelReq;
        private final int itemId;
        private final int baseXp;
        private final double sortXp;
        private final int[] lootItems;
    }

    @RequiredArgsConstructor @Getter
    public enum LootOption { // Conditionals are Container, Consume, Equip, Process, Cargo_Hold
        KEEP("Keep", new ArrayList<>(List.of("Use")), "keepColor"),
        DROP("Drop", new ArrayList<>(Arrays.asList("Drop", "Destroy")), "dropColor"),
        CONTAINER("Container", new ArrayList<>(List.of("Use")), "containerColor"),
        ALCH("Alch", new ArrayList<>(List.of("Use")), "alchColor"),
        CONSUME("Consume", new ArrayList<>(Arrays.asList("Use", "Drink", "Apply")), "consumeColor"),
        EQUIP("Equip", new ArrayList<>(Arrays.asList("Equip", "Wear", "Wield")), "equipColor"),
        PROCESS("Process", new ArrayList<>(List.of("Use")), "processColor"),
        CARGO_HOLD("Cargo Hold", new ArrayList<>(List.of("Use")), "cargoHoldColor"), // Eligible items: https://oldschool.runescape.wiki/w/Cargo_hold
        OTHER("Other", new ArrayList<>(List.of("Use")), "otherColor");

        private final String name;
        private final ArrayList<String> menuOptionWhitelist;
        private final String colorConfigKey;

        @Override
        public String toString() { return name; }
    }

    public static LootOption[] defaultLootOptions = new LootOption[]{
            LootOption.KEEP,
            LootOption.DROP,
            LootOption.ALCH,
            LootOption.OTHER
    };

    public LootManager(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, ConfigManager configManager) {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        init();
    }

    // Produce and cache maps now to reduce per-frame overhead and simplify reloading on config change
    public void init() {
        lootItemMap = new ConcurrentHashMap<>();
        underlayColorMap = new ConcurrentHashMap<>();
        rebuildLootColors();
        initializeItemDefaults();
        buildContainerMaps();
    }

    public void rebuildLootColors() {
        lootOptionToColor.put(LootOption.KEEP, config.keepColor());
        lootOptionToColor.put(LootOption.DROP, config.dropColor());
        lootOptionToColor.put(LootOption.CONTAINER, config.containerColor());
        lootOptionToColor.put(LootOption.ALCH, config.alchColor());
        lootOptionToColor.put(LootOption.CONSUME, config.consumeColor());
        lootOptionToColor.put(LootOption.EQUIP, config.equipColor());
        lootOptionToColor.put(LootOption.PROCESS, config.processColor());
        lootOptionToColor.put(LootOption.CARGO_HOLD, config.cargoHoldColor());
        lootOptionToColor.put(LootOption.OTHER, config.otherColor());
    }

    private void initializeItemDefaults() {
        // Import per-item properties and defaults from a text file to save space.
        // Line format: itemID,defaultLootOption,canContainer,canConsume,canEquip,canProcess,canCargoHold,name
        // (Manually specifying name despite obvious issues to easily allow replacements of overly-long
        // names with abbreviations, e.g. Dragon cannon barrel -> D. cannon barrel)
        try (BufferedReader br = new BufferedReader(new FileReader(ITEM_FILE_PATH))) {
            String singleLine;
            while ((singleLine = br.readLine()) != null) {
                String[] params = singleLine.split(ITEM_DELIM);
                LootItem lootItem = new LootItem(Integer.parseInt(params[0]), configManager, lootItemMap,
                        LootOption.valueOf(params[1]), Boolean.parseBoolean(params[2]), Boolean.parseBoolean(params[3]),
                        Boolean.parseBoolean(params[4]), Boolean.parseBoolean(params[5]),
                        Boolean.parseBoolean(params[6]), params[7], parseLootCategory(params[8]));
                lootItemMap.put(lootItem.getItemId(), lootItem);
                setLootColor(lootItem.getItemId(), lootItem.getLootCategory());
            }
        } catch (IOException e) {
            plugin.sendChatMessage("Salvaging Helper couldn't find file to import loot item data: "+e.toString(), true);
        }
    }

    private LootContainer parseLootCategory(String toParse) {
        if (toParse.equals("*")) {
            return null;
        } else {
            try {
                LootContainer c = LootContainer.valueOf(toParse);
                return c;
            } catch (IllegalArgumentException e) {
                plugin.sendChatMessage("Salvaging Helper: error parsing params[8] while building LootItem", false);
            }
        }
        return null;
    }

    public void setLootColor(int itemId, LootOption lootOption) {
        underlayColorMap.put(itemId, lootOptionToColor.get(lootOption));
    }

    public void rebuildUnderlayColorMap() {
        for (int item : underlayColorMap.keySet()) {
            LootItem lootItem = lootItemMap.get(item);
            if (lootItem != null) {
                setLootColor(item, lootItem.getLootCategory());
            }
        }
    }

    public Color toLootColor(int itemId) {
        return underlayColorMap.get(itemId);
    }

    public LootItem getLootItem(int itemId) {
        return lootItemMap.get(itemId);
    }

    public LootItem getLootItem(Item item) {
        return lootItemMap.get(item.getId());
    }

    public LootContainer isItemLootContainer(int itemId) {
        for (LootContainer container : LootContainer.values()) {
            if (container.getItemIds().contains(itemId)) {
                return container;
            }
        }
        return null;
    }

    public void buildContainerMaps() {
        for (LootContainer container : LootContainer.values()) {
            for (int itemId : container.getItemIds()) {
                toContainer.put(itemId, container);
            }
        }

        toEnabled.put(LootContainer.LOG_BASKET, config.logBasketEnabled());
        toEnabled.put(LootContainer.PLANK_SACK, config.plankSackEnabled());
        toEnabled.put(LootContainer.GEM_BAG, config.gemBagEnabled());
        toEnabled.put(LootContainer.HERB_SACK, config.herbSackEnabled());
        toEnabled.put(LootContainer.FISH_BARREL, config.fishBarrelEnabled());
        toEnabled.put(LootContainer.SOULBEARER, config.soulbearerEnabled());
        toEnabled.put(LootContainer.RUNE_POUCH, config.runePouchEnabled());
        toEnabled.put(LootContainer.SEED_BOX, config.seedBoxEnabled());
        toEnabled.put(LootContainer.COAL_BAG, config.coalBagEnabled());
        toEnabled.put(LootContainer.TACKLE_BOX, config.tackleBoxEnabled());
        toEnabled.put(LootContainer.HUNTSMAN_KIT, config.huntsmanKitEnabled());
        toEnabled.put(LootContainer.REAGENT_POUCH, config.reagentPouchEnabled());

        for (LootContainer container : LootContainer.values()) {
            for (int itemId : container.getEligibleItems()) {
                itemToItsContainer.put(itemId, container);
            }
        }
    }

    public void setColor(int itemId, Color color) {
        underlayColorMap.put(itemId, color);
    }

    public void updateContainerMaps() {
        toEnabled.clear();
        buildContainerMaps();
    }

    public LootContainer getContainer(int itemId) {
        return toContainer.get(itemId);
    }

    public LootContainer getContainerFromEligibleItem(int itemId) {
        return itemToItsContainer.get(itemId);
    }

    public Boolean isContainerEnabled(LootContainer container) {
        return toEnabled.get(container);
    }
}
