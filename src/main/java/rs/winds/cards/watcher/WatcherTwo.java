package rs.winds.cards.watcher;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.stances.AbstractStance;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class WatcherTwo extends AbstractSECard {
    public static final String ID = King.MakeID("WatcherTwo");
    
    public WatcherTwo() {
        super(ID, "watcher_two", 1, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        setBlockValue(12, true);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new GainBlockAction(s, s, block));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeBaseCost(0);
            upgradeTexts();
        }
    }
    
    @Override
    public void onChangeStance(AbstractStance oldStance, AbstractStance newStance) {
        if (!inHand() && cpr().hand.size() < BaseMod.MAX_HAND_SIZE) {
            addToTop(new QuickAction(() -> {
                CardGroup group = findCardGroupWhereCardIs();
                if (group != null && group.contains(this)) {
                    group.moveToHand(this);
                    cpr().hand.glowCheck();
                }
            }));
        }
    }
    
    private CardGroup findCardGroupWhereCardIs() {
        AbstractPlayer p = LMSK.Player();
        if (p.drawPile.contains(this))
            return p.drawPile;
        if (p.discardPile.contains(this))
            return p.discardPile;
        return null;
    }
}