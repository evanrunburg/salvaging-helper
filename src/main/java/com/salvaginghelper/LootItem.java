package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import com.salvaginghelper.LootManager.LootOption;

import java.util.ArrayList;
import java.util.HashMap;
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
    //@Getter
    //private Boolean isNameOverridden = false;
    @Getter
    private String name;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<Integer, LootItem> itemIdMap;


    public LootItem(int itemId, ConfigManager configManager, ConcurrentHashMap<Integer, LootItem> itemMap,
                    LootOption defaultLootOption, boolean canContainer, boolean canConsume, boolean canEquip,
                    boolean canProcess, boolean canCargoHold, String newName) {

        this.itemId = itemId;
        this.configManager = configManager;
        this.itemIdMap = itemMap;

        LootOption savedLootOption = configManager.getConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, LootOption.class);
        lootCategory = (savedLootOption != null) ? savedLootOption : defaultLootOption;

        // Build out allowed categories - will save time later for combobox
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
        lootCategory = newCategory;
        configManager.setConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, newCategory);
    }

    public void reloadFromConfig() {
        LootOption savedCategory = configManager.getConfiguration(SalvagingHelperConfig.GROUP, "item_"+itemId, LootOption.class);

    }

    public ConcurrentHashMap<Integer, LootItem> getMap() {
        return itemIdMap;
    }

}
