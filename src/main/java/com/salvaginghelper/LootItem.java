package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import com.salvaginghelper.LootManager.LootOption;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LootItem {

    @Getter @Setter
    private String leftClickOption;
    @Getter
    private final int itemId;
    @Getter
    private final ArrayList<LootOption> allowedOptions;
    @Getter
    private LootOption lootCategory;
    @Getter
    private final LootOption defaultLootCategory;
    @Getter
    private final String name;
    @Getter
    private final LootContainer container;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<Integer, LootItem> itemIdMap;


    public LootItem(int itemId, ConfigManager configManager, ConcurrentHashMap<Integer, LootItem> itemMap,
                    LootOption defaultLootOption, boolean canContainer, boolean canConsume, boolean canEquip,
                    boolean canProcess, boolean canCargoHold, String newName, LootContainer container) {

        this.defaultLootCategory = defaultLootOption;
        this.itemId = itemId;
        this.configManager = configManager;
        this.itemIdMap = itemMap;
        this.container = container;

        LootOption savedLootOption = configManager.getConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, LootOption.class);
        lootCategory = (savedLootOption != null) ? savedLootOption : defaultLootOption;

        // ArrayList insertion order is preserved, so we need to do this in the right order for the sidepanel
        ArrayList<LootOption> allowedTemp = new ArrayList<>();
        allowedTemp.add(lootCategory);
        if (!allowedTemp.contains(defaultLootOption)) { allowedTemp.add(defaultLootOption); }
        if (!allowedTemp.contains(LootOption.CONTAINER) && canContainer) { allowedTemp.add(LootOption.CONTAINER); }
        if (!allowedTemp.contains(LootOption.CONSUME) && canConsume) { allowedTemp.add(LootOption.CONSUME); }
        if (!allowedTemp.contains(LootOption.EQUIP) && canEquip) { allowedTemp.add(LootOption.EQUIP); }
        if (!allowedTemp.contains(LootOption.PROCESS) && canProcess) { allowedTemp.add(LootOption.PROCESS); }
        if (!allowedTemp.contains(LootOption.CARGO_HOLD) && canCargoHold) { allowedTemp.add(LootOption.CARGO_HOLD); }
        allowedTemp.add(LootOption.KEEP);
        allowedTemp.add(LootOption.DROP);
        allowedTemp.add(LootOption.ALCH);
        allowedTemp.add(LootOption.OTHER);
        allowedOptions = allowedTemp;

        itemIdMap.put(itemId, this);

        this.name = newName;
    }

    public void updateLootCategory(LootOption newCategory) {
        this.lootCategory = newCategory;
        configManager.setConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, newCategory);
    }

    public void reloadFromConfig() {
        LootOption savedCategory = configManager.getConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, LootOption.class);

    }
}
