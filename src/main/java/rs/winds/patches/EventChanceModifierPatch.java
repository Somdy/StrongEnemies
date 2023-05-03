package rs.winds.patches;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.Ghosts;
import com.megacrit.cardcrawl.random.Random;
import javassist.CtBehavior;
import rs.lazymankits.utils.LMSK;

import java.util.ArrayList;

public class EventChanceModifierPatch {
    
    @SpirePatch2(clz = AbstractDungeon.class, method = "getEvent")
    public static class GhostsChancePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"tmp"})
        public static void Insert(Random rng, ArrayList<String> tmp) {
            if (tmp.contains(Ghosts.ID) && tmp.size() >= 2) {
                ArrayList<String> dup = new ArrayList<>(tmp);
                boolean removeGhosts = dup.remove(Ghosts.ID);
                if (removeGhosts) {
                    int originAmt = tmp.size();
                    float reciprocalTimes = 2;
                    int amount = MathUtils.ceil((reciprocalTimes - 1) * originAmt);
                    if (amount > 0) {
                        Random dupRng = rng.copy();
                        ArrayList<String> tmpCopies = new ArrayList<>();
                        for (int i = 0; i < amount; i++) {
                            String luckydog = LMSK.GetRandom(dup, dupRng).orElse(dup.get(0));
                            tmpCopies.add(luckydog);
                            tmpCopies.addAll(dup);
                        }
                        tmp.addAll(tmpCopies);
                    }
                }
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(ArrayList.class, "remove");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
}