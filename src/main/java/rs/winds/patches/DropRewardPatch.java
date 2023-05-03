package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.ending.SpireShield;
import com.megacrit.cardcrawl.relics.BottledLightning;
import com.megacrit.cardcrawl.relics.Circlet;
import com.megacrit.cardcrawl.relics.PrayerWheel;
import com.megacrit.cardcrawl.relics.QuestionCard;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rs.winds.core.King;
import rs.winds.relics.SERBarricade;
import rs.winds.relics.SERInvitation;
import rs.winds.rewards.ApoReward;

public class DropRewardPatch {
    @SpirePatch2(clz = AbstractRoom.class, method = "update")
    public static class ApoRewardPatch {
        @SpireInsertPatch(rloc = 174)
        public static void Insert() {
            if (!King.ApoDropped && AbstractDungeon.getCurrRoom() instanceof MonsterRoom) {
                King.ApoDropped = true;
                AbstractDungeon.getCurrRoom().rewards.add(new ApoReward());
                AbstractDungeon.getCurrRoom().addRelicToRewards(new BottledLightning());
                AbstractDungeon.getCurrRoom().addRelicToRewards(new PrayerWheel());
                AbstractDungeon.getCurrRoom().addRelicToRewards(new SERInvitation());
            }
        }
    }
    @SpirePatch2(clz = MonsterRoomElite.class, method = "dropReward")
    public static class SpearShieldRewardPatch {
        @SpireInstrumentPatch
        public static ExprEditor Instrument() {
            return new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("addRelicToRewards".equals(m.getMethodName())) {
                        m.replace("{if(!" + King.class.getName() + ".ShieldAndSpearExisting){$_=$proceed($$);}" +
                                "else{" + SpearShieldRewardPatch.class.getName() + ".DropShieldAndSpearReward($0);}}");
                    }
                }
            };
        }
        public static void DropShieldAndSpearReward(AbstractRoom room) {
            King.ShieldAndSpearExisting = false;
            room.addRelicToRewards(new SERBarricade());
        }
    }
}