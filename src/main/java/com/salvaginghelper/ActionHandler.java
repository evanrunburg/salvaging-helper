package com.salvaginghelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.salvaginghelper.Crewmate.Activity;
import com.salvaginghelper.LootManager.LootOption;
import net.runelite.client.config.ConfigManager;

public class ActionHandler {

    //region Enum - Instruction
    public enum Instruction {
        SAIL_TO_SHIPWRECK,
        GET_BOAT_MOVING,
        FIND_NEW_SHIPWRECK,
        TELL_CREWMATE_TO_SALVAGE,
        GO_SALVAGE,
        SORT_SALVAGE,
        PROCESS_SALVAGE,
        DEPOSIT_IN_CARGO_HOLD,
        REBOOST,
        JUST_CHILLING
    }
    //endregion

    //region Enum - Salvaging Mode
    @RequiredArgsConstructor @Getter
    public enum SalvageMode {
        SALVAGE_AND_SORT("Salvage + Sort"),
        SALVAGE_ONLY("Salvage Only"),
        SORT_ONLY("Sort Only");

        private final String name;

        @Override
        public String toString() { return name; }
    }
    //endregion

    //region Variable declarations
    public static HashMap<Integer, Activity> mapAnimToActivity = new HashMap<>();
    public ConcurrentHashMap<NPC, Color> npcHighlightMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<GameObject, Color> objectHighlightMap = new ConcurrentHashMap<>();
    public List<GameObject> activeShipwrecks = new ArrayList<>();
    public List<GameObject> inactiveShipwrecks = new ArrayList<>();
    private final SalvagingHelperObjectOverlay objectOverlay;
    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    public boolean flagRebuild = false;

    private Instruction currentInstruction = Instruction.JUST_CHILLING;

    private ItemContainer inv;
    private Boat boat;
    public Color clear = new Color(0, 0, 0, 0);

    //region Inventory variables
    // General
    @Setter
    private boolean inventoryWasUpdated = false;
    private boolean invHasSalvage = false;
    // Containers
    private HashMap<Integer, LootContainer> currentContainers = new HashMap<>();
    private ArrayList<Integer> currentContainerableItems = new ArrayList<>();
    private boolean invHasContainerLogBasket = false;
    private int logBasketItemId = -1;
    private boolean isLogBasketFull = false;
    private boolean containsDropLogs = false;
    private boolean invHasContainerPlankSack = false;
    private boolean isPlankSackFull = false;
    private boolean containsDropPlanks = false;
    private boolean invHasContainerGemBag = false;
    private boolean isGemBagFull = false;
    private boolean invHasContainerHerbSack = false;
    public boolean isHerbSackFull = false;
    private boolean containsDropHerbs = false;
    private boolean invHasContainerFishBarrel = false;
    private boolean isFishBarrelFull = false;
    private boolean containsDropFish = false;
    private boolean invHasContainerSoulbearer = false;
    private boolean isSoulbearerEmpty = false;
    private boolean invHasContainerRunePouch = false;
    private boolean isRunePouchFull = false;
    //private boolean invHasContainerBoltPouch = false;
    //private boolean isBoltPouchFull = false;
    private boolean invHasContainerSeedBox = false;
    private boolean isSeedBoxFull = false;
    private boolean containsDropSeeds = false;
    private boolean invHasContainerCoalBag = false;
    private boolean isCoalBagFull = false;
    private boolean invHasContainerTackleBox = false;
    private boolean isTackleBoxFull = false;
    private boolean invHasContainerHuntsmanKit = false;
    private boolean isHuntsmanKitFull = false;
    private boolean invHasContainerReagentPouch = false;
    private boolean isReagentPouchFull = false;

    private boolean containsSalvage = false;
    private boolean isFull = false;
    private boolean containsKeepLoot = false;
    private boolean containsDroppableLoot = false;
    private boolean containsAlchLoot = false;
    private boolean containsContainerableLoot = false;
    private boolean containsConsumableLoot = false;
    private boolean containsEquippableLoot = false;
    private boolean containsProcessableLoot = false;
    private boolean containsCargoHoldLoot = false;
    private boolean containsOtherLoot = false;
    //endregion

    @Setter
    private ItemContainer cargoHold;
    private ArrayList<Item> cargoContainerItems = new ArrayList<>();
    private int cargoHoldCapacity;
    public boolean cargoHoldNeedsUpdate = false;
    public boolean cargoHoldFull;
    private long cargoHoldSalvageCount;
    private int inactiveHooks;
    private int activeHooks;
    @Getter
    private int closestWreckDist;

    private final Client client;
    private final LootManager lootManager;
    private final ConfigManager configManager;
    //endregion

    //region Constructor
    public ActionHandler(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, Client client, LootManager lootManager,
                         List<Crewmate> activeCrewmates, SalvagingHelperObjectOverlay objOverlay, Boat boat,
                         ConfigManager configManager) {
        // Initialize maps
        this.plugin = plugin;
        this.config = config;
        this.objectOverlay = objOverlay;
        this.boat = boat;
        this.lootManager = lootManager;
        this.client = client;
        this.configManager = configManager;
        buildAnimationMap();
        buildCrewmateMap(activeCrewmates);
    }
    //endregion


    //region LOGIC (determineState)
    public Instruction determineState(SalvagingHelperPlugin plugin, Client client) {

        Instruction newInstruction = currentInstruction;

        if (flagRebuild) {
            rebuild(plugin, client);
            flagRebuild = false;
        }

        // Set up to perform the logic all at once cleanly
        processInventoryItems();
        processCargoHold();
        SalvageMode mode = config.salvageMode();
        activeHooks = countHooks("Active");
        inactiveHooks = countHooks("Inactive");
        int hookCount = activeHooks + inactiveHooks;
        int closestActiveShipwreckDistance = closestActiveWreckDist(client);
        int closestInactiveShipwreckDistance = closestInactiveWreckDist(client);
        closestWreckDist = closestWreckDist(client);

        // Always evaluate these
        // Salvaging station
        if (invHasSalvage && !config.dropAllSalvage()) {
            highlight(boat.getSalvagingStation(), Color.GREEN);
        } else {
            highlight(boat.getSalvagingStation(), clear);
        }
        // Cargo hold
        highlight(boat.getCargoHold(), clear);
        if ((containsCargoHoldLoot && boat.getCargoHold()!=null)) {
            //plugin.sendChatMessage("Highlighting cargo hold (Has Cargo Loot)", true);
            highlight(boat.getCargoHold(), config.cargoHoldColor());
        }
        // Ready to grab more salvage out of the cargo hold
        if (!invHasSalvage && !containsDroppableLoot && !containsAlchLoot && !containsContainerableLoot &&
                !containsConsumableLoot && !containsEquippableLoot && !containsProcessableLoot &&
                (plugin.playerCurrentActivity==Activity.SORTING_SALVAGE ||
                    (plugin.playerCurrentActivity==Activity.EXTRACTING && plugin.playerLastActivity==Activity.SORTING_SALVAGE))) {
            highlight(boat.getCargoHold(), config.cargoHoldColor());
            //plugin.sendChatMessage("Highlighting cargo hold (Ready for More Salvage)", true);
            // TODO - add condition that there's enough salvage in the cargo hold?
        }

        if (cargoHoldFull) {
            highlight(boat.getCargoHold(), Color.RED);
            // TODO - fire idle notification?
            if (mode != SalvageMode.SALVAGE_ONLY) {

            }
        }

        // Needs to move to new spot?
        // all hooks inactive, boat not moving, closest shipwreck
        // TODO: # crewmates assigned to salvage > 0
        if (inactiveHooks>0 && boat.getBoatMoveMode()==0 && currentInstruction!=Instruction.SAIL_TO_SHIPWRECK
                    && closestInactiveShipwreckDistance < 1400) { // && closestActiveShipwreckDistance>1200
            newInstruction = Instruction.SAIL_TO_SHIPWRECK;
            plugin.sendIdleNotification("Move to a new shipwreck!");
        }
        if (inactiveHooks==0 && plugin.playerCurrentActivity!=Activity.IDLE) { //  && !invHasSalvage?
            newInstruction = Instruction.JUST_CHILLING;
        }

        objectOverlay.setGameObjHighlights(objectHighlightMap);
        objectOverlay.setNPCHighlights(npcHighlightMap);

        currentInstruction = newInstruction;
        return newInstruction;
    }
    //endregion


    //region Processing
    public void processPlayerAnimation(AnimationChanged event, Player player, int animationId) {
        if (animationId==plugin.playerCurrentAnimation){ return; }
        Activity mappedActivity = mapAnimToActivity.get(animationId);
        if (mappedActivity == null) {
            plugin.sendChatMessage("Unhandled player animation: "+animationId, false);
        } else {
            plugin.playerLastAnimation = plugin.playerCurrentAnimation;
            if (mappedActivity==plugin.playerCurrentActivity){ return; } else {
                plugin.playerLastActivity = plugin.playerCurrentActivity;
                plugin.playerCurrentActivity = mappedActivity;
            }
        }
        return;
    }

    public void processCrewmateAnimation(SalvagingHelperPlugin plugin, AnimationChanged event, Crewmate crewmate, int animationId) {
        Activity mappedActivity = mapAnimToActivity.get(animationId);
        if (mappedActivity == null) {
            plugin.sendChatMessage("Unhandled animation "+animationId+" for "+crewmate.getName(), false);
            return;
        } else if (animationId==crewmate.getCurrentAnimation() || mappedActivity==crewmate.getCurrentStatus()) {
            return;
        } else {
            if (animationId==-1) {
                // TODO: check if the facility they were at has an idle animation. Change nothing if not.
                return;
            }
            //plugin.sendChatMessage(crewmate.getName()+" activity from "+crewmate.getCurrentStatus()+" ("+crewmate.getCurrentAnimation()+") to "+mappedActivity.name()+" for new animation "+animationId);
            crewmate.setLastAnimation(crewmate.getCurrentAnimation());
            crewmate.setCurrentAnimation(animationId);
            crewmate.setLastStatus(crewmate.getCurrentStatus());
            crewmate.setCurrentStatus(mappedActivity);
        }
        return;
    }

    public void buildAnimationMap() {

        // TODO: replace this with hashmap?

        int[] salvagingAnims = new int[]{ 13576, 13577, 13583, 13584, 13598 };
        int[] sortingAnims = new int[]{ 13599 };
        int[] processingAnims = new int[]{ 713 }; // alching, crafting, etc?
        int[] cannonAnims = new int[]{ 13325, 13326, 13327 };
        int[] trawlingAnims = new int[]{ 13451, 13452, 13470, 13471, 13474 };
        int[] extractorAnims = new int[]{ 13176, 13178 };
        int[] repairAnims = new int[]{ 13252 };
        int[] steeringAnims = new int[]{ 13351, 13352, 13353, 13354, 13355, 13362, 13363, 13364, 13365, 13366 };
        int[] otherFishingAnims = new int[]{ 13436 };
        int[] otherAnims = new int[]{ 3660, 13310, 13317, 13318 };
        int[] movingAnims = new int[]{  };
        int[] idleAnims = new int[]{ -1, 866, 868, 2106, 7537, 13578, 13585 };
        int[] notSailingRelated = new int[]{ 714, 725, 829 };

        for (int anim : salvagingAnims) { mapAnimToActivity.put(anim, Activity.SALVAGING); }
        for (int anim : sortingAnims) { mapAnimToActivity.put(anim, Activity.SORTING_SALVAGE); }
        for (int anim : processingAnims) { mapAnimToActivity.put(anim, Activity.PROCESSING_SALVAGE); }
        for (int anim : cannonAnims) { mapAnimToActivity.put(anim, Activity.CANNON); }
        for (int anim : trawlingAnims) { mapAnimToActivity.put(anim, Activity.TRAWLING); }
        for (int anim : extractorAnims) { mapAnimToActivity.put(anim, Activity.EXTRACTING); }
        for (int anim : repairAnims) { mapAnimToActivity.put(anim, Activity.SORTING_SALVAGE); }
        for (int anim : steeringAnims) { mapAnimToActivity.put(anim, Activity.STEERING); }
        for (int anim : otherFishingAnims) { mapAnimToActivity.put(anim, Activity.FISHING_OTHER); }
        for (int anim : otherAnims) { mapAnimToActivity.put(anim, Activity.OTHER); }
        for (int anim : movingAnims) { mapAnimToActivity.put(anim, Activity.MOVING); }
        for (int anim : idleAnims) { mapAnimToActivity.put(anim, Activity.IDLE); }
    }

    public void buildCrewmateMap(List<Crewmate> crewmates) {
        for (Crewmate crewmate : crewmates) {
            if (crewmate != null && crewmate.getCrewmateNpc() != null) {
                npcHighlightMap.put(crewmate.getCrewmateNpc(), clear);
            }
        }
    }

    // TODO - reimplement with stream()
    public void collectShipwrecks(Client client, boolean rebuild) {
        // GameObject transmogrification makes things wonky and means we can't rely on our regular event
        // listeners to tell us when shipwrecks are and aren't there/active
        Tile[][] salvagingTiles = client.getTopLevelWorldView().getScene().getExtendedTiles()[0];
        for (Tile[] rowOfTiles : salvagingTiles) {
            if (rowOfTiles==null) { continue; } // java is a Perfect Language with No Flaws
            for (Tile singleTile : rowOfTiles) {
                if (singleTile==null) { continue; }
                for (GameObject gameObject : singleTile.getGameObjects()) {
                    if (gameObject==null) { continue; }
                    if (plugin.activeShipwreckIds.contains(gameObject.getId()) && !objectHighlightMap.containsKey(gameObject)) {
                        processObject(gameObject, plugin.ObjectTable, plugin.boat);

                        // Clear out the current inactive shipwreck unless we're rebuilding both lists from scratch
                        LocalPoint loc = gameObject.getLocalLocation();
                        if (!rebuild) {
                            for (GameObject depletedWreck : inactiveShipwrecks) {
                                if (depletedWreck.getLocalLocation().equals(loc)) {
                                    objectHighlightMap.remove(depletedWreck);
                                    inactiveShipwrecks.remove(depletedWreck);
                                }
                            }
                        }
                    } else if (plugin.inactiveShipwreckIds.contains(gameObject.getId()) && rebuild) {
                        processObject(gameObject, plugin.ObjectTable, plugin.boat);
                    }
                }
            }
        }
    }

    public int getObjectAnimation(GameObject obj) {
        if (obj.getRenderable() instanceof DynamicObject) {
            if (((DynamicObject) obj.getRenderable()).getAnimation() != null) {
                return ((DynamicObject) obj.getRenderable()).getAnimation().getId();
            }
        }
        return -1;
    }

    public void processObject(GameObject obj, LookupTable map, Boat boat) {
        if (obj != null) {
            switch (map.toVal(obj.getId())) {
                case "1": // Shipwrecks (active)
                    objectHighlightMap.put(obj, clear);
                    if (!activeShipwrecks.contains(obj)) { activeShipwrecks.add(obj); }
                    return;
                case "2": // Shipwrecks (inactive)
                    objectHighlightMap.put(obj, clear);
                    if (!inactiveShipwrecks.contains(obj)) { inactiveShipwrecks.add(obj); }
                    return;
                case "3": // Salvaging hooks
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear); // Color.PINK
                        boat.addHook(obj);
                    }
                    return;
                case "4": // Salvaging station
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.setSalvagingStation(obj);
                    }
                    return;
                case "5": // Crystal extractor
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.setCrystalExtractor(obj);
                    }
                    return;
                case "6": // Cargo hold
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.addHold(obj);
                    }
                    return;
                case "7": // Cannons
                    //objectHighlightMap.put(obj, new Color(255, 0, 0));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                    }
                    // TODO: add to boat
                    return;
                case "8": // Kegs
                    //objectHighlightMap.put(obj, new Color(102, 51, 0));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.addKeg(obj);
                    }
                    return;
                case "9": // Teleports
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                    }
                    // TODO: add to boat?
                    return;
                case "10": // Wind/gale catcher
                    //objectHighlightMap.put(obj, new Color(0, 153, 153));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                    }
                    // TODO: add to boat
                    return;
                case "11": // Misc
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                    }
                    return;
                case "12": // Helms
                    //objectHighlightMap.put(obj, new Color(153, 204, 0));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.addHelm(obj);
                    }
                    return;
                case "13": // sails
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.addSails(obj);
                    }
                    return;
                default:
                    return;
            }
        }
    }
    //endregion

    public boolean isOurs(GameObject object) {
        if (object.getWorldView()==plugin.getClient().getLocalPlayer().getWorldView()) {
            return true;
        } else {
            return false;
        }
    }

    // Loading into a new top-level worldview recalculates all our local coordinates and ruins our overlays,
    // so we need to relocate and rebuild each of those entities in the new coordinate system.
    @SuppressWarnings("unchecked")
    public void rebuild(SalvagingHelperPlugin plugin, Client client) {

        List<GameObject> activeShipwrecksCopy = new ArrayList<>(activeShipwrecks);
        List<GameObject> inactiveShipwrecksCopy = new ArrayList<>(inactiveShipwrecks);

        for (GameObject wreck : activeShipwrecksCopy) { objectHighlightMap.remove(wreck); }
        for (GameObject wreck : inactiveShipwrecksCopy) { objectHighlightMap.remove(wreck); }
        objectOverlay.setGameObjHighlights(objectHighlightMap);
        activeShipwrecks.clear();
        inactiveShipwrecks.clear();
        collectShipwrecks(client, true);
    }

    public void deleteObject(GameObject obj) {
        objectHighlightMap.remove(obj);
        activeShipwrecks.remove(obj);
        inactiveShipwrecks.remove(obj);
    }

    public void deleteNPC(NPC npc) {
        npcHighlightMap.remove(npc);
    }

    public void dumpGameObjects(Client client) {
        for (GameObject obj : objectHighlightMap.keySet()) {
            //plugin.debugLog(Arrays.asList(obj.getId() + "", client.getObjectDefinition(obj.getId()).getName(), obj.getLocalLocation().toString(), obj.getWorldLocation().toString(), obj.getSceneMaxLocation().toString(), obj.getSceneMinLocation().toString(), getObjectAnimation(obj) + ""), plugin);
        }

    }

    //region Helper Functions
    private void processInventoryItems() {

//        Item container class:
//                contains(itemId) -> boolean
//                count() -> # filled slots
//                count(itemId) -> int
//                find(itemId) -> first index
//                size() -> # slots
//        Item class:
//                getId(), getQuantity()

        if (!inventoryWasUpdated) { return; }

        Item[] items = inv.getItems();
        invHasSalvage = SalvagingHelperPlugin.salvageItemIds.stream().anyMatch(i -> inv.contains(i));

        // LootOption categories present
        List<LootItem> invItemStream = Stream.of(inv.getItems()).map(Item::getId).map(lootManager::getLootItem)
                .filter(Objects::nonNull).collect(Collectors.toList());
        containsKeepLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.KEEP);
        containsDroppableLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.DROP);
        containsAlchLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.ALCH);
        containsContainerableLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.CONTAINER);
        containsConsumableLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.CONSUME);
        containsEquippableLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.EQUIP);
        containsProcessableLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.PROCESS);
        containsCargoHoldLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.CARGO_HOLD);
        containsOtherLoot = invItemStream.stream().anyMatch(x -> x.getLootCategory()==LootOption.OTHER);

        // Containers
        for (LootContainer c : LootContainer.values()) {
            highlight(c, clear);
        }
        currentContainers.clear();
        currentContainerableItems.clear();
        invHasContainerLogBasket = invHasContainer(LootContainer.LOG_BASKET, inv);
        invHasContainerPlankSack = invHasContainer(LootContainer.PLANK_SACK, inv);
        invHasContainerGemBag = invHasContainer(LootContainer.GEM_BAG, inv);
        invHasContainerHerbSack = invHasContainer(LootContainer.HERB_SACK, inv);
        invHasContainerFishBarrel = invHasContainer(LootContainer.FISH_BARREL, inv);
        invHasContainerSoulbearer = invHasContainer(LootContainer.SOULBEARER, inv);
        invHasContainerRunePouch = invHasContainer(LootContainer.RUNE_POUCH, inv);
        invHasContainerSeedBox = invHasContainer(LootContainer.SEED_BOX, inv);
        invHasContainerCoalBag = invHasContainer(LootContainer.COAL_BAG, inv);
        invHasContainerTackleBox = invHasContainer(LootContainer.TACKLE_BOX, inv);
        invHasContainerHuntsmanKit = invHasContainer(LootContainer.HUNTSMAN_KIT, inv);
        invHasContainerReagentPouch = invHasContainer(LootContainer.REAGENT_POUCH, inv);


        for (Item item : items) {
            int id = item.getId();
            if (isSalvagingLoot(id)) {
                if (getLootCategory(id) == LootOption.CONTAINER) {
                    LootContainer container = lootManager.getContainerFromEligibleItem(id);

                    // If we have an item's container and it's we can left-click to fill, highlight that to streamline
                    if (invHasContainer(container, inv) && lootManager.isContainerEnabled(container)) { // Updates currentContainers
                        if (container.getHasLeftClickFill()) {
                            highlight(id, clear);
                        } else {
                            highlight(id, config.containerColor());
                        }
                    } else {
                        highlight(id, config.containerColor());
                    }
                }
            }
        }

        // Highlight qualifying containers
        for (int itemId : currentContainers.keySet()) {
            LootContainer c = currentContainers.get(itemId);
            if (invContainsEligibleLoot(c, inv) && !invContainsIneligibleLoot(c, inv)) {
                lootManager.setColor(itemId, config.containerColor());
            }
        }

        // Highlight cargo hold loot
        if (containsCargoHoldLoot && !containsDroppableLoot) {
            // TODO - finish
        }

        // Highlight salvage
        highlightAllSalvage(false, null);
        if (config.dropAllSalvage()) {
            highlightAllSalvage(true, config.dropColor());
        } else if (!containsDroppableLoot) {
            highlightAllSalvage(true, clear);
        }

        // Reset flag
        inventoryWasUpdated = false;
    }

    public LootOption getLootCategory(int itemId) {
        LootItem lootItem = lootManager.lootItemMap.get(itemId);
        if (lootItem != null) {
            return lootItem.getLootCategory();
        }
        return null;
    }

    public Boolean isSalvagingLoot(int itemId) {
        if (itemId > 0 && lootManager.lootItemMap.containsKey(itemId)){
            return true;
        } else {
            return false;
        }
    }

    public void setInventory(ItemContainer newInventory) {
        inv = newInventory;
        inventoryWasUpdated = true;
    }

    private int countHooks(String type) {
        int activeHooks = 0;
        int inactiveHooks = 0;
        ArrayList<Integer> idleHookAnims = new ArrayList<>(Arrays.asList(13575, 13582));
        // TODO - figure out real IDs to use
        //13565, 13567, 13572, 13579)); //13574, 13581 wrong?
        if (boat.getHookPort() != null) {
            if (idleHookAnims.contains(getObjectAnimation(boat.getHookPort()))) {
                inactiveHooks++;
            } else { activeHooks++; }
        }
        if (boat.getHookStarboard() != null) {
            if (idleHookAnims.contains(getObjectAnimation(boat.getHookStarboard()))) {
                inactiveHooks++;
            } else { activeHooks++; }
        }
        if (type.equals("Active")){ return activeHooks; }
        else if (type.equals("Inactive")){ return inactiveHooks; }
        else { return -1; }
    }

    private int closestActiveWreckDist(Client client) {
        int closestActiveShipwreckDistance = 100000;
        if (activeShipwrecks.isEmpty()) {
            return -1;
        }
        for (GameObject wreck : activeShipwrecks) {
            int dist = distanceToPlayer(wreck, boat, client);
            if (dist < closestActiveShipwreckDistance) {
                closestActiveShipwreckDistance = dist;
            }
        }
        return closestActiveShipwreckDistance;
    }

    private int closestInactiveWreckDist(Client client) {
        int closestInactiveShipwreckDistance = 100000;
        if (inactiveShipwrecks.isEmpty()) {
            return 100000;
        }
        for (GameObject wreck : inactiveShipwrecks) {
            int dist = distanceToPlayer(wreck, boat, client);
            if (dist < closestInactiveShipwreckDistance){
                closestInactiveShipwreckDistance = dist;
            }
        }
        return closestInactiveShipwreckDistance;
    }

    public int closestWreckDist(Client client) {
        int closestShipwreckDistance = 100000;
        if (inactiveShipwrecks.isEmpty() && activeShipwrecks.isEmpty()) {
            return 100000;
        }
        for (GameObject wreck : inactiveShipwrecks) {
            int dist = distanceToPlayer(wreck, boat, client);
            if (dist < closestShipwreckDistance){
                closestShipwreckDistance = dist;
            }
        }
        for (GameObject wreck : activeShipwrecks) {
            int dist = distanceToPlayer(wreck, boat, client);
            if (dist < closestShipwreckDistance) {
                closestShipwreckDistance = dist;
            }
        }

        return closestShipwreckDistance;
    }

    private int distanceToPlayer(GameObject obj, Boat theBoat, Client theClient) {
        if (obj == null | theBoat == null | theClient == null | theClient.getLocalPlayer() == null | theBoat.getBoatEntity() == null) {
            return -1;
        }
        LocalPoint playerTranslated = theBoat.getBoatEntity().transformToMainWorld(theClient.getLocalPlayer().getLocalLocation());
        return obj.getLocalLocation().distanceTo(playerTranslated);
    }

    private void processCargoHold() {
        if (!cargoHoldNeedsUpdate) { return; }
        cargoContainerItems.clear();
        cargoContainerItems.addAll(List.of(cargoHold.getItems()));
        cargoHoldCapacity = boat.getCargoHoldCapacity();
        cargoHoldSalvageCount = cargoContainerItems.stream().filter(x ->
                SalvagingHelperPlugin.salvageItemIds.contains(x.getId())).count();
        cargoHoldFull = (cargoHoldCapacity == cargoHold.count());
        cargoHoldNeedsUpdate = false;
    }

    public void highlight(int itemId, Color newColor) {
        lootManager.setColor(itemId, newColor);
    }

    public void highlight(GameObject object, Color newColor) {
        if (object!=null) {
            objectHighlightMap.put(object, newColor);
        }
    }

    public void highlight(NPC npc, Color newColor) {
        if (npc!=null) {
            npcHighlightMap.put(npc, newColor);
        }
    }

    private void highlight(LootContainer lootContainer, Color newColor) {
        if (lootContainer!=null) {
            for (int itemId : lootContainer.getItemIds()) {
                lootManager.setColor(itemId, newColor!=null ? newColor : config.containerColor());
            }
        }
    }

    // Checks both that inventory contains the physical item AND that it's enabled in config
    public Boolean invHasContainer(LootContainer container, ItemContainer inv) {
        List<Integer> containerIds = container.getItemIds();
        for (int itemId : containerIds) {
            if (inv.contains(itemId)) {
                currentContainers.put(itemId, container);
                return true;
            }
        }
        return false;
    }

    public Boolean invContainsEligibleLoot(LootContainer container, ItemContainer inv) {
        List<Integer> itemIds = container.getEligibleItems();
        for (int itemId : itemIds) {
            if (inv.contains(itemId) && lootManager.getLootItem(itemId).getLootCategory()==LootOption.CONTAINER) {
                return true;
            }
        }
        return false;
    }

    public Boolean invContainsIneligibleLoot(LootContainer container, ItemContainer inv) {
        List<Integer> itemIds = container.getEligibleItems();
        for (int itemId : itemIds) {
            if (inv.contains(itemId)) {
                LootOption category = lootManager.lootItemMap.get(itemId).getLootCategory();
                if (category != LootOption.CONTAINER && category != LootOption.KEEP) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dumpActionHandlerVars() {
        plugin.sendChatMessage("invHasSalvage: "+Boolean.toString(invHasSalvage), false);
        plugin.sendChatMessage("containsCargoHoldLoot: "+Boolean.toString(containsCargoHoldLoot), false);
        plugin.sendChatMessage("containsDroppableLoot: "+Boolean.toString(containsDroppableLoot), false);
        plugin.sendChatMessage("containsAlchLoot: "+Boolean.toString(containsAlchLoot), false);
        plugin.sendChatMessage("containsContainerableLoot: "+Boolean.toString(containsContainerableLoot), false);
        plugin.sendChatMessage("containsConsumableLoot: "+Boolean.toString(containsConsumableLoot), false);
        plugin.sendChatMessage("containsEquippableLoot: "+Boolean.toString(containsEquippableLoot), false);
        plugin.sendChatMessage("containsProcessableLoot: "+Boolean.toString(containsProcessableLoot), false);
        plugin.sendChatMessage("inactiveHooks: "+inactiveHooks, false);
        plugin.sendChatMessage("activeHooks: "+activeHooks, false);
        plugin.sendChatMessage("closestActiveShipwreckDistance: "+ closestActiveWreckDist(client), false);
        plugin.sendChatMessage("closestInactiveShipwreckDistance: "+closestInactiveWreckDist(client), false);
        plugin.sendChatMessage("cargoHoldCapacity: "+cargoHoldCapacity, false);
        plugin.sendChatMessage("cargoHold.count(): "+cargoHold.count(), false);
    }

    // Pass null for color to use CargoHold category default
    public void highlightAllSalvage(Boolean highlight, Color override) {
        for (int itemId : SalvagingHelperPlugin.salvageItemIds) {
            if (highlight) {
                if (override != null) {
                    highlight(itemId, override);
                } else {
                    highlight(itemId, config.cargoHoldColor());
                }
            } else {
                highlight(itemId, clear);
            }
        }
    }

    public int msSinceInteract() {

        return -1;
    }
    //endregion
}
