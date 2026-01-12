package com.salvaginghelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor @Getter
public enum LootContainer {
    LOG_BASKET("Log basket", List.of(28140, 28142, 18143, 28145), "logBasketEnabled", true,
            new ArrayList<>(List.of(1511, 1521, 6332, 6333, 10810))),
    PLANK_SACK("Plank sack", List.of(24882), "plankSackEnabled", true,
            new ArrayList<>(List.of(960, 8778, 8780, 8782))),
    GEM_BAG("Gem bag", List.of(24481, 12020), "gemBagEnabled", true,
            new ArrayList<>(List.of(1623, 1621, 1619, 1617))),
    HERB_SACK("Herb sack", List.of(13226, 24478), "herbSackEnabled", true,
            new ArrayList<>(List.of(207, 211, 213, 215, 217, 219, 2485, 3051))),
    FISH_BARREL("Fish barrel", List.of(25582, 25584, 25585, 25587), "fishBarrelEnabled",
            true, new ArrayList<>(List.of(331, 353, 359, 371, 377, 383, 7944, 32349))),
    SOULBEARER("Soul bearer", List.of(19634), "soulbearerEnabled", true,
            new ArrayList<>(List.of(13483))),
    RUNE_POUCH("Rune pouch", List.of(12791, 23650, 24416, 27281, 27509), "runePouchEnabled",
            false, new ArrayList<>(List.of(555, 556, 9075))),
    //BOLT_POUCH("Bolt pouch", List.of(9433)),
    SEED_BOX("Seed box", List.of(13639, 24482), "seedBoxEnabled", true,
            new ArrayList<>(List.of(5106, 5280, 5281, 5296, 5297, 5298, 5299, 5300, 5301, 5302, 5303, 5304,
                    22878, 22879, 31511, 31513, 31515, 31543, 31545, 31547, 31549, 31551))),
    COAL_BAG("Coal bag", List.of(12019, 24480), "coalBagEnabled", true,
            new ArrayList<>(List.of())),
    TACKLE_BOX("Tackle box", List.of(25580), "tackleBoxEnabled", true,
            new ArrayList<>(List.of(301, 307, 311, 313, 314, 11334))),
    HUNTSMAN_KIT("Huntsman's kit", List.of(29309), "huntsmanKitEnabled", true,
            new ArrayList<>(List.of(954))),
    REAGENT_POUCH("Reagent pouch", List.of(29996, 29998), "reagentPouchEnabled", true,
            new ArrayList<>(List.of()));

    private final String defaultName;
    private final List<Integer> itemIds;
    private final String configKey;
    private final Boolean hasLeftClickFill;
    private final ArrayList<Integer> eligibleItems;
}