package com.salvaginghelper;

import net.runelite.client.config.*;

import java.awt.*;

import com.salvaginghelper.ActionHandler.SalvageMode;

@ConfigGroup(SalvagingHelperConfig.GROUP)
public interface SalvagingHelperConfig extends Config
{
	//region General
	String GROUP = "salvagingHelper";

/*    @ConfigSection(
			name = "General",
			description = "General settings",
			position = 1)
	String generalSection = "generalSection";*/
    //endregion

    //region Alerts - General
/*    @ConfigSection(
			name = "Alerts",
			description = "Configure when and how notifications fire",
			position = 3)
	String alertSection = "alertSection";*/

    //endregion

    //region Alerts - Idle
/*    @ConfigSection(
			name = "Alerts - Idle",
			description = "Configure idle alerts.",
			position = 4)
	String idleAlertsSection = "idleAlertsSection";*/

	@ConfigItem(
			position = 2,
			keyName = "idleScreenFlashType",
			name = "Flash",
			description = "Controls how and how long the screen flashes for during this notification type.",
			//section = idleAlertsSection,
			hidden = true
	)
	default FlashNotification idleScreenFlashType() { return FlashNotification.FLASH_TWO_SECONDS; }

	@Alpha
	@ConfigItem(
			position = -1,
			keyName = "idleFlashColor",
			name = "Flash Color",
			description = "Sets what color and transparency level to use when the screen flashes.",
			//section = idleAlertsSection,
			hidden = true
	)
	default Color idleFlashColor() { return new Color(255, 0, 0, 70); }

	@ConfigItem(
			position = 3,
			keyName = "idleTrayType",
			name = "Tray Notification",
			description = "Configures what type of tray notification to send, if any.",
			//section = idleAlertsSection,
			hidden = true
	)
	default TrayIcon.MessageType idleTrayType() { return TrayIcon.MessageType.INFO; }

	@ConfigItem(
			position = 4,
			keyName = "idleFocusType",
			name = "Request Focus",
			description = "Configures whether the application should request focus.",
			//section = idleAlertsSection,
			hidden = true
	)
	default RequestFocusType idleFocusType() { return RequestFocusType.REQUEST; }

	@ConfigItem(
			position = 5,
			keyName = "idleAlertSound",
			name = "Alert Sound",
			description = "What type of sound should be played when this alert is fired..",
			//section = idleAlertsSection,
			hidden = true
	)
	default NotificationSound idleAlertSound() { return NotificationSound.NATIVE; }

	@Range(min = -1, max = 99999)
	@ConfigItem(
			position = 6,
			keyName = "idleCustomSound",
			name = "Custom Sound ID",
			description = "Configures the custom sound ID to play if the alert sound chosen is 'Custom'.\n" +
					"See: https://oldschool.runescape.wiki/w/List_of_sound_IDs",
			//section = idleAlertsSection,
			hidden = true
	)
	default int idleCustomSound() { return -1; }

	@Units(Units.PERCENT)
	@Range(min = 0, max = 100)
	@ConfigItem(
			position = 7,
			keyName = "idleAlertVolume",
			name = "Custom Volume",
			description = "If the client's Sound Effects volume setting is muted, this will determine how loudly a custom sound should play.",
			//section = idleAlertsSection,
			hidden = true
	)
	default int idleAlertVolume() { return 20; }

	@ConfigItem(
			position = 8,
			keyName = "idleAlertWhileFocused",
			name = "Alert when focused",
			description = "Whether to trigger an alert even if RuneLite is your current active window.",
			//section = idleAlertsSection,
			hidden = true
	)
	default boolean idleAlertWhileFocused() { return true; }
    //endregion

	//region Alerts - Extractor
/*	@ConfigSection(
			name = "Alerts - Extractor",
			description = "Configure extractor alerts.",
			position = 4)
	String extractorAlertsSection = "extractorAlertsSection";*/

	@ConfigItem(
			position = 2,
			keyName = "extractorScreenFlashType",
			name = "Flash",
			description = "Controls how and how long the screen flashes for during this notification type.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default FlashNotification extractorScreenFlashType() { return FlashNotification.DISABLED; }

	@Alpha
	@ConfigItem(
			position = -1,
			keyName = "extractorFlashColor",
			name = "Flash Color",
			description = "Sets what color and transparency level to use when the screen flashes.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default Color extractorFlashColor() { return new Color(255, 0, 0, 70); }

	@ConfigItem(
			position = 3,
			keyName = "extractorTrayType",
			name = "Tray Notification",
			description = "Configures what type of tray notification to send, if any.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default TrayIcon.MessageType extractorTrayType() { return TrayIcon.MessageType.NONE; }

	@ConfigItem(
			position = 4,
			keyName = "extractorFocusType",
			name = "Request Focus",
			description = "Configures whether the application should request focus.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default RequestFocusType extractorFocusType() { return RequestFocusType.OFF; }

	@ConfigItem(
			position = 5,
			keyName = "extractorAlertSound",
			name = "Alert Sound",
			description = "What type of sound should be played when this alert is fired.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default NotificationSound extractorAlertSound() { return NotificationSound.CUSTOM; }

	@Range(min = -1, max = 99999)
	@ConfigItem(
			position = 6,
			keyName = "extractorCustomSound",
			name = "Custom Sound ID",
			description = "Configures the custom sound ID to play if the alert sound chosen is 'Custom'.\n" +
					"See: https://oldschool.runescape.wiki/w/List_of_sound_IDs",
			//section = extractorAlertsSection,
			hidden = true
	)
	default int extractorCustomSound() { return 30; }

	@Units(Units.PERCENT)
	@Range(min = 0, max = 100)
	@ConfigItem(
			position = 7,
			keyName = "extractorAlertVolume",
			name = "Custom Volume",
			description = "If the client's Sound Effects volume setting is muted, this will determine how loudly a custom sound should play.",
			//section = extractorAlertsSection,
			hidden = true
	)
	default int extractorAlertVolume() { return 25; }

	@ConfigItem(
			position = 8,
			keyName = "extractorAlertWhileFocused",
			name = "Alert when focused",
			description = "Whether to trigger an alert even if RuneLite is your current active window.",
			//section = extractorAlertsSection,
			hidden = true
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

	//region Panel - Plugin Modules
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
			name = "Override item left-clicks",
			description = "Replace inventory items' left-click options with options tailored to that item and its loot " +
					"category. Note: this will NOT affect your MenuEntrySwapper settings, and will occur only while salvaging.",
			hidden = true
	)
	default boolean swapInvItemOptions() { return true; }

	@ConfigItem(
			keyName = "idleAlerts",
			name = "Idle alerts",
			description = "Alert user when idle - for example, when you need to move your boat to an active shipwreck.",
			hidden = true
	)
	default boolean idleAlertsEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "extractorAlerts",
			name = "Extractor alerts",
			description = "Alert when your extractor is ready to be harvested.",
			hidden = true
	)
	default boolean extractorAlertsEnabled()
	{
		return true;
	}
	//endregion

    //region Panel - Salvage Strategy

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

/*	@ConfigItem(
			keyName = "dockOnFull",
			name = "[TODO] Dock when cargo hold full",
			description = "Navigate to dock when cargo hold and inventory are full. Select if you want to bank salvage " +
					"or don't have a salvaging station on your boat yet.",
			hidden = true
	)
	default boolean dockOnFull() { return false; }*/

    //endregion



	//region Panel - Left Click Overrides

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

/*	@ConfigItem( // TODO - come back to this after 1.0
			keyName = "drawCargoContents",
			name = "Overlay cargo contents",
			description = "Track the number of items currently in the cargo hold and overlay that number on its model.",
			hidden = true
	)
	default boolean drawCargoContents() { return true; }*/

    //endregion

	//region Panel - Loot Underlays
	@ConfigSection(
			name = "See sidepanel for settings",
			description = "There used to be settings here, but now they are over there.",
			position = 2)
	String lootSection = "lootSection";

	@ConfigItem(
			position = -5,
			keyName = "overlayOnCargoHold",
			name = "Overlay in cargo hold",
			description = "Draw overlays on items that are in the cargo hold.",
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
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
			section = lootSection,
			hidden = true
	)
	default Color otherColor()
	{
		return Color.BLACK;
	}
	//endregion


}
