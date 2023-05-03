package rs.winds.cards.silent;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

import java.util.ArrayList;
import java.util.List;

public class AlwaysPrepared extends AbstractSECard {
    public static final String ID = King.MakeID("AlwaysPrepared");
    private List<AbstractCard> discards = new ArrayList<>();
    
    public AlwaysPrepared() {
        super(ID, "prepared", 0, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.NONE);
        magicNumber = baseMagicNumber = 1;
        exhaust = true;
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        discards.clear();
        addToBot(new DrawCardAction(s, magicNumber));
        addToBot(new SimpleHandCardSelectBuilder(c -> true)
                .setCanPickZero(false).setAnyNumber(false)
                .setAmount(magicNumber).setMsg(DiscardAction.TEXT[0]).setManipulator(new HandCardManipulator() {
                    @Override
                    public boolean manipulate(AbstractCard card, int i) {
                        discards.add(card);
                        cpr().hand.moveToDiscardPile(card);
                        card.triggerOnManualDiscard();
                        GameActionManager.incrementDiscard(false);
                        return false;
                    }
                }).setFollowUpAction(new QuickAction(() -> {
                    for (AbstractCard card : discards.toArray(new AbstractCard[0])) {
                        if (cpr().discardPile.contains(card)) {
                            discards.remove(card);
                            cpr().discardPile.removeCard(card);
                            addToTop(new NewQueueCardAction(card, true, true, true));
                        }
                    }
                })));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }
}