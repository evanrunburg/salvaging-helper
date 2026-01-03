package com.salvaginghelper;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Notification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import com.salvaginghelper.Crewmate.Activity;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


@Slf4j
@PluginDescriptor(
		name = "Salvaging Helper",
		description = "Utilities and QOL updates to help you train Sailing",
		tags = {
				"sailing",
				"salvaging",
				"shipwreck",
				"notification",
				"alert",
				"tracker",
				"sort loot"
		}
)
public class SalvagingHelperPlugin extends Plugin
{
    //region Variable Declarations
    private Path LOG_FILE_PATH_VARBIT;
	private LookupTable VarbNameTable;
	private LookupTable VarpNameTable;
	private LookupTable VBLT;
	private LookupTable VPLT;
	public LookupTable ObjectTable;
	private static final int VARBIT_WHITELIST = 2;
	private static final int VARBIT_BLACKLIST = 3;
	private static final int VARBIT_SAILING_LIST = 4;

	public boolean debug = true;
	public Instant startTime;

	public final static ArrayList<Integer> shipwreckRespawnAnimIds = new ArrayList<>(Arrays.asList(13603, 13607, 13611,
			13615, 13619, 13623, 13627, 13631));
	public final static ArrayList<Integer> activeShipwreckIds = new ArrayList<>(List.of(60464, 60466, 60468, 60470,
			60472, 60474, 60476, 60478));
	public final static ArrayList<Integer> inactiveShipwreckIds = new ArrayList<>(List.of(60465, 60467, 60469, 60471,
			60473, 60475, 60477, 60479));
	public final static ArrayList<Integer> salvageItemIds = new ArrayList<>(List.of(32847, 32849, 32851, 32853, 32855,
			32857, 32859, 32861));

	// Status variables about one's current voyage
	List<Crewmate> activeCrewmates = new ArrayList<>(5);
	Map<Integer, Crewmate> toCrewmate = new HashMap<>();
	public boolean onBoat = false;
	private int[] boatHotspots = new int[11];
	private boolean ownsCurrentBoat = false;
	public Activity playerLastActivity = Activity.IDLE;
	public Activity playerCurrentActivity = Activity.IDLE;
	public int playerLastAnimation = -1;
	public int playerCurrentAnimation = -1;
	public ActionHandler actionHandler;
	public ActionHandler.Instruction directions;
	private NavigationButton navigationButton;
	private int playerAtFacility=-1;
	public int crewmateCount=0;
	public Boat currentBoat = new Boat(this);

	// Player specific variables
	private int[][] runePouch = new int[4][2]; // runePouch[slot]={runeType, runeQuantity}
	private int[] plankSack = new int[7]; // 0-6 are normal through rosewood
	@Getter
	private ItemContainer inventoryContainer;
	@Getter
	private ArrayList<Item> inventoryItems;

    //endregion

    //region Plugin Boilerplate
	@Inject
	private SalvagingHelperItemUnderlay itemOverlay;

    @Inject
	public ItemManager itemManager; // basically just used to canonicalize

	@Inject
	public ConfigManager configManager;

	@Inject
	private SalvagingHelperDebugOverlay debugOverlay;

	@Inject
	private SalvagingHelperObjectOverlay objectOverlay;

	@Inject @Getter
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Notifier notifier;

	@Inject
	private SalvagingHelperConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientThread clientThread;

	private SalvagingHelperPanel sidePanel;

	@Inject
	private ColorPickerManager colorPickerManager;

	public LootManager lootManager;

	@Override
	protected void startUp() throws Exception {
		debug = config.debugModeEnabled();

		// Mappings
		ObjectTable = new LookupTable("src/main/resources/sailingobjects.properties");

		activeCrewmates.add(0, null);
		activeCrewmates.add(1, null);
		activeCrewmates.add(2, null);
		activeCrewmates.add(3, null);
		activeCrewmates.add(4, null);
		currentBoat.setActionHandler(actionHandler);

		// Graphics
		overlayManager.add(itemOverlay);
		overlayManager.add(debugOverlay);
		overlayManager.add(objectOverlay);

		// Concept managers
		lootManager = new LootManager(this, config, configManager);
		actionHandler = new ActionHandler(this, config, client, activeCrewmates, objectOverlay, currentBoat);

		// UI
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "salvaging_helper_icon.png");
		sidePanel = new SalvagingHelperPanel(this, config, client, itemManager, configManager, lootManager, clientThread);
		navigationButton = NavigationButton.builder()
				.tooltip("Salvaging Helper")
				.icon(icon)
				.priority(4)
				.panel(sidePanel)
				.build();
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(itemOverlay);
		overlayManager.remove(debugOverlay);
		overlayManager.remove(objectOverlay);
		clientToolbar.removeNavigation(navigationButton);
	}

	@Provides
	SalvagingHelperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(SalvagingHelperConfig.class);
	}
    //endregion

    //region Debug/Dev
    public void debugLog(List<String> elements, SalvagingHelperPlugin plugin) {
		if (!debug) { return; }
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String message = timestamp + "|" + String.join("|", elements)+"\n";
		try {
			Files.writeString(plugin.LOG_FILE_PATH_VARBIT, message, StandardOpenOption.APPEND);
			log.debug(message);
		} catch (IOException e) {
			log.debug("Error writing to log file: " + plugin.LOG_FILE_PATH_VARBIT);
		}
	}

	public void sendChatMessage(String chatMessage, boolean alwaysSend) {
		if (alwaysSend || debug) {
			final String message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append(chatMessage)
					.build();

			chatMessageManager.queue(
					QueuedMessage.builder()
							.type(ChatMessageType.CONSOLE)
							.runeLiteFormattedMessage(message)
							.build());
		}
	}
    //endregion

    //region Event Listeners

    @Subscribe
	public void onGameTick(GameTick gameTick) {
		if (!onBoat) { return; }

		// TODO: do we need to increase/decrease the priority to make sure we have data?

		// Plugin keeps mysteriously detaching NPCs in testing :)
		if (onBoat && crewmateCount>0) {
			for (Crewmate mate : activeCrewmates) {
				if (mate != null && mate.getCrewmateNpc()==null) { mate.rescanNPCs(); }
			}
		}
		actionHandler.buildCrewmateMap(activeCrewmates);
		crewmateCount = Math.toIntExact(activeCrewmates.stream().filter(Objects::nonNull).count()); // TODO - copy this around?

		// Check animation state of particular objects that transmog instead of triggering event handlers
		GameObject extractor = currentBoat.getCrystalExtractor();
		if (extractor != null) {
			switch (actionHandler.getObjectAnimation(extractor)) {
				case 13174:
					actionHandler.objectHighlightMap.put(extractor, Color.RED);
					currentBoat.setExtractorAnimation(13174);
					break;
				case 13177:
					actionHandler.objectHighlightMap.put(extractor, Color.GREEN);
					if (currentBoat.getExtractorAnimation() != 13177) {
						currentBoat.setExtractorAnimation(13177);
						sendExtractorNotification();
					}
					break;
				case 13175:
					actionHandler.objectHighlightMap.put(extractor, new Color(0, 0, 0, 0));
					currentBoat.setExtractorAnimation(13175);
					break;
				default:
					sendChatMessage("Error with crystal extractor animation: "+actionHandler.getObjectAnimation(extractor), false);
			}

		}

		// Notification/activity tracking
		//notificationManager.setLastMouseClick(Instant.now().minusMillis((long) (0.6*client.getMouseLastPressedMillis())));
		//notificationManager.setLastMouseMovement(Instant.now().minusMillis(600*client.getMouseIdleTicks()));

		//notificationManager.process();

		// "Main"
		directions = actionHandler.determineState(this, client);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		actionHandler.processObject(event.getGameObject(), ObjectTable, currentBoat);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		actionHandler.deleteObject(event.getGameObject());
	}

	@Subscribe
	private void onConfigChanged(final ConfigChanged event) {
		// TODO - create...?
		debug = config.debugModeEnabled();
		return;
	}

	@Subscribe
	private void onVarbitChanged(final VarbitChanged event) {
		int vbId = event.getVarbitId();
		int val = event.getValue();
		switch (vbId) {

			//region Boat and Crew
			case VarbitID.SAILING_BOARDED_BOAT:
				onBoat = (val > 0);
				return;
			case VarbitID.SAILING_SIDEPANEL_BOAT_MOVE_MODE:
				currentBoat.setBoatMoveMode(val);
				return;
			case VarbitID.SAILING_CREW_SLOT_1:
				replaceCrewMember(1, val);
				return;
			case VarbitID.SAILING_CREW_SLOT_2:
				replaceCrewMember(2, val);
				return;
			case VarbitID.SAILING_CREW_SLOT_3:
				replaceCrewMember(3, val);
				return;
			case VarbitID.SAILING_CREW_SLOT_4:
				replaceCrewMember(4, val);
				return;
			case VarbitID.SAILING_CREW_SLOT_5:
				replaceCrewMember(5, val);
				return;
			case VarbitID.SAILING_CREW_SLOT_1_POSITION:
				activeCrewmates.get(0).setAssignedStationNumber(val);
				return;
			case VarbitID.SAILING_CREW_SLOT_2_POSITION:
				activeCrewmates.get(1).setAssignedStationNumber(val);
				return;
			case VarbitID.SAILING_CREW_SLOT_3_POSITION:
				activeCrewmates.get(2).setAssignedStationNumber(val);
				return;
			case VarbitID.SAILING_CREW_SLOT_4_POSITION:
				activeCrewmates.get(3).setAssignedStationNumber(val);
				return;
			case VarbitID.SAILING_CREW_SLOT_5_POSITION:
				activeCrewmates.get(4).setAssignedStationNumber(val);
				return;
			case VarbitID.SAILING_PLAYER_IS_ON_PLAYER_BOAT:
				currentBoat.setOwned(val > 0);
				return;
			case VarbitID.SAILING_BOARDED_BOAT_TYPE:
				currentBoat.setBoatType(val);
				return;
			//endregion

			//region Facilities, Stations, Assignments
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT0:
				boatHotspots[0] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT1:
				boatHotspots[1] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT2:
				boatHotspots[2] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT3:
				boatHotspots[3] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT4:
				boatHotspots[4] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT5:
				boatHotspots[5] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT6:
				boatHotspots[6] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT7:
				boatHotspots[7] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT8:
				boatHotspots[8] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT9:
				boatHotspots[9] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_FACILITY_HOTSPOT10:
				boatHotspots[10] = val;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_0:
				playerAtFacility = (val==1) ? 0 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_1:
				playerAtFacility = (val==1) ? 1 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_2:
				playerAtFacility = (val==1) ? 2 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_3:
				playerAtFacility = (val==1) ? 3 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_4:
				playerAtFacility = (val==1) ? 4 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_5:
				playerAtFacility = (val==1) ? 5 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_6:
				playerAtFacility = (val==1) ? 6 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_7:
				playerAtFacility = (val==1) ? 7 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_8:
				playerAtFacility = (val==1) ? 8 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_9:
				playerAtFacility = (val==1) ? 9 : -1;
				return;
			case VarbitID.SAILING_SIDEPANEL_PLAYER_AT_FACILITY_10:
				playerAtFacility = (val==1) ? 10 : -1;
				return;
			//endregion
			//region Containers (despairge...)
			case VarbitID.RUNE_POUCH_TYPE_1: // varb 4070 = current spellbook if that ever becomes relevant
				runePouch[0][0] = val; // TODO also apparently 16201 =
				return;
			case VarbitID.RUNE_POUCH_TYPE_2:
				runePouch[1][0] = val;
				return;
			case VarbitID.RUNE_POUCH_TYPE_3:
				runePouch[2][0] = val;
				return;
			case VarbitID.RUNE_POUCH_TYPE_4:
				runePouch[3][0] = val;
				return;
			case VarbitID.RUNE_POUCH_QUANTITY_1:
				runePouch[0][1] = val;
				return;
			case VarbitID.RUNE_POUCH_QUANTITY_2:
				runePouch[1][1] = val;
				return;
			case VarbitID.RUNE_POUCH_QUANTITY_3:
				runePouch[2][1] = val;
				return;
			case VarbitID.RUNE_POUCH_QUANTITY_4:
				runePouch[3][1] = val;
				return;
			case VarbitID.RUNE_POUCH_SELECTEDQUANTITY: // TODO: do we need to work this in?
				return;
			case VarbitID.PLANK_SACK_PLAIN:
				plankSack[0] = val;
				return;
			case VarbitID.PLANK_SACK_OAK:
				plankSack[1] = val;
				return;
			case VarbitID.PLANK_SACK_TEAK:
				plankSack[2] = val;
				return;
			case VarbitID.PLANK_SACK_MAHOGANY:
				plankSack[3] = val;
				return;
			case VarbitID.PLANK_SACK_CAMPHOR:
				plankSack[4] = val;
				return;
			case VarbitID.PLANK_SACK_IRONWOOD:
				plankSack[5] = val;
				return;
			case VarbitID.PLANK_SACK_ROSEWOOD:
				plankSack[6] = val;
				return;
			//endregion
			default:
		}
	}

	@Subscribe
	private void onAnimationChanged(final AnimationChanged event) {
		// TODO
		Actor actor = event.getActor();
		int animationId = actor.getAnimation();
		if (actor instanceof Player) {
			if (actor == client.getLocalPlayer()) {
				if (!onBoat) { return; }
				actionHandler.processPlayerAnimation(this, event, client.getLocalPlayer(), animationId);
			}
		} else if (actor instanceof NPC) {
			for (Crewmate crewmate : activeCrewmates) {
				if (crewmate != null && crewmate.getCrewmateNpc() == actor) {
					actionHandler.processCrewmateAnimation(this, event, crewmate, animationId);
					return;
				}
			}
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		NPC npc = event.getNpc();
		// The only thing distinguishing our crewmates from other players' crewmates is that they share a WorldView
		// with the one our player (but not client) occupies
		if (Crewmate.isNPCSailingCrewmate(npc) && npc.getLocalLocation().getWorldView()==client.getLocalPlayer().getWorldView().getId()) {
			Crewmate.matchNPCToCrewmates(npc, activeCrewmates, client.getLocalPlayer());
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		NPC npc = event.getNpc();
		for (Crewmate crewmate : activeCrewmates) {
			if (crewmate != null && crewmate.getCrewmateNpc() == npc) {
				crewmate.setCrewmateNpc(null);
			}
		}
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged itemChange) {
		// TODO: Runelite gamevals seem to be broken for interfaces atm, let's replace these numbers later
		ItemContainer container = itemChange.getItemContainer();
		int containerId = itemChange.getContainerId();
		// sendChatMessage(itemChange.toString()+" / size: "+container.size());
		switch (containerId) {
			case 93: // inventory
				inventoryContainer = container;
				actionHandler.setInventory(container);
				inventoryItems = new ArrayList<>(List.of(container.getItems()));
				return;
			case 94: // ?? populates on spawn?
				return;
			case 95: // bank
				return;
			case 964: // ???? SAILING_BOAT_2_CARGOHOLD ???????
				return;
			case 33732: // cargo hold
				actionHandler.cargoHoldNeedsUpdate = true;
				actionHandler.setCargoHold(container);
				return;
			case 33733: // cargo hold also...?
				return;
			default:
				return;
		}
	}

	@Subscribe
	private void onMenuOpened(MenuOpened event) {
		//debugLog(Arrays.asList(event.toString(), event.getFirstEntry().toString(), String.valueOf(event.getFirstEntry().isDeprioritized())), this);
	}

	@Subscribe
	private void onPostAnimation(PostAnimation event) {
		if (shipwreckRespawnAnimIds.contains(event.getAnimation().getId())) {
			actionHandler.collectShipwrecks(client, false);
		}
	}

	@Subscribe
	private void onWorldEntitySpawned(WorldEntitySpawned event) {
		WorldEntity entity = event.getWorldEntity();
		if (entity.getOwnerType()==WorldEntity.OWNER_TYPE_SELF_PLAYER) {
			currentBoat.setBoatEntity(entity);
		}
	}

	// TODO - parse status of item containers
	@Subscribe
	private void onChatMessage(final ChatMessage event) {

	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event) {
		if (event.getGameState()==GameState.LOGIN_SCREEN || event.getGameState()==GameState.HOPPING) {
			actionHandler.inactiveShipwrecks.clear();
			actionHandler.activeShipwrecks.clear();
			actionHandler.objectHighlightMap.clear();
			actionHandler.npcHighlightMap.clear();
			sendChatMessage("Clearing all cached objects and NPCs.", false);
		}
	}
    //endregion


    //region Notifications

    public void sendExtractorNotification() {
		if (config.extractorAlertsEnabled()) {
			Notification notif = new Notification(true, true, true, true,
					config.extractorTrayType(),
					config.extractorFocusType(),
					config.extractorAlertSound(), "",
					config.extractorAlertVolume(),
					5000,
					false,
					config.extractorScreenFlashType(),
					config.screenFlashColor(),
					config.extractorAlertWhileFocused());
			notifier.notify(notif, "Crystal extractor ready to harvest");
		}
	}

	public void sendIdleNotification() {
		if (config.idleAlertsEnabled()) {
			Notification notif = new Notification(true, true, true, true,
					config.idleTrayType(),
					config.idleFocusType(),
					config.idleAlertSound(), "",
					config.idleAlertVolume(),
					5000,
					false,
					config.idleScreenFlashType(),
					config.screenFlashColor(),
					config.idleAlertWhileFocused());
			notifier.notify(notif, "Move to a new shipwreck!");
		}
	}
    //endregion


    //region Sailing Methods
    private void replaceCrewMember(int slot, int newCrewmemberListId) {
		// Delete
		if (newCrewmemberListId == 0 && activeCrewmates.get(slot - 1) != null) {
			toCrewmate.remove(activeCrewmates.get(slot-1).getNpcId());
			activeCrewmates.set(slot - 1, null);
		}
		// Add
		else if (newCrewmemberListId != 0) {
			activeCrewmates.set(slot - 1, new Crewmate(this, slot - 1, newCrewmemberListId, onBoat, client, 0));
		}
	}

	//endregion


}
