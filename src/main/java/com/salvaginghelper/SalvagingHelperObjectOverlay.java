package com.salvaginghelper;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SalvagingHelperObjectOverlay extends Overlay {

    private final Client client;
    private final SalvagingHelperPlugin plugin;
    private final ModelOutlineRenderer outlineRenderer;
    private final SalvagingHelperConfig config;
    //private List<GameObject> gameObjectSet = new ArrayList<>();
    private ConcurrentHashMap<GameObject, Color> gameObjectsToHighlight = new ConcurrentHashMap<>();
    private ConcurrentHashMap<NPC, Color> npcsToHighlight = new ConcurrentHashMap<>();
    private final int hookRenderRadius = 2500;
    public final int salvageRadius = 1150;
    private Boolean renderCargoOverlayText = true;

    @Inject
    private SalvagingHelperObjectOverlay(Client client, SalvagingHelperPlugin plugin, SalvagingHelperConfig config, ModelOutlineRenderer outlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.outlineRenderer = outlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPriority(PRIORITY_HIGH);
    }

/*    public void buildEnvObjects(SalvagingHelperPlugin plugin, Client client, Player player) {

        // Don't rebuild too often - final cooldown length TBD
        // Need a grace period for login moment/startup as many objects stream in
        long timeSinceRebuild = Duration.between(lastRebuildTimestamp, Instant.now()).getSeconds();
        long timeSinceStart = Duration.between(plugin.startTime, Instant.now()).getSeconds();
        plugin.sendChatMessage("Time since rebuild: "+timeSinceRebuild+" seconds");
        if (timeSinceRebuild < cooldownInSeconds && timeSinceStart > 5) {
            return;
        }

        // Update the object and npc sets all at once later to prevent NullPointerException when
        // renderer tries to read them
        List<GameObject> tempGameObjectSet = new ArrayList<>();

        // Player's boat and player's overworld position are stored in completely different areas of the
        // map, so we need to do two passes of the possible game objects, one from player POV and one
        // from the client camera's POV.
        //
        // Boat's always at height 1 and sea 0, so we can cheat a bit
        Tile[][] salvagingTiles1 = player.getWorldView().getScene().getExtendedTiles()[1];
        for (Tile[] rowOfTiles : salvagingTiles1) {
            if (rowOfTiles==null) { continue; } // java is a Perfect Language with No Flaws
            for (Tile singleTile : rowOfTiles) {
                if (singleTile==null) { continue; }
                for (GameObject gameObject : singleTile.getGameObjects()) {
                    if (gameObject==null) { continue; }
                    //plugin.sendChatMessage("Game object "+gameObject.getId()+" at ("+gameObject.getX()+", "+gameObject.getY()+").");
                    tempGameObjectSet.add(gameObject);
                }
            }
        }
        Tile[][] salvagingTiles2 = client.getTopLevelWorldView().getScene().getExtendedTiles()[0];
        for (Tile[] rowOfTiles : salvagingTiles2) {
            if (rowOfTiles==null) { continue; } // java is a Perfect Language with No Flaws
            for (Tile singleTile : rowOfTiles) {
                if (singleTile==null) { continue; }
                for (GameObject gameObject : singleTile.getGameObjects()) {
                    if (gameObject==null) { continue; }
                    //plugin.sendChatMessage("Game object "+gameObject.getId()+" at ("+gameObject.getX()+", "+gameObject.getY()+").");
                    tempGameObjectSet.add(gameObject);
                }
            }
        }

        plugin.sendChatMessage("Object cache built with size "+tempGameObjectSet.size(), false);
        //plugin.actionHandler.processObject(null, tempGameObjectSet, plugin.ObjectTable);
        //lastRebuildTimestamp = Instant.now();

    }

    public void updateEnvObject(SalvagingHelperPlugin plugin, GameObject object, boolean shouldAdd) {
        if (shouldAdd) {
            gameObjectSet.add(object);
        } else {
            gameObjectSet.remove(object);
        }
    }*/

    // Get this tick's object-color pairs from ActionHandler
    @SuppressWarnings("unchecked")
    public void setGameObjHighlights(ConcurrentHashMap<GameObject, Color> newMap) {
        if (newMap != null) {
            //gameObjectsToHighlight = (ConcurrentHashMap<GameObject, Color>) newMap.clone();
            gameObjectsToHighlight = (ConcurrentHashMap<GameObject, Color>) newMap;
        }
    }

    // Get this tick's npc-color pairs from ActionHandler
    @SuppressWarnings("unchecked")
    public void setNPCHighlights(ConcurrentHashMap<NPC, Color> newMap) {
        if (newMap != null) {
            //npcsToHighlight = (HashMap<NPC, Color>) newMap.clone();
        }
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if (!plugin.onBoat) { return null; }

        Boat b = plugin.boat;
        graphics.setFont(FontManager.getRunescapeFont());
        WorldEntity boatEntity = b.getBoatEntity();


        // game objects - facilities
        for (GameObject obj : gameObjectsToHighlight.keySet()) {
            if (obj == null || gameObjectsToHighlight.get(obj)==null) { continue; } // I LOVE JAVA!!!!!
            try {
                outlineRenderer.drawOutline(obj, 3, gameObjectsToHighlight.getOrDefault(obj, Color.BLACK), 5);
            } catch (NullPointerException e) {
                if (config.debugModeEnabled() && config.showObjectLoads()) {
                    plugin.sendChatMessage("Error rendering overlay on "+client.getObjectDefinition(obj.getId()).getName()
                            +" ("+obj.getId()+") at ("+obj.getLocalLocation().getX()+", "+obj.getLocalLocation().getY()+"). Deleting.", false);
                }
                plugin.actionHandler.deleteObject(obj);
                setGameObjHighlights(plugin.actionHandler.objectHighlightMap);
                plugin.actionHandler.flagRebuild = true;
            }
        }

        // TODO - does this not ever get populated? wtf?
        // crewmates
        for (NPC npc : npcsToHighlight.keySet()) {
            if (npc == null || npcsToHighlight.get(npc)==null) { continue; }
            try {
                outlineRenderer.drawOutline(npc, 5, npcsToHighlight.getOrDefault(npc, Color.BLACK), 5);
            } catch (NullPointerException e) {
                plugin.sendChatMessage("Error rendering overlay on "+npc.getName()+" ["+npc.getId()+"] at "
                        +npc.getLocalLocation().toString(), false);
            }
        }

        // shipwrecks
        LocalPoint player = boatEntity.transformToMainWorld(client.getLocalPlayer().getLocalLocation());
        int renderThreshold = 10000;
        for (GameObject wreck : plugin.actionHandler.activeShipwrecks) {
            LocalPoint wp = wreck.getLocalLocation();
            if (player.distanceTo(wp) < renderThreshold) {
                drawShipwreckRange(graphics, client, salvageRadius, wp, new Color(129, 255, 148));
                if (config.debugModeEnabled() && config.showObjectOverlays()) {
                    renderText(graphics, wp, wreck.getId()+": (x="+wp.getX()+", y="+wp.getY()+")  A: "+plugin.actionHandler.getObjectAnimation(wreck), Color.MAGENTA, 0);
                }
            }
        }
        for (GameObject wreck : plugin.actionHandler.inactiveShipwrecks) {
            LocalPoint wp = wreck.getLocalLocation();
            if (player.distanceTo(wp) < renderThreshold) {
                drawShipwreckRange(graphics, client, salvageRadius, wp, new Color(255, 172, 172, 128));
                if (config.debugModeEnabled() && config.showObjectOverlays()) {
                    renderText(graphics, wp, wreck.getId()+": (x="+wp.getX()+", y="+wp.getY()+")  A: "+plugin.actionHandler.getObjectAnimation(wreck), new Color(255, 172, 172, 128), 0);
                }
            }
        }

        // Salvaging hook underlays
        int w = 8, h = 8;
        drawHookOverlay(graphics, b.getHookPort(), boatEntity, w, h);
        drawHookOverlay(graphics, b.getHookStarboard(), boatEntity, w, h);

        // cargo hold
        if (config.drawCargoContents() && renderCargoOverlayText && b.getCargoHold() != null) {
            GameObject cargoHold = b.getCargoHold();
            int items = b.getItemsInHold();
            String str = (items>0) ? items + " / " + b.getCargoHoldCapacity() : "?";
            Point p = Perspective.getCanvasTextLocation(client, graphics, boatEntity.transformToMainWorld(cargoHold.getLocalLocation()), str, 1);
            OverlayUtil.renderTextLocation(graphics, p, str,
                    (items==b.getCargoHoldCapacity()) ? Color.RED : config.cargoHoldColor());
        }



        return null;
    }

    public int getClosestShipwreckDistance(LocalPoint pointWE) {
        int smallestDistance = 100000;
        for (GameObject shipwreck : plugin.actionHandler.activeShipwrecks) {
            LocalPoint local = shipwreck.getLocalLocation();
            int dist_int = pointWE.distanceTo(local);
            if (dist_int < smallestDistance) {
                smallestDistance = dist_int;
                //dist = dist_int+" (@ "+local.getX()+", "+local.getY()+")";
            }
        }
        return smallestDistance;
    }

    public void drawShipwreckRange(Graphics2D graphics, Client client, int r, LocalPoint center, Color color) {
        // https://spencermortensen.com/articles/bezier-circle/
        //double a=1.00005507808;
        double b = 0.55342925736;
        //double c=0.99873327689;
        // a & c don't make much of a difference, so we'll exclude them for a teeny bit of performance

        if (!config.drawShipwreckRadius()){ return; }

        graphics.setColor(color);

        //Point oPoint = Perspective.localToCanvas(plugin.client, center, 0, 0);
        Point wPoint = Perspective.localToCanvas(client, center.plus(-1*r, 0), 0, 0);
        Point nPoint = Perspective.localToCanvas(client, center.plus(0, r), 0, 0);
        Point ePoint = Perspective.localToCanvas(client, center.plus(r, 0), 0, 0);
        Point sPoint = Perspective.localToCanvas(client, center.plus(0, -1*r), 0, 0);
        Point nw1 = Perspective.localToCanvas(client, center.plus(-1*r, (int) (r*b)), 0, 0);
        Point nw2 = Perspective.localToCanvas(client, center.plus((int) (-1*r*b), r), 0, 0);
        Point ne1 = Perspective.localToCanvas(client, center.plus((int) (r*b), r), 0, 0);
        Point ne2 = Perspective.localToCanvas(client, center.plus(r, (int) (r*b)), 0, 0);
        Point se1 = Perspective.localToCanvas(client, center.plus(r, (int) (-1*r*b)), 0, 0);
        Point se2 = Perspective.localToCanvas(client, center.plus((int) (1*r*b), -1*r), 0, 0);
        Point sw1 = Perspective.localToCanvas(client, center.plus((int) (-1*r*b), -1*r), 0, 0);
        Point sw2 = Perspective.localToCanvas(client, center.plus(-1*r, (int) (-1*r*b)), 0, 0);

        if (wPoint!=null && nPoint!=null && ePoint!=null && sPoint!=null && nw1!=null && nw2!=null && ne1!=null && ne2!=null &&
                se1!=null && se2!=null && sw1!=null && sw2!=null) { // smile

            CubicCurve2D.Double nwCurve = new CubicCurve2D.Double(wPoint.getX(), wPoint.getY(), nw1.getX(), nw1.getY(), nw2.getX(),
                    nw2.getY(), nPoint.getX(), nPoint.getY());
            CubicCurve2D.Double neCurve = new CubicCurve2D.Double(nPoint.getX(), nPoint.getY(), ne1.getX(), ne1.getY(), ne2.getX(),
                    ne2.getY(), ePoint.getX(), ePoint.getY());
            CubicCurve2D.Double seCurve = new CubicCurve2D.Double(ePoint.getX(), ePoint.getY(), se1.getX(), se1.getY(), se2.getX(),
                    se2.getY(), sPoint.getX(), sPoint.getY());
            CubicCurve2D.Double swCurve = new CubicCurve2D.Double(sPoint.getX(), sPoint.getY(), sw1.getX(), sw1.getY(), sw2.getX(),
                    sw2.getY(), wPoint.getX(), wPoint.getY());

            graphics.draw(nwCurve);
            graphics.draw(neCurve);
            graphics.draw(seCurve);
            graphics.draw(swCurve);

/*            OverlayUtil.renderTextLocation(graphics, wPoint, "w", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, nw1, "nw1", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, nw2, "nw2", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, nPoint, "n", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, ne1, "ne1", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, ne2, "ne2", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, ePoint, "e", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, se1, "se1", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, se2, "se2", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, sPoint, "s", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, sw1, "sw1", Color.GREEN);
            OverlayUtil.renderTextLocation(graphics, sw2, "sw2", Color.GREEN);*/
        }
    }

    public void renderText(Graphics2D graphics, LocalPoint local, String textToDraw, Color color, int zOffset) {
        if (!plugin.debug) { return; }
        Point p = Perspective.getCanvasTextLocation(plugin.getClient(), graphics, local, textToDraw, zOffset);
        if (p != null) {
            OverlayUtil.renderTextLocation(graphics, p, textToDraw, color);
        }

    }

    public void drawHookOverlay(Graphics2D graphics, GameObject hook, WorldEntity boatEntity, int w, int h) {

        if (!config.drawHookLocation()){
            return;
        }

        if (hook != null && (hook.getWorldView()==boatEntity.getWorldView())) { // Java is a Perfect Language with No Flaws :)
            LocalPoint objWE = boatEntity.transformToMainWorld(hook.getLocalLocation());
            Point hook1Center = Perspective.localToCanvas(plugin.getClient(), objWE, 0, 0);
            int minD = getClosestShipwreckDistance(objWE);
            Color hookColor = (minD<salvageRadius) ? new Color(0, 255, 0, 150) : new Color(255, 135, 135);
            graphics.setColor(hookColor);
            if (minD < hookRenderRadius) {
                graphics.fillOval(hook1Center.getX() - w/2, hook1Center.getY() - h/2, w, h);
                if (plugin.debug && config.showObjectOverlays()) {
                    OverlayUtil.renderTileOverlay(graphics, hook, hook.getId()+"  (x="+objWE.getX()+", y="+objWE.getY()+"), min D="+minD, (minD<salvageRadius) ? Color.GREEN : new Color(255, 135, 135, 100));
                }
            }
        }
    }
    
    public void disableCargoTracking() {
        renderCargoOverlayText = false;
    }

    public void enableCargoTracking() {
        renderCargoOverlayText = true;
    }


    //public
}
