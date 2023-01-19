package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TempHexPower extends AbstractSEPower {
    public static final String ID = King.MakeID("TempHexPower");
    
    public TempHexPower(AbstractCreature owner, int amount) {
        super(ID, "hex", PowerType.DEBUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type != AbstractCard.CardType.ATTACK) {
            flash();
            addToBot(new MakeTempCardInDrawPileAction(new Dazed(), amount, true, true));
        }
    }
    
    @Override
    public void atEndOfRound() {
        if (amount > 0) {
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}