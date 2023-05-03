package rs.winds.cards.curse;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.curses.Regret;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CtBehavior;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class CurseOfGod extends AbstractSECard {
    public static final String ID = King.MakeID("CurseOfGod");
    public static final CardStrings strings = CardCrawlGame.languagePack.getCardStrings(ID);
    
    public CurseOfGod() {
        super(ID, "godcurse", -1, CardType.CURSE, CardColor.CURSE, CardRarity.CURSE, CardTarget.NONE);
        exhaust = true;
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new LoseEnergyAction(EnergyPanel.getCurrentEnergy()));
    }
    
    @Override
    public void upgrade() {}
    
    @Override
    public void onPlayCard(AbstractCard c, AbstractMonster m) {
        if (inHand()) {
            addToBot(new MakeTempCardInDrawPileAction(new Regret(), 1, true, true));
        }
    }
    
    @SpirePatch(clz = CardGroup.class, method = "moveToExhaustPile")
    public static class MoveToExhaustPilePatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(CardGroup _inst, @ByRef AbstractCard[] c) {
            if (c[0] instanceof CurseOfGod)
                c[0] = new Regret();
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(CardGroup.class, "resetCardBeforeMoving");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
}
