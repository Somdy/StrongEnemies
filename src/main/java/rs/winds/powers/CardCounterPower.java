package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class CardCounterPower extends AbstractSEPower {
    public static final String ID = King.MakeID("CardCounterPower");
    private final int counter;
    
    public CardCounterPower(AbstractCreature owner, int counter) {
        super(ID, "draw", PowerType.BUFF, owner);
        setValues(0);
        this.counter = counter;
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card != null) {
            amount++;
        }
        if (amount % counter == 0) {
            amount = 0;
            addToBot(new MakeTempCardInDrawPileAction(new Pain(), 1, true, true));
            addToBot(new MakeTempCardInDrawPileAction(new Slimed(), 2, true, true));
        }
    }
    
    @Override
    public void atStartOfTurn() {
        amount = 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}