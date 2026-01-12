package com.salvaginghelper;

import net.runelite.api.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.menus.MenuManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.salvaginghelper.LootManager.LootOption;

public class LeftClickManager {

    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    private final Client client;
    private final MenuManager menuManager;
    private final LootManager lootManager;
    private final ActionHandler actionHandler;

    private final int SWAP_DROP = 1;
    private final int SWAP_USE = 2;
    private final int SWAP_FILL = 3;

    private final ArrayList<String> boatOptionsToDeprio = new ArrayList<>(Arrays.asList("Set", "Check", "Empty",
            "Operate", "Check-ammunition", "Reset-ammunition", "Un-set", "Navigate", "Escape"));
    private final ArrayList<String> itemMenuOptionBlacklist = new ArrayList<>(Arrays.asList("Drink", "Wear", "Clean",
            "Apply", "Equip", "Inspect"));

    public ConcurrentHashMap<Integer, Boolean> deprioNPCMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Boolean> deprioObjMap = new ConcurrentHashMap<>();

    public ArrayList<Integer> npcsInTheWay = new ArrayList<>();

    public LeftClickManager(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, Client client,
                            MenuManager menuManager, LootManager lootManager, ActionHandler actionHandler) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.menuManager = menuManager;
        this.lootManager = lootManager;
        this.actionHandler = actionHandler;
    }

    public void process(Menu m) {

        MenuEntry[] menuEntries = m.getMenuEntries();
        int needSwap = 0;
        int dropIndex = -1;

        // Incremental loop instead of colon-for-loop for indices
        // Distinguish FILL and DROP for future-proofing reasons, even though we currently treat them identically
        for (int i=0; i<menuEntries.length; i++) {
            int k = processOneEntry(menuEntries[i]);
            if (k == SWAP_DROP || k == SWAP_FILL) {
                dropIndex = i;
                needSwap = k;
            }
            else if (k == SWAP_USE) {
                needSwap = k;
            }
        }
        if (needSwap == SWAP_DROP || needSwap == SWAP_FILL) {
            MenuEntry[] newMenu = swapEntryToTop(menuEntries, dropIndex);
            m.setMenuEntries(newMenu);

        } else if (needSwap == SWAP_USE) {
            int useIndex = getMenuOptionIndex(menuEntries, "Use");
            if (useIndex > 0) {
                MenuEntry[] newMenu = swapEntryToTop(menuEntries, useIndex);
                m.setMenuEntries(newMenu);
            }
        }

        // TODO - shift click configure?
    }

    public int processOneEntry(MenuEntry e) {
        String opt = e.getOption();
        int idf = e.getIdentifier();

        // Facilities
        if (boatOptionsToDeprio.contains(opt) ) {
            if (idf>0) {
                if (deprioObjMap.containsKey(idf) && deprioObjMap.get(idf)==true && config.hideFacilityLeftClick()) {
                    e.setDeprioritized(true);
                }
            }
        }

        // Shipwrecks
        else if (opt.equals("Inspect") && SalvagingHelperPlugin.activeShipwreckIds.contains(idf)) {
            if (config.hideShipwreckInspect()) {
                e.setDeprioritized(true);
            }
        }

        // Crewmates
        else if (opt.equals("Command")) {
            if (e.getNpc()!=null) {
                int npcId = e.getNpc().getId();
                if (deprioNPCMap.containsKey(npcId) && deprioNPCMap.get(npcId) && config.hideCrewmateLeftClick()) {
                    e.setDeprioritized(true);
                }
            }
        }

        // Other NPCs
        else if (opt.equals("Talk-to") || opt.equals("Dive")) {
            if (e.getNpc()!=null) {
                int npcId = e.getNpc().getId();
                if (deprioNPCMap.containsKey(npcId) && deprioNPCMap.get(npcId) && config.hideNpcInteract()) {
                    e.setDeprioritized(true);
                }
            }
        }

        // Ground items
        else if (e.getType()==MenuAction.GROUND_ITEM_THIRD_OPTION && config.hideGroundItems()){
            e.setDeprioritized(true);
        }

        // Inventory items
        else if (e.getItemId()>0 && e.getParam1()==9764864 && config.swapInvItemOptions()) {

            int id = e.getItemId();

            // Don't touch what isn't ours
            if (e.getType()==MenuAction.RUNELITE) {
                return 0;
            }

            // Salvage
            if (SalvagingHelperPlugin.salvageItemIds.contains(id)) {
                if (config.dropAllSalvage() && "Drop".equals(opt)) {
                    e.setType(MenuAction.CC_OP);
                    e.setDeprioritized(false);
                    return SWAP_DROP;
                } else {
                    return 0;
                }
            }

            // Loot containers
            LootContainer c = lootManager.getContainer(id);
            if (c != null) {
                if (lootManager.isContainerEnabled(c)) {
                    if (opt.equals("View") || opt.equals("Open") || opt.equals("Close") || opt.equals("Empty")
                            || opt.equals("Check")) {
                        // You might be thinking, "Let's set these menu entries to deprioritized here, so they show up
                        // later down on the list." This is a trap. If you deprioritize an entry that the game wants
                        // the item's left click to be, the game will create a *new* menu entry of its desired action
                        // *after* we finish our modifications, and insert it at the top. Even worse, the game will SAY
                        // in the top left corner of the client that the action it'll take when you click that item will
                        // be the one you want. It will be lying to you, you will spend many moments of your fleeting
                        // existence trying in vain to make this engine work in a reasonable way, and you will fail.
                        return 0;
                    } else {
                        // Case 1 - can click the container to fill (everything except rune pouch)
                        if (c.getHasLeftClickFill()) {
                            if (opt.equals("Fill")) {
                                return SWAP_FILL;
                            } else {
                                return 0;
                            }
                        } else {
                            // Case 2 - cannot (only rune pouch)
                            return SWAP_USE;
                        }
                    }
                }
                return 0;
            }

            // Everything past here should be loot items
            if (lootManager.getLootItem(id)==null) {
                return 0;
            }

            LootOption defaultLeftClick = lootManager.getLootItem(id).getLootCategory();

            // DROP
            if (defaultLeftClick.getMenuOptionWhitelist().contains(opt) && defaultLeftClick==LootOption.DROP) {
                // Drop option's default menu action type causes it to be auto-deprioritized, so swap it out
                e.setType(MenuAction.CC_OP);
                e.setDeprioritized(false);
                return SWAP_DROP;
            }

            // Deprioritize all other high-priority left click options that aren't the one we want - if we failed
            // every check above, we probably just want Use
            if (itemMenuOptionBlacklist.contains(opt) && !defaultLeftClick.getMenuOptionWhitelist().contains(opt)
                    && defaultLeftClick != LootOption.DROP) {
                //e.setDeprioritized(true);
                e.setType(MenuAction.CC_OP_LOW_PRIORITY);
                return SWAP_USE;
            }
        }
        return 0;
    }

    private MenuEntry[] swapEntryToTop(MenuEntry[] menuEntries, int indexToSwap) {
        // "Cancel" and "Examine" will always be menu entries; will use?
        if (indexToSwap == menuEntries.length - 1 || menuEntries.length < 3) { return null; }

        MenuEntry[] tempArray = new MenuEntry[menuEntries.length];
        for (int j=0; j<menuEntries.length; j++) {
            if (j==menuEntries.length - 1) {
                tempArray[j] = menuEntries[indexToSwap];
            }
            else if (j==indexToSwap) {
                tempArray[j] = menuEntries[menuEntries.length - 1];
            }
            else {
                tempArray[j] = menuEntries[j];
            }
        }
        return tempArray;
    }

    public int getMenuOptionIndex(MenuEntry[] menuEntries, String option) {
        for (int i=0; i<menuEntries.length; i++) {
            if (menuEntries[i].getOption().equals(option)) {
                return i;
            }
        }
        return -1;
    }

    public void deprio(GameObject obj) {

    }

    public void buildFacilityIgnoreList() {
        Boat boat = plugin.boat;
        // todo - make real facility objects for all
        deprioObjMap.put(59451, true);
        deprioObjMap.put(59698, true);
        deprioObjMap.put(59696, true);

        if (boat.getHelm()!=null && !deprioObjMap.containsKey(boat.getHelm().getId())) {
            deprioObjMap.put(boat.getHelm().getId(), true);
        }

        if (boat.getSails()!=null && !deprioObjMap.containsKey(boat.getSailsId())) {
            deprioObjMap.put(boat.getSailsId(), true);
        }

    }

    public void buildNPCIgnoreList() {
        if (!plugin.activeCrewmates.isEmpty()) {
            for (Crewmate mate : plugin.activeCrewmates) {
                if (mate != null) {
                    deprioNPCMap.put(mate.getNpcId(), true);
                }
            }
        }
        // Other NPCs will frequently get in the way of our salvaging - suppress those options too:
        // Selina-Kebbit Monkfish (15157) - Merchant Shipwrecks southwest of Brittle
        deprioNPCMap.put(NpcID.SAILING_CHARTING_MERMAID_GUIDE_3, true);
    }
}
