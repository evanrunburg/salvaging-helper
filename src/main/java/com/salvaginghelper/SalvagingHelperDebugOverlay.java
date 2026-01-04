package com.salvaginghelper;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;

public class SalvagingHelperDebugOverlay extends Overlay {

    private final SalvagingHelperPlugin plugin;
    //private final SalvagingHelperConfig config;
    public ArrayList<GameObject> objectsToOverlay = new ArrayList<>();

    @Inject
    public SalvagingHelperDebugOverlay(final SalvagingHelperPlugin plugin)
    {
        this.plugin = plugin;
        //this.config = plugin.provideConfig()
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_HIGH);
        setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        int thickness = 4;
        int offset = 2;
        int width = 300;
        int height = 100;

        Client client = plugin.getClient();
        WorldView clientWorldview = client.getTopLevelWorldView();
        Player player = client.getLocalPlayer();
        WorldView playerwv = player.getWorldView();

        graphics.setFont(FontManager.getRunescapeFont());
        //Color debugOverlayColor = new Color(185, 0, 201, 1);
        Color debugOverlayColor = Color.MAGENTA;

        if (plugin.debug && plugin.onBoat) {
            OverlayUtil.renderTextLocation(graphics, new Point(10,50), "SALVAGING HELPER DEBUG OVERLAY", debugOverlayColor);
            for (int i=0; i<5; i++) {
                Crewmate mate = plugin.activeCrewmates.get(i);

                if (mate==null) {
                    OverlayUtil.renderTextLocation(graphics, new Point(10,50 + 16*(i+2)), i+1+". Empty", debugOverlayColor);
                } else {
                    LocalPoint loc = (mate.getCrewmateNpc() != null) ? plugin.boat.getBoatEntity().transformToMainWorld(mate.getCrewmateNpc().getLocalLocation()) : null;
                    OverlayUtil.renderTextLocation(graphics, new Point(10,50 + 16*(i+2)), i+1+". "+mate.getName(), debugOverlayColor);
                    OverlayUtil.renderTextLocation(graphics, new Point(140,50 + 16*(i+2)), String.valueOf(mate.getCurrentStatus()), debugOverlayColor);
                    OverlayUtil.renderTextLocation(graphics, new Point(220,50 + 16*(i+2)), mate.mapAssignedStation(mate.getAssignedStationNumber()), debugOverlayColor);
                    OverlayUtil.renderTextLocation(graphics, new Point(345,50 + 16*(i+2)), "NPC "+(mate.getCrewmateNpc() == null ? "not found" : "ID: "+mate.getCrewmateNpc().getId()+
                            " @ ("+loc.getX()+", "+loc.getY()+")"), debugOverlayColor);
                }
            }
            OverlayUtil.renderTextLocation(graphics, new Point(10,184), "Current activity: "+plugin.playerCurrentActivity, debugOverlayColor);
            OverlayUtil.renderTextLocation(graphics, new Point(10,200), "Last activity: "+plugin.playerLastActivity, debugOverlayColor);
            OverlayUtil.renderTextLocation(graphics, new Point(10,216), "Next task: "+plugin.directions, debugOverlayColor);
            OverlayUtil.renderTextLocation(graphics, new Point(10, 248), "Inactive shipwrecks: "+plugin.actionHandler.inactiveShipwrecks.size()+"; active shipwrecks: "+plugin.actionHandler.activeShipwrecks.size(), debugOverlayColor);
            OverlayUtil.renderTextLocation(graphics, new Point(10,264), "Tracked game objects: "+plugin.actionHandler.objectHighlightMap.size(), debugOverlayColor);
        }
        return getBounds().getSize();
    }
}
