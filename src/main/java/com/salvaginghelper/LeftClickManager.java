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
        for (int i=0; i<menuEntries.length; i++) {
            int k = processOneEntry(menuEntries[i]);
            if (k == SWAP_DROP) {
                dropIndex = i;
                needSwap = SWAP_DROP;
            }
            else if (k == SWAP_USE) {
                needSwap = SWAP_USE;
            }
        }
        if (needSwap == SWAP_DROP) {
            MenuEntry[] newMenu = swapEntryToTop(menuEntries, dropIndex);
            m.setMenuEntries(newMenu);
            return;
        } else if (needSwap == SWAP_USE) {
            int useIndex = getMenuOptionIndex(menuEntries, "Use");
            if (useIndex > 0) {
                MenuEntry[] newMenu = swapEntryToTop(menuEntries, useIndex);
                m.setMenuEntries(newMenu);
            }
        }

        // TODO - shift click configure
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
            //plugin.sendChatMessage("ping on npc id "+e.getNpc().getId(), false);
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
            // TODO - modify to allow containers to be handled - swap fill on containers

            // Support drop-all-salvage even if it's rancid
            if (SalvagingHelperPlugin.salvageItemIds.contains(id)) {
                if (config.dropAllSalvage() && "Drop".equals(opt)) {
                    e.setType(MenuAction.CC_OP);
                    e.setDeprioritized(false);
                    return SWAP_DROP;
                } else {
                    return 0;
                }
            }

            if (lootManager.getLootItem(id)==null) {
                return 0;
            }
            // Don't touch what isn't ours
            if (e.getType()==MenuAction.RUNELITE) {
                return 0;
            }

            LootOption defaultLeftClick = lootManager.getLootItem(id).getLootCategory();

            if (defaultLeftClick.getMenuOptionWhitelist().contains(opt) && defaultLeftClick==LootOption.DROP) {
                // Drop option's default menu action type causes it to be auto-deprioritized, so swap it out
                e.setType(MenuAction.CC_OP);
                e.setDeprioritized(false);
                return SWAP_DROP;
            }

            // Deprioritize all other high-priority left click options that aren't the one we want
            if (itemMenuOptionBlacklist.contains(opt) && !defaultLeftClick.getMenuOptionWhitelist().contains(opt)
                    && defaultLeftClick != LootOption.DROP) {
                e.setDeprioritized(true);
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
