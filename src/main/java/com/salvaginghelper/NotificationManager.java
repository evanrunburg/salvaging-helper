package com.salvaginghelper;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.config.FlashNotification;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import net.runelite.client.config.RequestFocusType;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class NotificationManager {

    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    private final Client client;

    @Inject
    private Notifier notifier;

    private long cooldownLength = 2; // min seconds between alerts
    private Boolean onCooldown = false;

    private ArrayList<ScheduledFuture<?>> upcomingTasks = new ArrayList<>();

    @Getter @Setter
    private boolean clientFocused = true;
    @Getter @Setter
    private Instant lastFocusChange = Instant.now();
    @Getter
    private Instant lastMouseClick = Instant.now();
    @Getter
    private Instant lastMouseMovement = Instant.now();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // ENUMS: RequestFocusType, FlashNotification, NotificationSound

    public NotificationManager(SalvagingHelperPlugin plugin, SalvagingHelperConfig config, Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

    }

    public void triggerNotification(RequestFocusType focusType, FlashNotification flash, NotificationSound sound,
                                 int soundId, TrayIcon.MessageType trayType, float delay) {

    }

    public void playCustomSound(int soundId) {
        //
    }

    public void clearNotifications() {
        for (ScheduledFuture event : upcomingTasks) {
            event.cancel(true);
        }
    }

    public void setLastMouseClick(Instant instant) {
        lastMouseClick = instant;
        clearNotifications();
    }

    public void setLastMouseMovement(Instant instant) {
        lastMouseMovement = instant;
        if (lastMouseMovement.isAfter(Instant.now().minusMillis(600))){
            clearNotifications();
        }
    }



/*    public void sendExtractorNotification() {
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
            notifier.notify(notif, "aaa");
        }
    }*/
}
