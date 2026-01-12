package com.salvaginghelper;

import net.runelite.client.config.*;

import javax.swing.*;
import java.awt.*;

import com.salvaginghelper.ActionHandler.SalvageMode;

@ConfigGroup(SalvagingHelperConfig.GROUP)
public interface SalvagingHelperConfig extends Config
{
	String GROUP = "salvagingHelper";

    //region General
    @ConfigSection(
			name = "General",
			description = "General settings",
			position = 1)
	String generalSection = "generalSection";

	@ConfigItem(
			position = 0,
			keyName = "Test",
			name = "test",
			description = "test.",
			section = generalSection
	)
	default boolean configTest()
	{
		return false;
	}
    //endregion


    //region Loot Overlays
    @ConfigSection(
			name = "Loot Overlays",
			description = "Configure how inventory items are highlighted",
			position = 2)
	String lootSection = "lootSection";

	@ConfigItem(
			position = -5,
			keyName = "overlayOnCargoHold",
			name = "Overlay in cargo hold",
			description = "Draw overlays on items that are in the cargo hold.",
			section = lootSection
	)
	default boolean overlayOnCargoHold()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 1,
			keyName = "keepColor",
			name = "Color - Keep",
			description = "Color to highlight items to keep in the inventory.",
			section = lootSection
	)
	default Color keepColor()
	{
		return new Color(0, 0, 0, 0);
	}

	@Alpha
	@ConfigItem(
			position = 2,
			keyName = "dropColor",
			name = "Color - Drop",
			description = "Color to highlight items to drop in the inventory.",
			section = lootSection
	)
	default Color dropColor()
	{
		return new Color(255, 0, 0, 255);
	}

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "containerColor",
			name = "Color - Container",
			description = "Color to highlight items to be put in a container in the inventory.",
			section = lootSection
	)
	default Color containerColor()
	{
		return new Color(46, 255, 84, 255);
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "alchColor",
			name = "Color - Alch",
			description = "Color to highlight items to alch in the inventory.",
			section = lootSection
	)
	default Color alchColor()
	{
		return new Color(255, 255, 0, 255);
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "consumeColor",
			name = "Color - Consume",
			description = "Color to highlight items to consume in the inventory.",
			section = lootSection
	)
	default Color consumeColor()
	{
		return new Color(101, 255, 211, 255);
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "equipColor",
			name = "Color - Equip",
			description = "Color to highlight items to equip from the inventory.",
			section = lootSection
	)
	default Color equipColor()
	{
		return new Color(27, 99, 255, 255);
	}

	@Alpha
	@ConfigItem(
			position = 7,
			keyName = "processColor",
			name = "Color - Process",
			description = "Color to highlight items to process in the inventory.",
			section = lootSection
	)
	default Color processColor()
	{
		return new Color(124, 0, 142, 255);
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "cargoHoldColor",
			name = "Color - Cargo Hold",
			description = "Color to highlight items to be put into the cargo hold.",
			section = lootSection
	)
	default Color cargoHoldColor()
	{
		return new Color(255, 141, 252, 255);
	}

	@Alpha
	@ConfigItem(
			position = 9,
			keyName = "otherColor",
			name = "Color - Other",
			description = "Color to highlight items of some other yet-to-be-determined category.",
			section = lootSection
	)
	default Color otherColor()
	{
		return Color.BLACK;
	}
	//endregion

    //region Alerts - General
    @ConfigSection(
			name = "Alerts",
			description = "Configure when and how notifications fire",
			position = 3)
	String alertSection = "alertSection";

	@Alpha
	@ConfigItem(
			position = -1,
			keyName = "screenFlashColor",
			name = "Screen Flash Color",
			description = "Sets what color to use when the screen flashes.",
			section = alertSection
	)
	default Color screenFlashColor() { return new Color(255, 0, 0, 70); }

	@ConfigItem(
			position = 10,
			keyName = "levelBoostAlerts",
			name = "Level Boost Alerts",
			description = "Configures whether to alert the user when their boost is no longer active or their level is no longer high enough for the shipwreck they are salvaging.",
			section = alertSection
	)
	default boolean boostAlertsEnabled()
	{
		return true;
	}
    //endregion

    //region Idle Alerts
    @ConfigSection(
			name = "Alerts - Idle",
			description = "Configure idle alerts.",
			position = 4)
	String idleAlertsSection = "idleAlertsSection";

	@ConfigItem(
			position = 2,
			keyName = "idleScreenFlashType",
			name = "Flash",
			description = "Controls how and how long the screen flashes for during this notification type.",
			section = idleAlertsSection
	)
	default FlashNotification idleScreenFlashType() { return FlashNotification.FLASH_TWO_SECONDS; }

	@ConfigItem(
			position = 3,
			keyName = "idleTrayType",
			name = "Tray Notification",
			description = "Configures what type of tray notification to send, if any.",
			section = idleAlertsSection
	)
	default TrayIcon.MessageType idleTrayType() { return TrayIcon.MessageType.INFO; }

	@ConfigItem(
			position = 4,
			keyName = "idleFocusType",
			name = "Request Focus",
			description = "Configures whether the application should request focus.",
			section = idleAlertsSection
	)
	default RequestFocusType idleFocusType() { return RequestFocusType.REQUEST; }

	@ConfigItem(
			position = 5,
			keyName = "idleAlertSound",
			name = "Alert Sound",
			description = "What type of sound should be played when this alert is fired..",
			section = idleAlertsSection
	)
	default NotificationSound idleAlertSound() { return NotificationSound.NATIVE; }

	@Units(Units.PERCENT)
	@Range(min = 0, max = 100)
	@ConfigItem(
			position = 6,
			keyName = "idleAlertVolume",
			name = "Alert Volume",
			description = "Configures how loudly a custom sound should play.",
			section = idleAlertsSection
	)
	default int idleAlertVolume() { return 20; }

	@Range(min = -1)
	@ConfigItem(
			position = 7,
			keyName = "idleCustomSound",
			name = "Custom Sound ID",
			description = "Configures the custom sound ID to play if the alert sound chosen is 'Custom'.\n" +
					"See: https://oldschool.runescape.wiki/w/List_of_sound_IDs",
			section = idleAlertsSection
	)
	default int idleCustomSound() { return -1; }

	@ConfigItem(
			position = 8,
			keyName = "idleAlertWhileFocused",
			name = "Alert when focused",
			description = "Whether to trigger an alert even if RuneLite is your current active window.",
			section = idleAlertsSection
	)
	default boolean idleAlertWhileFocused()
	{
		return true;
	}


    //endregion

	//region Extractor Alerts
	@ConfigSection(
			name = "Alerts - Extractor",
			description = "Configure extractor alerts.",
			position = 4)
	String extractorAlertsSection = "extractorAlertsSection";



	@ConfigItem(
			position = 2,
			keyName = "extractorScreenFlashType",
			name = "Flash",
			description = "Controls how and how long the screen flashes for during this notification type.",
			section = extractorAlertsSection
	)
	default FlashNotification extractorScreenFlashType() { return FlashNotification.DISABLED; }

	@ConfigItem(
			position = 3,
			keyName = "extractorTrayType",
			name = "Tray Notification",
			description = "Configures what type of tray notification to send, if any.",
			section = extractorAlertsSection
	)
	default TrayIcon.MessageType extractorTrayType() { return TrayIcon.MessageType.NONE; }

	@ConfigItem(
			position = 4,
			keyName = "extractorFocusType",
			name = "Request Focus",
			description = "Configures whether the application should request focus.",
			section = extractorAlertsSection
	)
	default RequestFocusType extractorFocusType() { return RequestFocusType.OFF; }

	@ConfigItem(
			position = 5,
			keyName = "extractorAlertSound",
			name = "Alert Sound",
			description = "What type of sound should be played when this alert is fired.",
			section = extractorAlertsSection
	)
	default NotificationSound extractorAlertSound() { return NotificationSound.NATIVE; }

	@Units(Units.PERCENT)
	@Range(min = 0, max = 100)
	@ConfigItem(
			position = 6,
			keyName = "extractorAlertVolume",
			name = "Alert Volume",
			description = "Configures how loudly a custom sound should play.",
			section = extractorAlertsSection
	)
	default int extractorAlertVolume() { return 20; }

	@Range(min = -1)
	@ConfigItem(
			position = 7,
			keyName = "extractorCustomSound",
			name = "Custom Sound ID",
			description = "Configures the custom sound ID to play if the alert sound chosen is 'Custom'.\n" +
					"See: https://oldschool.runescape.wiki/w/List_of_sound_IDs",
			section = extractorAlertsSection
	)
	default int extractorCustomSound() { return -1; }

	@ConfigItem(
			position = 8,
			keyName = "extractorAlertWhileFocused",
			name = "Alert when focused",
			description = "Whether to trigger an alert even if RuneLite is your current active window.",
			section = extractorAlertsSection
	)
	default boolean extractorAlertWhileFocused()
	{
		return true;
	}


	//endregion

	//region Debug
	@ConfigSection(
			name = "Debug",
			description = "Configure debug overlays and messages.",
			position = 99)
	String debugSection = "debugSection";

	@ConfigItem(
			position = 2,
			keyName = "debugMode",
			name = "Debug Mode",
			description = "Configures whether to enable debug overlays and messages.",
			section = debugSection
	)
	default boolean debugModeEnabled()
	{
		return false;
	}

	@ConfigItem(
			position = 3,
			keyName = "objectLoad",
			name = "Object loading information",
			description = "Send chat messages when objects are loaded and unloaded.",
			section = debugSection
	)
	default boolean showObjectLoads()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "objectOverlays",
			name = "Object overlay information",
			description = "Show id and location information on shipwrecks and facilities.",
			section = debugSection
	)
	default boolean showObjectOverlays()
	{
		return false;
	}

	@ConfigItem(
			position = 5,
			keyName = "sidePanelMessages",
			name = "Log UI events",
			description = "Show chat messages when JPanels in the side panel UI change size or shape.",
			section = debugSection
	)
	default boolean showSidePanelMessages()
	{
		return false;
	}

	//endregion



    //region Hidden
    @ConfigItem(
			keyName = "activeTab",
			name = "Active tab",
			description = "The currently selected tab.",
			hidden = true
	)
	default JPanel activeTab()
	{
		// TODO after - put the default main JPanel here?
		return null;
	}

	@ConfigItem(
			keyName = "activeTab",
			name = "",
			description = "",
			hidden = true
	)
	void setActiveTab(JPanel t);
    //endregion

	//region Loot Containers Enabled
	@ConfigItem(
			keyName = "logBasketEnabled",
			name = "Log Basket Enabled",
			description = "Whether to use the log basket while salvaging.",
			hidden = true
	)
	default boolean logBasketEnabled() { return false; }

	@ConfigItem(
			keyName = "plankSackEnabled",
			name = "Plank Sack Enabled",
			description = "Whether to use the plank sack while salvaging.",
			hidden = true
	)
	default boolean plankSackEnabled() { return false; }

	@ConfigItem(
			keyName = "gemBagEnabled",
			name = "Gem Bag Enabled",
			description = "Whether to use the gem bag while salvaging.",
			hidden = true
	)
	default boolean gemBagEnabled() { return false; }

	@ConfigItem(
			keyName = "herbSackEnabled",
			name = "Herb Sack Enabled",
			description = "Whether to use the herb sack while salvaging.",
			hidden = true
	)
	default boolean herbSackEnabled() { return false; }

	@ConfigItem(
			keyName = "fishBarrelEnabled",
			name = "Fish Barrel Enabled",
			description = "Whether to use the fish barrel while salvaging.",
			hidden = true
	)
	default boolean fishBarrelEnabled() { return false; }

	@ConfigItem(
			keyName = "soulbearerEnabled",
			name = "Soulbearer Enabled",
			description = "Whether to use the soulbearer while salvaging.",
			hidden = true
	)
	default boolean soulbearerEnabled() { return false; }

	@ConfigItem(
			keyName = "runePouchEnabled",
			name = "Rune Pouch Enabled",
			description = "Whether to use the rune pouch while salvaging.",
			hidden = true
	)
	default boolean runePouchEnabled() { return false; }

	@ConfigItem(
			keyName = "seedBoxEnabled",
			name = "Seed Box Enabled",
			description = "Whether to use the seed box while salvaging.",
			hidden = true
	)
	default boolean seedBoxEnabled() { return false; }

	@ConfigItem(
			keyName = "coalBagEnabled",
			name = "Coal Bag Enabled",
			description = "Whether to use the coal bag while salvaging.",
			hidden = true
	)
	default boolean coalBagEnabled() { return false; }

	@ConfigItem(
			keyName = "tackleBoxEnabled",
			name = "Tackle Box Enabled",
			description = "Whether to use the tackle box while salvaging.",
			hidden = true
	)
	default boolean tackleBoxEnabled() { return false; }

	@ConfigItem(
			keyName = "huntsmanKitEnabled",
			name = "Huntsman's Kit Enabled",
			description = "Whether to use the huntsman's kit while salvaging.",
			hidden = true
	)
	default boolean huntsmanKitEnabled() { return false; }

	@ConfigItem(
			keyName = "reagentPouchEnabled",
			name = "Reagent Pouch Enabled",
			description = "Whether to use the reagent pouch while salvaging.",
			hidden = true
	)
	default boolean reagentPouchEnabled() { return false; }
	//endregion

    //region Panel Settings - Salvage Style

	@ConfigItem(
			keyName = "salvageMode",
			name = "Salvage mode",
			description = "What strategy to employ while salvaging.",
			hidden = true
	)
	default SalvageMode salvageMode() { return SalvageMode.SALVAGE_AND_SORT; }

	@ConfigItem(
			keyName = "dropAllSalvage",
			name = "Drop all salvage",
			description = "Drop all salvage instead of sorting it at a salvaging station.",
			hidden = true
	)
	default boolean dropAllSalvage() { return false; }

	@ConfigItem(
			keyName = "minMaxHookUptime",
			name = "Fill empty hook while sorting",
			description = "Prompt you to assign a crewmate to an idle hook while you're temporarily sorting.",
			hidden = true
	)
	default boolean minMaxHookUptime() { return false; }

	@ConfigItem(
			keyName = "cargoBeforeSort",
			name = "Fill cargo hold before sorting?",
			description = "Completely fill the cargo hold before sorting all your loot in one fell swoop.",
			hidden = true
	)
	default boolean cargoBeforeSort() { return false; }

	@ConfigItem(
			keyName = "dockOnFull",
			name = "[TODO] Dock when cargo hold full",
			description = "Navigate to dock when cargo hold and inventory are full. Select if you want to bank salvage " +
					"or don't have a salvaging station on your boat yet.",
			hidden = true
	)
	default boolean dockOnFull() { return false; }

    //endregion

	//region Panel Settings - General
	@ConfigItem(
			keyName = "drawShipwreckRadius",
			name = "Draw shipwreck salvage radius",
			description = "Draw a circle around a shipwreck to indicate the effective range a hook can loot it from.",
			hidden = true
	)
	default boolean drawShipwreckRadius() { return true; }

	@ConfigItem(
			keyName = "drawHookLocation",
			name = "Draw salvaging hook marker",
			description = "Draw a circle underneath boat's salvaging hooks to indicate its 'true tile' for salvaging calculations.",
			hidden = true
	)
	default boolean drawHookLocation() { return true; }

	@ConfigItem(
			keyName = "enableLootOverlays",
			name = "Enable loot overlays",
			description = "Draw overlays on inventory items based on their loot category.",
			hidden = true
	)
	default boolean enableLootOverlays()
	{
		return true;
	}

	@ConfigItem(
			keyName = "swapInvItemOptions",
			name = "Override item left-click options",
			description = "Replace inventory items' left-click options with options tailored to that item and its loot " +
					"category. Note: this will NOT affect your MenuEntrySwapper settings, and will occur only while salvaging.",
			hidden = true
	)
	default boolean swapInvItemOptions() { return true; }

	@ConfigItem(
			keyName = "idleAlerts",
			name = "Enable idle alerts",
			description = "Alert user when idle - for example, when you need to move your boat to an active shipwreck. " +
					"Customize those alerts in this plugin's Runelite configuration panel (wrench in sidebar).",
			hidden = true
	)
	default boolean idleAlertsEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "extractorAlerts",
			name = "Enable extractor alerts",
			description = "Alert when your extractor is ready to be harvested. Customize those alerts in this plugin's " +
					"Runelite configuration panel (wrench in sidebar).",
			hidden = true
	)
	default boolean extractorAlertsEnabled()
	{
		return true;
	}
	//endregion

	//region Panel Settings - Left Click Overrides

	@ConfigItem(
			keyName = "hideGroundItems",
			name = "Hide ground item left-clicks",
			description = "Prevent left-click from causing you to pick up an item on the ground while you're on your boat.",
			hidden = true
	)
	default boolean hideGroundItems() { return true; }

	@ConfigItem(
			keyName = "hideCrewmateLeftClick",
			name = "Hide crewmate left-clicks",
			description = "Deprioritize crewmates' left-click 'Command' option unless they're currently needed for " +
					"our salvaging process. This will ONLY affect left clicks while near shipwrecks.",
			hidden = true
	)
	default boolean hideCrewmateLeftClick() { return true; }

	@ConfigItem(
			keyName = "hideFacilityLeftClick",
			name = "Hide facility left-clicks",
			description = "Deprioritize non-salvaging facilities' left-click options (e.g. 'Set sails', 'Operate cannon') " +
					"unless relevant to our salvaging process. This will ONLY affect left clicks while near shipwrecks.",
			hidden = true
	)
	default boolean hideFacilityLeftClick() { return true; }

	@ConfigItem(
			keyName = "hideShipwreckInspect",
			name = "Hide shipwreck left-clicks",
			description = "Deprioritize active shipwrecks' left-click Inspect option. This prevents you from accidentally " +
					"clicking the shipwreck while trying to click a hook or cargo hold.",
			hidden = true
	)
	default boolean hideShipwreckInspect() { return true; }

	@ConfigItem(
			keyName = "hideNpcInteract",
			name = "Hide misc NPC left-clicks",
			description = "Deprioritize left-click options of NPCs in obnoxious spots - particularly the Kebbit-Monkfishes" +
					" - to prevent accidentally clicking them while salvaging. This will ONLY affect left clicks at " +
					"shipwrecks.",
			hidden = true
	)
	default boolean hideNpcInteract() { return true; }

	@ConfigItem(
			keyName = "hideCrewmateOverhead",
			name = "Hide crewmate overhead chats",
			description = "Suppress all overhead text generated by your crewmates on your ship.",
			hidden = true
	)
	default boolean hideCrewmateOverhead() { return false; }

	@ConfigItem(
			keyName = "hideOthersCrewmateOverhead",
			name = "Hide others' overhead chats",
			description = "Suppress all overhead text generated by other players' crewmates.",
			hidden = true
	)
	default boolean hideOthersCrewmateOverhead() { return true; }

	@ConfigItem(
			keyName = "drawCargoContents",
			name = "Overlay cargo contents",
			description = "Track the number of items currently in the cargo hold and overlay that number on its model.",
			hidden = true
	)
	default boolean drawCargoContents() { return true; }

    //endregion


}
