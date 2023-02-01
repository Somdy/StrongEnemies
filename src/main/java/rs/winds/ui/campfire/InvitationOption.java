package rs.winds.ui.campfire;

import basemod.BaseMod;
import basemod.CustomEventRoom;
import basemod.eventUtil.EventUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import rs.winds.core.King;
import rs.winds.events.ColosseumSE;

public class InvitationOption extends AbstractCampfireOption {
    private static final Texture IMAGE = ImageMaster.loadImage("SEAssets/images/ui/campfire/invitation_btn.png");
    private static String eventName = null;
    
    public InvitationOption(boolean active) {
        label = "进入竞技场";
        description = "进入竞技场";
        usable = active;
        img = IMAGE;
    }
    
    @Override
    public void useOption() {
        if (usable) {
            usable = false;
            AbstractDungeon.effectList.add(new AbstractGameEffect() {
                @Override
                public void update() {
                    isDone = true;
                    if (eventName == null) {
                        for (String s : EventUtils.getDungeonEvents(AbstractDungeon.id).keySet()) {
                            if (s.contains(ColosseumSE.ID)) {
                                eventName = s;
                                King.Log("Find event [" + eventName + "]");
                                break;
                            }
                        }
                        if (eventName == null) eventName = ColosseumSE.ID;
                    }
                    boolean keyNotExisted;
                    try {
                        AbstractEvent event = EventHelper.getEvent(eventName);
                        keyNotExisted = event == null;
                    } catch (Exception e) {
                        keyNotExisted = true;
                    }
                    if (keyNotExisted) {
                        King.Log("No event associated with event key [" + eventName + "]");
                    }
                    RoomEventDialog.optionList.clear();
                    AbstractDungeon.eventList.add(0, eventName);
                    King.Log("Adding event key to event list: " + AbstractDungeon.eventList.get(0));
                    MapRoomNode currNode = AbstractDungeon.getCurrMapNode();
                    MapRoomNode node = new MapRoomNode(currNode.x, currNode.y);
                    node.setRoom(!keyNotExisted ? new CustomEventRoom() : new CustomEventRoom(){
                        @Override
                        public void onPlayerEntry() {
                            AbstractDungeon.overlayMenu.proceedButton.hide();
                            event = EventUtils.getEvent(eventName);
                            event.onEnterRoom();
                        }
                    });
                    for (MapEdge e : currNode.getEdges()) {
                        node.addEdge(e);
                    }
                    AbstractDungeon.previousScreen = null;
                    AbstractDungeon.dynamicBanner.hide();
                    AbstractDungeon.dungeonMapScreen.closeInstantly();
                    AbstractDungeon.closeCurrentScreen();
                    AbstractDungeon.topPanel.unhoverHitboxes();
                    AbstractDungeon.fadeIn();
                    AbstractDungeon.topLevelEffects.clear();
                    AbstractDungeon.topLevelEffectsQueue.clear();
                    AbstractDungeon.effectsQueue.clear();
                    AbstractDungeon.dungeonMapScreen.dismissable = true;
                    AbstractDungeon.nextRoom = node;
                    AbstractDungeon.setCurrMapNode(node);
                    AbstractDungeon.getCurrRoom().onPlayerEntry();
                    AbstractDungeon.scene.nextRoom(node.room);
                    AbstractDungeon.rs = (node.room.event instanceof AbstractImageEvent) ? AbstractDungeon.RenderScene.EVENT : AbstractDungeon.RenderScene.NORMAL;
                }
    
                @Override
                public void render(SpriteBatch sb) {}
    
                @Override
                public void dispose() {}
            });
        }
    }
}