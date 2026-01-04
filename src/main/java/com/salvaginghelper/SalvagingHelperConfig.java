package com.salvaginghelper;

import net.runelite.client.config.*;

import javax.swing.*;
import java.awt.*;

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
    //endregion


    //region Loot Overlays
    @ConfigSection(
			name = "Loot Overlays",
			description = "Configure how inventory items are highlighted",
			position = 2)
	String lootSection = "lootSection";

	@ConfigItem(
			position = 0,
			keyName = "enableLootOverlays",
			name = "Enable loot overlays",
			description = "Draw overlays on inventory items depending on their loot category.",
			section = lootSection
	)
	default boolean enableLootOverlays()
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
			position = 1,
			keyName = "idleAlerts",
			name = "Enabled",
			description = "Whether to alert the user when idle.",
			section = idleAlertsSection
	)
	default boolean idleAlertsEnabled()
	{
		return true;
	}

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
			position = 1,
			keyName = "extractorAlerts",
			name = "Enabled",
			description = "Whether to alert the user when extractor is ready to be harvested.",
			section = extractorAlertsSection
	)
	default boolean extractorAlertsEnabled()
	{
		return true;
	}

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


}
