package rs.winds.patches;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.FlurryOfBlows;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.monsters.SETool;

@SpirePatch(clz = ChangeStanceAction.class, method = "update")
public class ChangeStanceActionPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void Prefix(AbstractGameAction _inst) {
        AbstractPlayer p = AbstractDungeon.player;
        if (!p.drawPile.isEmpty()) {
            for (AbstractCard card : p.drawPile.group) {
                if (card instanceof FlurryOfBlows) {
                    boolean modified = SETool.GetEditor(card).canModify();
                    if (modified) {
                        LMSK.AddToBot(new QuickAction(() -> {
                            if (p.drawPile.contains(card) && p.hand.size() < BaseMod.MAX_HAND_SIZE) {
                                p.drawPile.removeCard(card);
                                p.hand.addToHand(card);
                                card.unhover();
                                card.untip();
                                card.setAngle(0F);
                                card.applyPowers();
                            }
                            p.hand.refreshHandLayout();
                            p.hand.glowCheck();
                        }));
                    }
                }
            }
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