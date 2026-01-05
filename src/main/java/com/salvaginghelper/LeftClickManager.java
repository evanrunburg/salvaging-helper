package com.salvaginghelper;

import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.menus.MenuManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LeftClickManager {

    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    private final Client client;
    private final MenuManager menuManager;
    private final LootManager lootManager;
    private final ActionHandler actionHandler;

    private final ArrayList<String> annoyingMenuOptions = new ArrayList<>(Arrays.asList("Set", "Check", "Empty",
            "Operate", "Check-ammunition", "Reset-ammunition", "Un-set", "Navigate", "Escape"));

    public ConcurrentHashMap<Integer, Boolean> deprioNPCMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Boolean> deprioObjMap = new ConcurrentHashMap<>();

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
        ArrayList<MenuEntry> entries = new ArrayList<>();
        entries.addAll(List.of(m.getMenuEntries()));

        for (MenuEntry e : m.getMenuEntries()) {
            processOneEntry(e);
        }

        // at the end, add shift click configure
    }

    public void processOneEntry(MenuEntry e) {
        // Just want to deprioritize options we don't like
        String opt = e.getOption();
        int idf = e.getIdentifier();
        if (annoyingMenuOptions.contains(opt) ) {
            if (idf>0) {
                if (deprioObjMap.containsKey(idf) && deprioObjMap.get(idf)==true) {
                    e.setDeprioritized(true);
                }
            }
            // Shipwrecks
        } else if (idf==60478 && opt.equals("Inspect")) {
            e.setDeprioritized(true);
        } else if (opt.equals("Command")) {
            //plugin.sendChatMessage("Hit command line", true);
            //plugin.sendChatMessage(e.toString(), true);
            //plugin.sendChatMessage("Contains idf key: "+ Boolean.toString(deprioNPCMap.containsKey(idf)), true);
            //plugin.sendChatMessage("Maps to true: "+ Boolean.toString(deprioNPCMap.get(idf)), true);
            if (e.getNpc()!=null) {
                int npcId = e.getNpc().getId();
                if (deprioNPCMap.containsKey(npcId) && deprioNPCMap.get(npcId)) {
                    e.setDeprioritized(true);
                    //plugin.sendChatMessage("Deprio'd", true);
                }
            }

        }
    }

    private void processObjectMenu() {
        return;
    }

    private void processInvItemMenu() {
        return;
    }

    private void processGroundItemMenu() {
        return;
    }

    private void processNPCMenu() {
        return;
    }

    public Menu prioritizeDrop(Menu m) {

        return m;
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
        for (Crewmate mate : plugin.activeCrewmates) {
            deprioNPCMap.put(mate.getNpcId(), true);
        }
    }


}
