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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LootManager {

    //private HashMap<Integer, LootConfig> LootConfigMap = new HashMap<>();
    //private HashMap<Integer, LootOption> CategoryMap;
    private ConcurrentHashMap<Integer, Color> underlayColorMap;
    public ConcurrentHashMap<Integer, LootItem> lootItemMap;
    public HashMap<LootOption, Color> lootOptionToColor = new HashMap<>();
    //private HashMap<Integer, LootItem> LeftClickMap = new HashMap<>();

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
        SMALL("Small Salvage", 15, ItemID.SAILING_SMALL_SHIPWRECK_SALVAGE, smallSalvageItems),
        FISHY("Fishy Salvage", 26, ItemID.SAILING_FISHERMAN_SHIPWRECK_SALVAGE, fishySalvageItems),
        BARRACUDA("Barracuda Salvage", 35, ItemID.SAILING_BARRACUDA_SHIPWRECK_SALVAGE, barracudaSalvageItems),
        LARGE("Large Salvage", 53, ItemID.SAILING_LARGE_SHIPWRECK_SALVAGE, largeSalvageItems),
        PLUNDERED("Plundered Salvage", 64, ItemID.SAILING_PIRATE_SHIPWRECK_SALVAGE, plunderedSalvageItems),
        MARTIAL("Martial Salvage", 73, ItemID.SAILING_MERCENARY_SHIPWRECK_SALVAGE, martialSalvageItems),
        FREMENNIK("Fremennik Salvage", 80, ItemID.SAILING_FREMENNIK_SHIPWRECK_SALVAGE, fremennikSalvageItems),
        OPULENT("Opulent Salvage", 87, ItemID.SAILING_MERCHANT_SHIPWRECK_SALVAGE, opulentSalvageItems),
        OTHER("Gem Drop Table and Other", 0, ItemID.SAPPHIRE, otherLootItems);

        private final String name;
        private final int levelReq;
        private final int itemId;
        private final int[] lootItems;
    }


    @RequiredArgsConstructor @Getter
    public enum LootOption { // Conditionals are Container, Consume, Equip, Process, Cargo_Hold
        KEEP("Keep"),
        DROP("Drop"),
        CONTAINER("Container"),
        ALCH("Alch"),
        CONSUME("Consume"),
        EQUIP("Equip"),
        PROCESS("Process"),
        CARGO_HOLD("Cargo Hold"), // Eligible items: https://oldschool.runescape.wiki/w/Cargo_hold
        OTHER("Other");

        private final String name;

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
        // TODO: construct left click map
    }

    // Produce and cache maps here to reduce per-frame overhead and simplify reloading on config change
    public void init() {

        lootOptionToColor = new HashMap<>();
        lootItemMap = new ConcurrentHashMap<>();
        underlayColorMap = new ConcurrentHashMap<>();

        initializeItemDefaults();
    }

    private void initializeItemDefaults() {
        // Import per-item properties and defaults from a text file to save space
        // line format: itemID,defaultLootOption,canContainer,canConsume,canEquip,canProcess,canCargoHold,name
        try (BufferedReader br = new BufferedReader(new FileReader(ITEM_FILE_PATH))) {
            String singleLine;
            while ((singleLine = br.readLine()) != null) {
                String[] params = singleLine.split(ITEM_DELIM);
                LootItem lootItem = new LootItem(Integer.parseInt(params[0]), configManager, lootItemMap, LootOption.valueOf(params[1]),
                        Boolean.parseBoolean(params[2]), Boolean.parseBoolean(params[3]), Boolean.parseBoolean(params[4]),
                        Boolean.parseBoolean(params[5]), Boolean.parseBoolean(params[6]), params[7]);
                lootItemMap.put(lootItem.getItemId(), lootItem);
                setLootColor(lootItem.getItemId(), lootItem.getLootCategory(), config);
            }
        } catch (IOException e) {
            plugin.sendChatMessage("SalvagingHelper couldn't find file to import loot item data: "+e.toString(), true);
        }
    }

    // TODO: replace with hashmap
    public void setLootColor(int itemId, LootOption lootOption, SalvagingHelperConfig config) {
        switch (lootOption) {
            case KEEP:
                underlayColorMap.put(itemId, config.keepColor());
                break;
            case DROP:
                underlayColorMap.put(itemId, config.dropColor());
                break;
            case CONTAINER:
                underlayColorMap.put(itemId, config.containerColor());
                break;
            case ALCH:
                underlayColorMap.put(itemId, config.alchColor());
                break;
            case CONSUME:
                underlayColorMap.put(itemId, config.consumeColor());
                break;
            case EQUIP:
                underlayColorMap.put(itemId, config.equipColor());
                break;
            case PROCESS:
                underlayColorMap.put(itemId, config.processColor());
                break;
            case CARGO_HOLD:
                underlayColorMap.put(itemId, config.cargoHoldColor());
                break;
            case OTHER:
                underlayColorMap.put(itemId, config.otherColor());
                break;
        }
    }

    public void rebuildUnderlayColorMap() {
        for (int item : underlayColorMap.keySet()) {
            LootItem lootItem = lootItemMap.get(item);
            setLootColor(item, lootItem.getLootCategory(), config);
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
}
