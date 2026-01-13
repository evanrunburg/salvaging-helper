package com.salvaginghelper;

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

class SalvagingHelperItemUnderlay extends WidgetItemOverlay {

    private final SalvagingHelperPlugin plugin;
    private final SalvagingHelperConfig config;
    private final ItemManager itemManager;

    @Inject
    private SalvagingHelperItemUnderlay(ItemManager itemManager, SalvagingHelperPlugin plugin, SalvagingHelperConfig config) {
        this.itemManager = itemManager;
        this.plugin = plugin;
        this.config = config;

//        if (config.overlayOnCargoHold()) {
//            showOnInterfaces(InterfaceID.INVENTORY, InterfaceID.SAILING_BOAT_CARGOHOLD_SIDE,
//                    InterfaceID.SAILING_BOAT_CARGOHOLD);
//        } else {
//            showOnInterfaces(InterfaceID.INVENTORY, InterfaceID.SAILING_BOAT_CARGOHOLD_SIDE);
//        }

        showOnInterfaces(InterfaceID.INVENTORY, InterfaceID.SAILING_BOAT_CARGOHOLD_SIDE);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (!config.enableLootOverlays() || !plugin.onBoat) { return; }

        // Canonicalize for base item ID to match against our lists, but remember to render against actual
        // item ID for correct underlay
        int finalItemId = itemManager.canonicalize(itemId);
        Color color = plugin.lootManager.toLootColor(finalItemId);
        if (color!=null) {
            // While cargo hold is open, only render depositable items
            if (widgetItem.getWidget().getId()!=61865985 | color.equals(config.cargoHoldColor()) ) {
                Rectangle bounds = widgetItem.getCanvasBounds();
                final BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
                graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
            }
        }
    }
}