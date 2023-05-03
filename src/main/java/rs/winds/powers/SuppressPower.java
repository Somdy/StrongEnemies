package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.WeakPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class SuppressPower extends AbstractSEPower {
    public static final String ID = King.MakeID("SuppressPower");
    
    public SuppressPower(AbstractCreature owner, int amount) {
        super(ID, "anger", PowerType.BUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (amount > 0 && isCardTypeOf(card, AbstractCard.CardType.ATTACK)) {
            addToBot(new ApplyPowerAction(LMSK.Player(), owner, new WeakPower(LMSK.Player(), amount, !owner.isPlayer)));
            addToBot(new ApplyPowerAction(LMSK.Player(), owner, new SpellWeakPower(LMSK.Player(), amount, !owner.isPlayer)));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new SuppressPower(owner, amount);
    }
}
