package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.stances.AbstractStance;
import javassist.CtBehavior;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSECard;

@SpirePatch(clz = ChangeStanceAction.class, method = "update")
public class PlayerSwitchStancePatch {
    @SpireInsertPatch(locator = Locator.class, localvars = {"oldStance"})
    public static void Insert(AbstractGameAction _inst, AbstractStance ___newStance, AbstractStance old) {
        for (AbstractCard card : LMSK.Player().drawPile.group) {
            if (card instanceof AbstractSECard)
                ((AbstractSECard) card).onChangeStance(old, ___newStance);
        }
        for (AbstractCard card : LMSK.Player().discardPile.group) {
            if (card instanceof AbstractSECard)
                ((AbstractSECard) card).onChangeStance(old, ___newStance);
        }
    }
    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "onStanceChange");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}