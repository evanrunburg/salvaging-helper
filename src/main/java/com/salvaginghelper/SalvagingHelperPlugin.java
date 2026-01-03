package com.salvaginghelper;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarPlayerID;
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
import net.runelite.http.api.item.ItemPrice;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
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
	private VarbitLookupTable VarbNameTable;
	private VarbitLookupTable VarpNameTable;
	private VarbitLookupTable VBLT;
	private VarbitLookupTable VPLT;
	public VarbitLookupTable ObjectTable;
	private static final int VARBIT_WHITELIST = 2;
	private static final int VARBIT_BLACKLIST = 3;
	private static final int VARBIT_SAILING_LIST = 4;

	public boolean debug = true;
	public Instant startTime;

	// Status variables about one's current voyage
	List<Crewmate> activeCrewmates = new ArrayList<>(5);
	Map<Integer, Crewmate> toCrewmate = new HashMap<>();
	public boolean onBoat = false;
	private int[] boatHotspots = new int[11];
	private boolean ownsCurrentBoat = false;
	public Activity playerLastActivity;
	public Activity playerCurrentActivity;
	public int playerLastAnimation;
	public int playerCurrentAnimation;
	public ActionHandler actionHandler;
	public ActionHandler.Instruction directions;
	private NavigationButton navigationButton;
	private Activity playerStatus;
	private int playerAtFacility=-1;
	private int playerAssignedFacility=-1;
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

	//@Inject
	//private SalvagingHelperPanel salvagingHelperPanel;

	private SalvagingHelperPanel sidePanel;

	@Inject
	private ColorPickerManager colorPickerManager;

	public LootManager lootManager;

	@Override
	protected void startUp() throws Exception {
		debug = config.debugModeEnabled();



		// Logging and mappings
		log.debug("Salvaging Helper loaded");
		VarbNameTable = new VarbitLookupTable("src/main/resources/varbits.properties");
		VarpNameTable = new VarbitLookupTable("src/main/resources/varplayers.properties");
		VBLT = new VarbitLookupTable("src/main/resources/varbitlist.properties");
		VPLT = new VarbitLookupTable("src/main/resources/varplist.properties");
		ObjectTable = new VarbitLookupTable("src/main/resources/sailingobjects.properties");
        String LOG_FILE_NAME_VARBIT = "vb" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt";
		LOG_FILE_PATH_VARBIT = Paths.get("logs/varbits/"+ LOG_FILE_NAME_VARBIT);
		log.debug("Varbit log file name set: " + LOG_FILE_NAME_VARBIT);
		Files.createFile(LOG_FILE_PATH_VARBIT);
		startTime = Instant.now();

		activeCrewmates.add(0, null);
		activeCrewmates.add(1, null);
		activeCrewmates.add(2, null);
		activeCrewmates.add(3, null);
		activeCrewmates.add(4, null);
		currentBoat.setActionHandler(actionHandler);

		//objectOverlay = new SalvagingHelperObjectOverlay(client, this, config, )


		playerCurrentAnimation = playerLastAnimation = -1;
		playerCurrentActivity = playerLastActivity = Activity.IDLE;

		// Draw graphics
		overlayManager.add(itemOverlay);
		overlayManager.add(debugOverlay);
		overlayManager.add(objectOverlay);

		lootManager = new LootManager(this, config, configManager);
		actionHandler = new ActionHandler(this, config, client, activeCrewmates, objectOverlay, currentBoat);

		//final SalvagingHelperPanel sidePanel = new SalvagingHelperPanel(this, config, client, actionHandler, itemManager);
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
		log.debug("Plugin stopped");
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

	private void debugWatch(String varType, int varId, String varName, int val, List<Integer> blacklist) {
		if (!debug) { return; }
		if (blacklist.contains(val)) {
			return;
		} else {
			sendChatMessage("Watched "+varType+" "+varName+" ["+varId+"] set: "+val);
		}
	}

	public void sendChatMessage(String chatMessage) {
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
					sendChatMessage("Crystal extractor's animation is being weird...");
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
		int vpId = event.getVarpId();
		int val = event.getValue();

		if (vbId < 0) {
			switch (Integer.parseInt(VPLT.toGameVal(vpId))) {
				case VARBIT_WHITELIST:
					//debugLog("|", List.of(""+vbId, VarbNameTable.toGameVal(vbId), ""+vpId, VarpNameTable.toGameVal(vpId), ""+event.getValue()), this);
					switch (vpId) {
						case VarPlayerID.SAILING_SIDEPANEL_BOAT_TYPE:
							sendChatMessage("Varp SAILING_SIDEPANEL_BOAT_TYPE [5117] set to "+val+" - figure it out please");
							return;
						case VarPlayerID.SAILING_CREW_HELD_CARGO_0:
						case VarPlayerID.SAILING_CREW_HELD_CARGO_1:
						case VarPlayerID.SAILING_CREW_HELD_CARGO_2:
						case VarPlayerID.SAILING_CREW_HELD_CARGO_3:
						case VarPlayerID.SAILING_CREW_HELD_CARGO_4:
						case VarPlayerID.SAILING_CREW_HELD_CARGO_BOAT_INV:
						case VarPlayerID.SAILING_BOAT_CARGOHOLD_INV:
							// These look important but I haven't seen them ever get set, so let's just watch them for now
							debugWatch("varp", vpId, VarpNameTable.toGameVal(vpId), val, List.of(-1));
							return;
						default:
							return;
					}
                case VARBIT_BLACKLIST:
					//log.debug(event.toString()+" hit VARBIT_BLACKLIST");
					return;
				case VARBIT_SAILING_LIST:
					//log.debug(event.toString()+" hit VARBIT_SAILINGLIST");
					return;
				default:
					sendChatMessage("Unexpected varp "+VarpNameTable.toGameVal(vpId)+" ["+vpId+"] set to "+val);
					return;
					//debugLog("|", List.of(""+vbId, VarbNameTable.toGameVal(vbId), ""+vpId, VarpNameTable.toGameVal(vpId), ""+event.getValue(), "|New"), this);
			}
		} else {
			switch (Integer.parseInt(VBLT.toGameVal(vbId))) {
				case VARBIT_WHITELIST:
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
							currentBoat.setOwned( (val > 0) );
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
							//debugLog(List.of(""+vbId, VarbNameTable.toGameVal(vbId), ""+vpId, VarpNameTable.toGameVal(vpId), ""+event.getValue(), "|New"), this);
					}
				case VARBIT_BLACKLIST:
					return;
				case VARBIT_SAILING_LIST:
					return;
				default:
					sendChatMessage("Unexpected varb "+VarbNameTable.toGameVal(vbId)+" ["+vbId+"] set to "+val);
					//debugLog("|", List.of(""+vbId, VarbNameTable.toGameVal(vbId), ""+vpId, VarpNameTable.toGameVal(vpId), ""+event.getValue(), "|New"), this);
			}
		}
	}

	// TODO: fix/delete?
	@Subscribe
	private void onWorldViewLoaded(WorldViewLoaded worldLoad) {

		if (worldLoad.getWorldView().getId()==-1) {
			// Need to recalculate local points
			//sendChatMessage("Rebuilding shipwreck cache on worldview -1 refresh.");
			//actionHandler.rebuild(this, client);
		}
		//objectOverlay.buildEnvObjects(this, client, client.getLocalPlayer());
		//WorldView wv = worldLoad.getWorldView();
/*		WorldView wv = client.getLocalPlayer().getWorldView();
		debugLog(Arrays.asList("Player worldview: "+wv.getId()+", map regions "+wv.getMapRegions()[0]), this);
		Scene scene = wv.getScene();
		Tile[][][] tileSet = scene.getExtendedTiles();

		List<TileObject> tileObjects = new ArrayList<>();
		for (Tile[] tileSet2 : tileSet[1]) {
			for (Tile tile : tileSet2) {
				if (tile != null) {
					//sendChatMessage("Tile: "+tile.getWorldLocation().toString());
					for (GameObject obj : tile.getGameObjects()) {
						//sendChatMessage("id "+obj.getId()+", x "+obj.getX()+", y "+obj.getY());
						if (obj != null) {
							//sendChatMessage("id "+obj.getId()+", x "+obj.getX()+", y "+obj.getY());
							tileObjects.add(obj);
						}
					}
				}
			}
		}*/

/*		int[] impIds = client.getObjectDefinition(60478).getImpostorIds();
		if (impIds != null) {
			for (int id : impIds) {
				sendChatMessage(id+"");
			}
		}*/
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
			case 964: // ???? SAILING_BOAT_2_CARGOHOLD
				return;
			case 33732: // cargo hold
				return;
			case 33733: // cargo hold also...?
				return;
			default:
				return;
		}
	}

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event) {
		//sendChatMessage("Opened widget:"+event.toString());


		// N 944.0 SailingBoatCargoholdSide.UNIVERSE
		// S 944.1 SailingBoatCargoholdSide.ITEMS; 944.1[7] = item at slot 7
		// groupId 943, 944 both seem to be set when opening/closing cargo hold? 944 has items though...?

	}

	@Subscribe
	private void onWidgetClosed(WidgetClosed event) {
		//sendChatMessage("Closed widget:"+event.toString());
	}

	@Subscribe
	private void onMenuOpened(MenuOpened event) {
		//debugLog(Arrays.asList(event.toString(), event.getFirstEntry().toString(), String.valueOf(event.getFirstEntry().isDeprioritized())), this);
	}

	@Subscribe
	private void onPostAnimation(PostAnimation event) {
		//debugLog(Arrays.asList(event.getAnimation().getId()+"", event.toString()), this);
		if (event.getAnimation().getId() == 13623) {
			actionHandler.collectShipwrecks(client, false);
		}
	}

	@Subscribe
	private void onPostObjectComposition(PostObjectComposition event) {
/*		ObjectComposition oc = event.getObjectComposition();
		if (oc.getId()==60478 || oc.getId()==60479) // TODO: add all shipwrecks
		{
			//debugLog(Arrays.asList(oc.getId()+"", oc.getImpostor().getId()+"", oc.getVarbitId()+"", oc.getVarPlayerId()+"", oc.getName()), this);
			//sendChatMessage("onPostObjectComposition fired for "+oc.getId()+" ("+oc.getName()+")");
			actionHandler.collectShipwrecks(client, false);
		}*/
	}

	@Subscribe
	private void onWorldEntitySpawned(WorldEntitySpawned event) {
		WorldEntity entity = event.getWorldEntity();
		if (entity.getOwnerType()==WorldEntity.OWNER_TYPE_SELF_PLAYER) {
			currentBoat.setBoatEntity(entity);
		}
	}

	@Subscribe
	private void onChatMessage(final ChatMessage event) {
		// TODO
	}

	@Subscribe
	private void onFocusChanged(final FocusChanged event) {
		/*notificationManager.setClientFocused(event.isFocused());
		notificationManager.setLastFocusChange(Instant.now());
		if (event.isFocused()) {
			notificationManager.clearNotifications();
		}*/
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event) {
		if (event.getGameState()==GameState.LOGIN_SCREEN || event.getGameState()==GameState.HOPPING) {
			// clear all our cached data
			actionHandler.inactiveShipwrecks.clear();
			actionHandler.activeShipwrecks.clear();
			actionHandler.objectHighlightMap.clear();
			actionHandler.npcHighlightMap.clear();
			sendChatMessage("Clearing all cached objects and NPCs.");
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
