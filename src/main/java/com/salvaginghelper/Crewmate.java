package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("ALL")
public class Crewmate {

    //<editor-fold desc="Constants">
    // All the crewmates have insane identifiers in Crew Management and the cache, so we'll use our own :)
    // These npcIds are the ones used when they are spawned and visible with you on your boat
    public static final int JOBLESS_JIM_CM_ID = 1;
    public static final int JOBLESS_JIM_NPC_ID = 15256;
    public static final int CAPTAIN_SIAD_CM_ID = 9;
    public static final int CAPTAIN_SIAD_NPC_ID = 15334;
    public static final int ADVENTURER_ADA_CM_ID = 2;
    public static final int ADVENTURER_ADA_NPC_ID = 15265;
    public static final int CABIN_BOY_JENKINS_CM_ID = 10;
    public static final int CABIN_BOY_JENKINS_NPC_ID = 15344;
    public static final int OARSWOMAN_OLGA_CM_ID = 6;
    public static final int OARSWOMAN_OLGA_NPC_ID = 15305;
    public static final int JITTERY_JIM_CM_ID = 3;
    public static final int JITTERY_JIM_NPC_ID = 15275;
    public static final int BOSUN_ZARAH_CM_ID = 7;
    public static final int BOSUN_ZARAH_NPC_ID = 15315;
    public static final int JOLLY_JIM_CM_ID = 4;
    public static final int JOLLY_JIM_NPC_ID = 15285;
    public static final int SPOTTER_VIRGINIA_CM_ID = 8;
    public static final int SPOTTER_VIRGINIA_NPC_ID = 15325;
    public static final int SAILOR_JAKOB_CM_ID = 5;
    public static final int SAILOR_JAKOB_NPC_ID = 15295;
    //</editor-fold>


    //<editor-fold desc="Declarations">
    @Getter @Setter
    private NPC crewmateNpc;
    @Getter
    private int npcId;
    @Getter
    private int helmsmanship;
    @Getter
    private int privateering;
    @Getter
    private int deckhandiness;
    @Getter @Setter
    private int crewmemberNumber; // Slot in Crew Management the crewmate is loaded into, 1-5
    @Getter @Setter
    private Activity assignedTask; // What activity the captain has ordered this crewmate to do
    @Getter @Setter
    private int assignedStationNumber; // This is the integer the game's code associates with their assignment
    @Getter
    private String name;
    @Getter @Setter
    private Activity currentStatus = Activity.IDLE;
    @Getter @Setter
    private Activity lastStatus = Activity.IDLE;
    @Getter @Setter
    private int currentAnimation = -1;
    @Getter @Setter
    private int lastAnimation = -1;

    private final Client client;
    private final SalvagingHelperPlugin plugin;

    @Getter
    private final int slot;
    //</editor-fold>


    //<editor-fold desc="Enum - Activity">
    public enum Activity {
        SALVAGING,
        SORTING_SALVAGE,
        PROCESSING_SALVAGE,
        CANNON,
        TRAWLING,
        EXTRACTING,
        REPAIRS,
        STEERING,
        FISHING_OTHER,
        OTHER,
        MOVING,
        IDLE
    }
    //</editor-fold>

    //<editor-fold desc="Class Constructor">
    public Crewmate(SalvagingHelperPlugin plugin, int slotNumber, int npcCrewmemberNumber, boolean isOnBoat, Client parentClient, int position) {

        //plugin.sendChatMessage("Creating new crew member: slot "+slotNumber+", crewmember #"+npcCrewmemberNumber+", position "+position);
        this.slot = slotNumber;
        this.crewmemberNumber = npcCrewmemberNumber;
        this.assignedTask = Activity.IDLE;
        this.currentStatus = Activity.IDLE;
        this.lastStatus = Activity.IDLE;
        this.client = parentClient;
        this.assignedStationNumber = position;
        this.plugin = plugin;

        // Load variables per crewmate
        switch (npcCrewmemberNumber) {
            // Stats updated from wiki 12/18/2025
            case JOBLESS_JIM_CM_ID:
                this.name="Jobless Jim";
                this.npcId = JOBLESS_JIM_NPC_ID;
                this.helmsmanship = 2;
                this.privateering = 1;
                this.deckhandiness = 3;
                return;
            case CAPTAIN_SIAD_CM_ID:
                this.name = "Captain Siad";
                this.npcId = CAPTAIN_SIAD_NPC_ID;
                this.helmsmanship = 3;
                this.privateering = 2;
                this.deckhandiness = 3;
                return;
            case ADVENTURER_ADA_CM_ID:
                this.name = "Adventurer Ada";
                this.npcId = ADVENTURER_ADA_NPC_ID;
                this.helmsmanship = 3;
                this.privateering = 2;
                this.deckhandiness = 1;
                return;
            case CABIN_BOY_JENKINS_CM_ID:
                this.name = "Cabin Boy Jenkins";
                this.npcId = CABIN_BOY_JENKINS_NPC_ID;
                this.helmsmanship = 2;
                this.privateering = 2;
                this.deckhandiness = 4;
                return;
            case OARSWOMAN_OLGA_CM_ID:
                this.name = "Oarswoman Olga";
                this.npcId = OARSWOMAN_OLGA_NPC_ID;
                this.helmsmanship = 2;
                this.privateering = 4;
                this.deckhandiness = 2;
                return;
            case JITTERY_JIM_CM_ID:
                this.name = "Jittery Jim";
                this.npcId = JITTERY_JIM_NPC_ID;
                this.helmsmanship = 1;
                this.privateering = 4;
                this.deckhandiness = 1;
                return;
            case BOSUN_ZARAH_CM_ID:
                this.name = "Bosun Zarah";
                this.npcId = BOSUN_ZARAH_NPC_ID;
                this.helmsmanship = 3;
                this.privateering = 3;
                this.deckhandiness = 2;
                return;
            case JOLLY_JIM_CM_ID:
                this.name = "Jolly Jim";
                this.npcId = JOLLY_JIM_NPC_ID;
                this.helmsmanship = 1;
                this.privateering = 1;
                this.deckhandiness = 4;
                return;
            case SPOTTER_VIRGINIA_CM_ID:
                this.name = "Spotter Virginia";
                this.npcId = SPOTTER_VIRGINIA_NPC_ID;
                this.helmsmanship = 4;
                this.privateering = 2;
                this.deckhandiness = 2;
                return;
            case SAILOR_JAKOB_CM_ID:
                this.name = "Sailor Jakob";
                this.npcId = SAILOR_JAKOB_NPC_ID;
                this.helmsmanship = 4;
                this.privateering = 1;
                this.deckhandiness = 1;
                return;
            default:
                //return;
        //plugin.debugLog("Crewmember values set - name "+name+", NPC ID "+npcId+", slot "+this.slot);
        }

        plugin.toCrewmate.put(this.npcId, this);

        if (isOnBoat) {
            matchToNPC();
        }

    }
    //</editor-fold>

    //<editor-fold desc="NPC Match">
    // TODO: do we need to retroactively set animation/status?
    public void matchToNPC() {
/*        for (NPC npc : client.getTopLevelWorldView().npcs()) {
            if (npc.getId() == npcId) {
                //plugin.sendChatMessage("Checking wv NPC: "+npc.getComposition().toString()+", "+npc.getWorldLocation().getRegionID()+", "+npc.getWorldArea().toString());
                setCrewmateNpc(npc);
                return;
            }
        }*/
        for (NPC npc : client.getLocalPlayer().getWorldView().npcs()) {
            if (npc.getId() == npcId && client.getLocalPlayer().getWorldView()==npc.getWorldView()) {
                //plugin.sendChatMessage("Checking localplayer NPC: "+npc.getComposition().toString()+", "+npc.getWorldLocation().getRegionID()+", "+npc.getWorldArea().toString());
                setCrewmateNpc(npc);
                return;
            }
        }
    }

    public static void matchNPCToCrewmates(NPC npc, List<Crewmate> crewmateList, Player player) {
        for (Crewmate crewmate : crewmateList) {
            if (crewmate != null) {
                // Other players' crewmate NPCs won't have originated from the same WorldView as our player
                if (npc.getId() == crewmate.getNpcId() && npc.getLocalLocation().getWorldView()==player.getWorldView().getId()) {
                    crewmate.setCrewmateNpc(npc);
                    break;
                }
            }
        }
    }

    // TODO
    public void rescanNPCs() {
        if (crewmateNpc == null) {
            matchToNPC();
        } else {
            boolean found = false;
        }
    }

    public static boolean isNPCSailingCrewmate(@Nonnull NPC npc) {
        if (npc != null) {
            switch (npc.getId()) {
                case JOBLESS_JIM_NPC_ID:
                case CAPTAIN_SIAD_NPC_ID:
                case ADVENTURER_ADA_NPC_ID:
                case CABIN_BOY_JENKINS_NPC_ID:
                case OARSWOMAN_OLGA_NPC_ID:
                case JITTERY_JIM_NPC_ID:
                case BOSUN_ZARAH_NPC_ID:
                case JOLLY_JIM_NPC_ID:
                case SPOTTER_VIRGINIA_NPC_ID:
                case SAILOR_JAKOB_NPC_ID:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }

    }
    //</editor-fold>

    // TODO: figure out wtf this godforsaken field does and how I can figure out its values
    // Known values:
    //           VISUAL REPRESENTATION FOR SLOOP:                              SKIFF
    //   cargo hold (?)     |    inoculation station (?)        cargo hold       |   inoculation station
    //   -------------------|---------------------------      -------------------|---------------------------
    //   14 - port hook     |    13 - starboard hook                   -         |   9 - cannon
    //   12 - station       |    11 - station                          -         |   8 - station
    //   10 - port cannon   |    9 - starboard cannon                  -         |
    //                      |                                                    |
    //                      |    ? - ?
    //   4 - helm           |    ? - ?
    //
    //
    public String mapAssignedStation(int station) {
        if (plugin.currentBoat.getBoatType() == 2) {
            switch (station) {
                case 0:
                    return "No Assignment";
                case 4:
                    return "Helm";
                case 5:
                    return "Repairs";
                case 9:
                    return "Cannon (starboard)";
                case 10:
                    return "Cannon (port)";
                case 12:
                    return "Middle station (port)";
                case 13:
                    return "Hook (starboard)";
                case 14:
                    return "Hook (port)";
                default:
                    return "Unknown ("+station+")";
            }
        } else {
            return "Raft/skiff not yet implemented";
        }
    }

}