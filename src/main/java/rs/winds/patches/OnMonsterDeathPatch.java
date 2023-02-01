package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtBehavior;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;

@SpirePatch(clz = AbstractMonster.class, method = "die", paramtypez = {boolean.class})
public class OnMonsterDeathPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void Insert(AbstractMonster __instance, boolean t) {
        if (t) {
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> m != __instance)) {
                for (AbstractPower p : m.powers) {
                    if (p instanceof AbstractSEPower)
                        ((AbstractSEPower) p).onMonsterDeath(__instance);
                }
            }
        }
    }
    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(MonsterGroup.class, "areMonstersBasicallyDead");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}