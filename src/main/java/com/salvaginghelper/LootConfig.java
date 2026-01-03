package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;

public class LootConfig {

    @Getter
    private final int itemId;
    @Getter @Setter
    private LootManager.LootOption[] allowedOptions;
    @Getter @Setter
    private LootManager.LootOption defaultLootCategory;

    public LootConfig(int itemId, LootManager.LootOption[] allowedOptions, LootManager.LootOption defaultLootCategory) {
        this.itemId = itemId;
        this.allowedOptions = allowedOptions;
        this.defaultLootCategory = defaultLootCategory;
    }
}
