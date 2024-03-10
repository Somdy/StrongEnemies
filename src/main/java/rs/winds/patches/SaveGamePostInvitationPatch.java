package rs.winds.patches;

import basemod.CustomEventRoom;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;
import rs.winds.core.King;
import rs.winds.rooms.SEColosseumEventRoom;

public class SaveGamePostInvitationPatch {
//    @SpirePatch(clz = AbstractRoom.class, method = "update")
//    public static class RoomSavePatch {
//        @SpireInsertPatch(locator = Locator1.class, localvars = {"saveFile"})
//        public static void Insert1(AbstractRoom _inst, SaveFile sf) throws Exception {
//            MapRoomNode currNode = AbstractDungeon.getCurrMapNode();
//            King.Log("Game saved. Current position: " + currNode.x + ", " + currNode.y);
//            showSaveFile(sf);
//        }
//        private static class Locator1 extends SpireInsertLocator {
//            @Override
//            public int[] Locate(CtBehavior ctBehavior) throws Exception {
//                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(SaveAndContinue.class, "save");
//                return LineFinder.findInOrder(ctBehavior, matcher);
//            }
//        }
//        
//        private static void showSaveFile(SaveFile sf) {
//            String sb = "current room: " + sf.current_room + "\n" +
//                    "post combat: " + sf.post_combat + "\n";
//            King.Log(sb);
//        }
//    
//        @SpireInsertPatch(locator = Locator2.class)
//        public static void Insert2(AbstractRoom _inst) throws Exception {
//            King.Log("reward allowed: " + _inst.rewardAllowed);
//        }
//    
//        private static class Locator2 extends SpireInsertLocator {
//            @Override
//            public int[] Locate(CtBehavior ctBehavior) throws Exception {
//                Matcher.FieldAccessMatcher matcher = new Matcher.FieldAccessMatcher(AbstractRoom.class, "rewardAllowed");
//                return LineFinder.findInOrder(ctBehavior, matcher);
//            }
//        }
//    }
    
    @SpirePatch(clz = AbstractDungeon.class, method = "populatePathTaken")
    public static class PopulatePathOnLoadingSavePatch {
        @SpireInsertPatch(rloc = 0)
        public static void Insert(AbstractDungeon _inst, SaveFile sf) {
            String cern = SEColosseumEventRoom.class.getName();
            if (cern.equals(sf.current_room)) {
                MapRoomNode node = AbstractDungeon.map.get(sf.room_y).get(sf.room_x);
                if (!cern.equals(node.room.getClass().getName())) {
                    node.room = new EventRoom();
                }
            }
        }
    }
    
    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = {SaveFile.class})
    public static class RoomTransitionOnLoadingSavePatch {
        @SpireInsertPatch(rloc = 2239 - 2126, localvars = {"isLoadingPostCombatSave", "isLoadingCompletedEvent"})
        public static void Insert(AbstractDungeon _inst, SaveFile sf, boolean isLoadingPostCombatSave, boolean isLoadingCompletedEvent) {
            King.Log("post combat: " + isLoadingPostCombatSave + ", completed event:: " + isLoadingCompletedEvent);
        }
    }
}