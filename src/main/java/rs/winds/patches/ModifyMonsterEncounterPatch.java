package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.*;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.monsters.MonsterInfo;
import com.megacrit.cardcrawl.monsters.ending.CorruptHeart;
import javassist.CtBehavior;
import rs.winds.monsters.ending.EvilGod;

import java.util.ArrayList;

public class ModifyMonsterEncounterPatch {
    @SpirePatches2({
            @SpirePatch2(clz = TheCity.class, method = "generateWeakEnemies")
    })
    public static class GenerateWeakEnemiesPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"monsters"})
        public static void Insert(ArrayList<MonsterInfo> monsters) {
            monsters.removeIf(i -> i.name.equals(MonsterHelper.THREE_BYRDS_ENC));
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(MonsterInfo.class, "normalizeWeights");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
    
    @SpirePatches2({
            @SpirePatch2(clz = Exordium.class, method = "generateStrongEnemies"),
            @SpirePatch2(clz = TheBeyond.class, method = "generateStrongEnemies")
    })
    public static class GenerateStrongEnemiesPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"monsters"})
        public static void Insert(ArrayList<MonsterInfo> monsters) {
            monsters.removeIf(i -> i.name.equals(MonsterHelper.RED_SLAVER_ENC) || i.name.equals(MonsterHelper.MAW_ENC)
                    || i.name.equals(MonsterHelper.THREE_DARKLINGS_ENC));
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(MonsterInfo.class, "normalizeWeights");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
    
    @SpirePatches2({
            @SpirePatch2(clz = TheCity.class, method = "generateElites"),
            @SpirePatch2(clz = TheBeyond.class, method = "generateElites")
    })
    public static class GenerateElitesPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"monsters"})
        public static void Insert(ArrayList<MonsterInfo> monsters) {
            monsters.removeIf(i -> i.name.equals(MonsterHelper.GREMLIN_LEADER_ENC));
            if (TheBeyond.ID.equals(AbstractDungeon.id)) {
                monsters.add(new MonsterInfo(MonsterHelper.MAW_ENC, 2F));
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(MonsterInfo.class, "normalizeWeights");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
}