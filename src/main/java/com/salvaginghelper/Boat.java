package com.salvaginghelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectID;
import net.runelite.api.WorldEntity;

import java.util.HashMap;

public class Boat {

    // TODO: add other facilities
    @Getter @Setter
    private int boatType; // 0=not on boat, 1=skiff, 2=sloop
    @Getter @Setter
    private GameObject hookPort; // 60508
    @Getter @Setter
    private HookType hookPortType;
    @Getter @Setter
    private GameObject hookStarboard; // 60507
    @Getter @Setter
    private HookType hookStarboardType;
    @Getter @Setter
    private GameObject helm;
    @Getter @Setter
    private GameObject cargoHold;
    @Getter @Setter
    private GameObject crystalExtractor;
    @Getter @Setter
    private int extractorAnimation = -1;
    @Getter @Setter
    private GameObject salvagingStation;
    @Getter @Setter
    private GameObject[] kegs;
    @Getter @Setter
    private WorldEntity boatEntity;
    @Getter @Setter
    private int boatMoveMode;
    @Getter @Setter
    private boolean isOwned;
    @Getter @Setter
    private ActionHandler actionHandler;

    public HashMap<GameObject, HookType> objToHookType = new HashMap<>();
    public HashMap<Integer, HookType> idToHookType = new HashMap<>();
    //public HashMap<GameObject, HookType> toHookType = new HashMap<>();

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

    public Boat(SalvagingHelperPlugin plugin) {
        for (HookType hookType : HookType.values()) {
            idToHookType.put(hookType.getRaftId(), hookType);
            idToHookType.put(hookType.getSkiffId(), hookType);
            idToHookType.put(hookType.getSloopId1(), hookType);
            idToHookType.put(hookType.getSloopId2(), hookType);
        }
    }

    public void addHook(GameObject newHook) {
        // TODO: determine which side the hook is and slot it in
        // TODO; for non sloops
        if (newHook.getId()==60508) {
            setHookPort(newHook);
            setHookPortType(HookType.RUNE_HOOK);
            objToHookType.put(newHook, HookType.RUNE_HOOK);
        } else if (newHook.getId()==60507) {
            setHookStarboard(newHook);
            setHookStarboardType(HookType.RUNE_HOOK);
            objToHookType.put(newHook, HookType.RUNE_HOOK);
        } else if (newHook.getId()==60493) {
            setHookStarboard(newHook);
            setHookStarboardType(HookType.MITHRIL_HOOK);
            objToHookType.put(newHook, HookType.MITHRIL_HOOK);
        } else if (newHook.getId()==60504) {
            setHookPort(newHook);
            setHookPortType(HookType.MITHRIL_HOOK);
            objToHookType.put(newHook, HookType.MITHRIL_HOOK);
        } else if (newHook.getId()==60503) {
            setHookStarboard(newHook);
            setHookStarboardType(HookType.MITHRIL_HOOK);
            objToHookType.put(newHook, HookType.MITHRIL_HOOK);
        }
    }

    public void addKeg(GameObject newKeg) {
        // TODO: slot in and track
    }

    public int getSalvageRange() {
        int portSideRange = (hookPort != null) ? getHookPortType().getRange() : 0;
        int starboardSideRange = (hookStarboard != null) ? getHookStarboardType().getRange() : 0;
        return Math.max(portSideRange, starboardSideRange);
    }
}
