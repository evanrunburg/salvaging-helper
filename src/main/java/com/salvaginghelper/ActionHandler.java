package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.CharSetUtils.count;

public class ActionHandler {

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


    //region Variable declarations
    public static HashMap<Integer, Crewmate.Activity> mapAnimToActivity = new HashMap<>();
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
    private Boat activeBoat;
    public Color clear = new Color(0, 0, 0, 0);

    // Inventory variables
    // Containers
    @Setter
    private boolean inventoryWasUpdated = false;
    private boolean invHasContainerLogBasket = false;
    private boolean isLogBasketFull = false;
    private boolean containsDropLogs = false;
    private boolean invHasContainerPlankSack = false;
    private boolean isPlankSackFull = false;
    private boolean containsDropPlanks = false;
    private boolean invHasContainerGemBag = false;
    private boolean isGemBagFull = false;
    private boolean invHasContainerHerbSack = false;
    private boolean isHerbSackFull = false;
    private boolean containsDropHerbs = false;
    private boolean invHasContainerFishBarrel = false;
    private boolean isFishBarrelFull = false;
    private boolean containsDropFish = false;
    private boolean invHasContainerSoulbearer = false;
    private boolean isSoulbearerEmpty = false;
    private boolean invHasContainerRunePouch = false;
    private boolean isRunePouchFull = false;
    private boolean invHasContainerBoltPouch = false;
    private boolean isBoltPouchFull = false;
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
    private boolean containsContainerableLoot = false;
    private boolean containsConsumableLoot = false;
    private boolean containsEquippableLoot = false;
    private boolean containsProcessableLoot = false;
    private boolean containsCargoHoldLoot = false;


    @Setter
    private ItemContainer cargoHold;
    private ArrayList<Item> cargoContainerItems = new ArrayList<>();
    private int cargoHoldCapacity = 0;
    private int cargoHoldInventoryId = 33732;
    public boolean cargoHoldNeedsUpdate = false;
    private boolean cargoHoldFull;
    private boolean cargoHoldContainsSalvage;


    //endregion

    //TODO:  This one drives what the client suggests we do on the screen, so we want to be careful with it
    @Getter
    private int state;

    public ActionHandler(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, Client client, List<Crewmate> activeCrewmates, SalvagingHelperObjectOverlay objOverlay, Boat boat) {
        // Initialize maps
        this.plugin = plugin;
        this.config = config;
        this.objectOverlay = objOverlay;
        this.activeBoat = boat;
        buildAnimationMap();
        buildCrewmateMap(activeCrewmates);

    }

    public Instruction determineState(SalvagingHelperPlugin plugin, Client client) {

        Instruction newInstruction = currentInstruction;

        if (flagRebuild) {
            rebuild(plugin, client);
            flagRebuild = false;
        }

        processInventoryItems();
        processCargoHold();

        // Set up to perform the logic all at once cleanly
        int activeHooks = countHooks("Active");
        int inactiveHooks = countHooks("Inactive");
        int closestActiveShipwreckDistance = closestWreckDist(client);


        // Needs to move to new spot?
        // all hooks inactive, boat not moving, closest shipwreck


        // TODO: # crewmates assigned to salvage > 0
        if (inactiveHooks>0 && activeBoat.getBoatMoveMode()==0 && currentInstruction!=Instruction.SAIL_TO_SHIPWRECK
                    && closestActiveShipwreckDistance>1500) {
            newInstruction = Instruction.SAIL_TO_SHIPWRECK;
            plugin.sendIdleNotification();
        }

        objectOverlay.setGameObjHighlights(objectHighlightMap);
        objectOverlay.setNPCHighlights(npcHighlightMap);

        return newInstruction;
    }

    public void processPlayerAnimation(SalvagingHelperPlugin plugin, AnimationChanged event, Player player, int animationId) {
        if (animationId==plugin.playerCurrentAnimation){ return; }
        Crewmate.Activity mappedActivity = mapAnimToActivity.get(animationId);
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
        Crewmate.Activity mappedActivity = mapAnimToActivity.get(animationId);
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

        // TODO: replace this with deserialization?

        int[] salvagingAnims = new int[]{ 13576, 13577, 13583, 13584, 13598 };
        int[] sortingAnims = new int[]{ 13599 };
        int[] processingAnims = new int[]{ 713 }; // alching, crafting, etc?
        int[] cannonAnims = new int[]{ 13325, 13326, 13327 };
        int[] trawlingAnims = new int[]{ 13451, 13452, 13470, 13471, 13474 };
        int[] extractorAnims = new int[]{ 13176, 13178 };
        int[] repairAnims = new int[]{   };
        int[] steeringAnims = new int[]{ 13351, 13352, 13353, 13354, 13355, 13362, 13363, 13364, 13365, 13366 };
        int[] otherFishingAnims = new int[]{ 13436 };
        int[] otherAnims = new int[]{ 3660, 13310, 13317, 13318 };
        int[] movingAnims = new int[]{  };
        int[] idleAnims = new int[]{ -1, 866, 868, 2106, 7537, 13578, 13585 };
        int[] notSailingRelated = new int[]{ 714, 725, 829 };

        for (int anim : salvagingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.SALVAGING); }
        for (int anim : sortingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.SORTING_SALVAGE); }
        for (int anim : processingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.PROCESSING_SALVAGE); }
        for (int anim : cannonAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.CANNON); }
        for (int anim : trawlingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.TRAWLING); }
        for (int anim : extractorAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.EXTRACTING); }
        for (int anim : repairAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.SORTING_SALVAGE); }
        for (int anim : steeringAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.STEERING); }
        for (int anim : otherFishingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.FISHING_OTHER); }
        for (int anim : otherAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.OTHER); }
        for (int anim : movingAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.MOVING); }
        for (int anim : idleAnims) { mapAnimToActivity.put(anim, Crewmate.Activity.IDLE); }
    }

    public void buildCrewmateMap(List<Crewmate> crewmates) {
        for (Crewmate crewmate : crewmates) {
            if (crewmate != null && crewmate.getCrewmateNpc() != null) {
                npcHighlightMap.put(crewmate.getCrewmateNpc(), clear);
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
                    objectHighlightMap.put(obj, clear); // new Color(129, 255, 148);
                    if (!activeShipwrecks.contains(obj)) { activeShipwrecks.add(obj); }
                    return;
                case "2": // Shipwrecks (inactive)
                    //objectHighlightMap.put(obj, new Color(255, 179, 179));
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
                    //objectHighlightMap.put(obj, new Color(0, 51, 0));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.setCrystalExtractor(obj);
                    }
                    return;
                case "6": // Cargo hold
                    //objectHighlightMap.put(obj, new Color(0, 0, 153));
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                        boat.setCargoHold(obj);
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
                    }
                    return;
                case "13": // sails
                    if (isOurs(obj)) {
                        objectHighlightMap.put(obj, clear);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public boolean isOurs(GameObject object) {
        if (object.getWorldView()==plugin.getClient().getLocalPlayer().getWorldView()) {
            return true;
        } else {
            return false;
        }
    }

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
                        processObject(gameObject, plugin.ObjectTable, plugin.currentBoat);

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
                        processObject(gameObject, plugin.ObjectTable, plugin.currentBoat);
                    }
                }
            }
        }
    }

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

        // Containers
        invHasContainerLogBasket = inv.contains(28140) || inv.contains(28142) || inv.contains(28143) || inv.contains(28145);
        // TODO - account for wearable version
        invHasContainerPlankSack = inv.contains(24882);
        invHasContainerHerbSack = (inv.contains(13226) || inv.contains(24478)); // Closed, open
        invHasContainerGemBag = (inv.contains(24481) || inv.contains(12020));  // open, closed
        invHasContainerFishBarrel = (inv.contains(25582) || inv.contains(25584) || inv.contains(25585)
                || inv.contains(25587)); // TODO - wearable
        invHasContainerSoulbearer = inv.contains(19634);
        invHasContainerRunePouch = (inv.contains(12791) || inv.contains(23650) || inv.contains(24416) ||
                inv.contains(27281) || inv.contains(27509));






        inventoryWasUpdated = false;
    }

    public void setInventory(ItemContainer newInventory) {
        inv = newInventory;
        inventoryWasUpdated = true;
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
            plugin.debugLog(Arrays.asList(obj.getId() + "", client.getObjectDefinition(obj.getId()).getName(), obj.getLocalLocation().toString(), obj.getWorldLocation().toString(), obj.getSceneMaxLocation().toString(), obj.getSceneMinLocation().toString(), getObjectAnimation(obj) + ""), plugin);
        }
        for (GameObject wreck : activeShipwrecks) {
            plugin.sendChatMessage("Active: " + wreck.getLocalLocation().toString() + ", " + getObjectAnimation(wreck), false);
        }
        for (GameObject wreck : inactiveShipwrecks) {
            plugin.sendChatMessage("Inactive: " + wreck.getLocalLocation().toString() + ", " + getObjectAnimation(wreck), false);
        }
    }

    private int countHooks(String type) {
        int activeHooks = 0;
        int inactiveHooks = 0;
        ArrayList<Integer> idleHookAnims = new ArrayList<>(Arrays.asList(13575, 13582));
        //13565, 13567, 13572, 13579)); //13574, 13581 wrong?
        if (activeBoat.getHookPort() != null) {
            if (idleHookAnims.contains(getObjectAnimation(activeBoat.getHookPort()))) {
                inactiveHooks++;
            } else { activeHooks++; }
        }
        if (activeBoat.getHookStarboard() != null) {
            if (idleHookAnims.contains(getObjectAnimation(activeBoat.getHookStarboard()))) {
                inactiveHooks++;
            } else { activeHooks++; }
        }
        if (type.equals("Active")){ return activeHooks; }
        else if (type.equals("Inactive")){ return inactiveHooks; }
        else { return -1; }
    }

    private int closestWreckDist(Client client) {
        int closestActiveShipwreckDistance = 100000;
        for (GameObject wreck : activeShipwrecks) {
            int dist = wreck.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation());
            if (dist < closestActiveShipwreckDistance){
                closestActiveShipwreckDistance = dist;
            }
        }
        return closestActiveShipwreckDistance;
    }

    private void processCargoHold() {
        if (!cargoHoldNeedsUpdate) { return; }
        cargoContainerItems.addAll(List.of(cargoHold.getItems()));
        cargoHoldCapacity = cargoHold.size();
        cargoHoldContainsSalvage = plugin.salvageItemIds.stream().anyMatch(salvId -> cargoHold.contains(salvId));
        cargoHoldNeedsUpdate = false;
    }
}
