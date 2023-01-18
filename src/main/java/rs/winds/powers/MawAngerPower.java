package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class MawAngerPower extends AbstractSEPower {
    public static final String ID = King.MakeID("MawAngerPower");
    
    public MawAngerPower(AbstractCreature owner, int blocks) {
        super(ID, "anger", PowerType.BUFF, owner);
        setValues(blocks);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, AbstractCard.CardType.SKILL) && amount > 0) {
            flash();
            addToTop(new GainBlockAction(owner, owner, amount));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new MawAngerPower(owner, amount);
    }
}