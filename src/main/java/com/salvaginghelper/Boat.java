package com.salvaginghelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectID;
import net.runelite.api.WorldEntity;

import java.util.ArrayList;
import java.util.HashMap;

public class Boat {


    //region General Variables
    @Getter @Setter
    private ActionHandler actionHandler;
    private final SalvagingHelperPlugin plugin;

    @Getter @Setter
    private WorldEntity boatEntity;
    @Getter @Setter
    private int boatMoveMode;
    @Getter @Setter
    private boolean isOwned;
    @Getter @Setter
    private int boatType; // 0=raft(?), 1=skiff, 2=sloop
    //endregion

    //region Cargo Hold
    @Getter
    private GameObject cargoHold;
    private HoldType cargoHoldType;
    private int cargoHoldId = -1;
    @Getter
    private int cargoHoldCapacity = 0;
    //endregion

    //region Salvaging Hooks
    @Getter
    private GameObject hookPort;
    @Getter
    private int hookPortId = -1;
    @Getter
    private HookType hookPortType;
    @Getter @Setter
    private Crewmate hookPortAssignedCrewmate;
    @Getter @Setter
    private boolean hookPortPlayerAssigned = false;
    @Getter
    private GameObject hookStarboard;
    @Getter
    private int hookStarboardId = -1;
    @Getter
    private HookType hookStarboardType;
    @Getter @Setter
    private Crewmate hookStarboardAssignedCrewmate;
    @Getter @Setter
    private boolean hookStarboardPlayerAssigned = false;
    //endregion

    //region Other Facilities
    @Getter @Setter
    private GameObject crystalExtractor;
    @Getter @Setter
    private int extractorAnimation = -1;
    @Getter @Setter
    private GameObject salvagingStation;
    @Getter
    private GameObject[] kegs;
    @Getter @Setter
    private GameObject helm;
    @Getter
    private int helmId;
    @Getter
    private GameObject sails;
    @Getter
    private int sailsId;
    //endregion


    //region Maps
    public HashMap<GameObject, HookType> objToHookType = new HashMap<>();
    public HashMap<Integer, HookType> idToHookType = new HashMap<>();
    public HashMap<Integer, GameObject> idToHookObj = new HashMap<>();
    //endregion

    @RequiredArgsConstructor @Getter
    public enum HookType { // TODO: figure out actual ranges...
        BRONZE_HOOK(60483, 60490, 60497, 60498, 1, 500),
        IRON_HOOK(60484, 60491, 60499, 60500, 1, 500),
        STEEL_HOOK(60485, 60492, 60501, 60502, 1, 500),
        MITHRIL_HOOK(60486, 60493, 60503, 60504, 2, 500),
        ADAMANT_HOOK(60487, 60494, 60505, 60506, 2, 500),
        RUNE_HOOK(60488, 60495, 60507, 60508, 3, 1150),
        DRAGON_HOOK(60489, 60496, 60509, 60510, 4, 1500);

        private final int raftId;
        private final int skiffId;
        private final int sloopId1;
        private final int sloopId2;
        private final int deckhandinessReq;
        private final int range;

        private int[] allObjectIds() {
            return new int[]{ getRaftId(), getSkiffId(), getSloopId1(), getSloopId2() };
        }
    }

    @RequiredArgsConstructor @Getter
    public enum HoldType {
        BASIC("Basic cargo hold", 60245, 60259, 60273, 20, 30, 40),
        OAK("Oak cargo hold", 60247, 60261, 60275, 30, 45, 60),
        TEAK("Teak cargo hold", 60249, 60263, 60277, 45, 60, 90),
        MAHOGANY("Mahogany cargo hold", 60251, 60265, 60279, 60, 90, 120),
        CAMPHOR("Camphor cargo hold", 60253, 60267, 60281, 80, 120, 160),
        IRONWOOD("Ironwood cargo hold", 60255, 60269, 60283, 105, 150, 210),
        ROSEWOOD("Rosewood cargo hold", 60257, 60271, 60285, 120, 180, 240);

        private final String name;
        private final int raftId;
        private final int skiffId;
        private final int sloopId;
        private final int raftCapacity;
        private final int skiffCapacity;
        private final int sloopCapacity;

        public static int getCapacity(HoldType type, int shipType) {
            if (shipType==0) { return type.raftCapacity; }
            if (shipType==1) { return type.skiffCapacity; }
            if (shipType==2) { return type.sloopCapacity; }
            return -1;
        }

        public int getId(HoldType type, int shipType) {
            if (shipType==0) { return type.raftId; }
            if (shipType==1) { return type.skiffId; }
            if (shipType==2) { return type.sloopId; }
            return -1;
        }

        public static ArrayList<Integer> getAllIds() {
            ArrayList<Integer> ids = new ArrayList<>();
            for (HoldType holdType : HoldType.values()) {
                ids.add(holdType.getRaftId());
                ids.add(holdType.getSkiffId());
                ids.add(holdType.getSloopId());
            }
            return ids;
        }

        public static HoldType idToEnumVal(GameObject obj) {
            int id = obj.getId();
            if (id==BASIC.raftId || id==BASIC.skiffId || id==BASIC.sloopId) { return BASIC; }
            if (id==OAK.raftId || id==OAK.skiffId || id==OAK.sloopId) { return OAK; }
            if (id==TEAK.raftId || id==TEAK.skiffId || id==TEAK.sloopId) { return TEAK; }
            if (id==MAHOGANY.raftId || id==MAHOGANY.skiffId || id==MAHOGANY.sloopId) { return MAHOGANY; }
            if (id==CAMPHOR.raftId || id==CAMPHOR.skiffId || id==CAMPHOR.sloopId) { return CAMPHOR; }
            if (id==IRONWOOD.raftId || id==IRONWOOD.skiffId || id==IRONWOOD.sloopId) { return IRONWOOD; }
            if (id==ROSEWOOD.raftId || id==ROSEWOOD.skiffId || id==ROSEWOOD.sloopId) { return ROSEWOOD; }
            return null;
        }
    }

    public Boat(SalvagingHelperPlugin plugin) {
        this.plugin = plugin;
        buildHookTypeMap();
    }

    public void addHook(GameObject newHook) {
        int newId = newHook.getId();
        HookType newHookType = idToHookType.get(newId);
        idToHookObj.put(newId, newHook);
        objToHookType.put(newHook, newHookType);

        if (boatType==0 || boatType==1 || (boatType==2 && newId==newHookType.getSloopId1())) {
            hookStarboard = newHook;
            hookStarboardId = newId;
            hookStarboardType = newHookType;
            // TODO - determine crewmate/player assignment
            return;
        } else if (boatType==2 && newId==newHookType.getSloopId2()) {
            hookPort = newHook;
            hookPortId = newId;
            hookPortType = newHookType;
            return;
        } else {
            plugin.sendChatMessage("Uncaught boat/hook combination - failed to add hook "+newId, false);
        }
    }

    public void addHold(GameObject newCargoHold) {
        cargoHoldId = newCargoHold.getId();
        cargoHold = newCargoHold;
        cargoHoldType = HoldType.idToEnumVal(newCargoHold);
        cargoHoldCapacity = HoldType.getCapacity(cargoHoldType, boatType);
    }

    public void addKeg(GameObject newKeg) {
        // TODO: slot in and track
    }

    public void addSails(GameObject newSails) {
        sails = newSails;
        sailsId = newSails.getId();
    }

    public void addHelm(GameObject newHelm) {
        helm = newHelm;
        helmId = newHelm.getId();
    }

    public ArrayList<GameObject> allHooks() {
        ArrayList<GameObject> hooks = new ArrayList<>();
        if (hookPort!=null) { hooks.add(hookPort); }
        if (hookStarboard!=null) { hooks.add(hookStarboard); }
        return hooks;
    }

    public int getSalvageRange() {
        int portSideRange = (hookPort != null) ? getHookPortType().getRange() : 0;
        int starboardSideRange = (hookStarboard != null) ? getHookStarboardType().getRange() : 0;
        return Math.max(portSideRange, starboardSideRange);
    }

    private void buildHookTypeMap() {
        for (HookType hookType : HookType.values()) {
            idToHookType.put(hookType.getRaftId(), hookType);
            idToHookType.put(hookType.getSkiffId(), hookType);
            idToHookType.put(hookType.getSloopId1(), hookType);
            idToHookType.put(hookType.getSloopId2(), hookType);
        }
    }
}
