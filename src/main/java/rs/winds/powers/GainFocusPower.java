package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class GainFocusPower extends AbstractSEPower {
    public static final String ID = King.MakeID("GainFocusPower");
    
    public GainFocusPower(AbstractCreature owner, int amount) {
        super(ID, "shackle", PowerType.DEBUFF, owner);
        setValues(amount);
        updateDescription();
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        flash();
        addToBot(new ApplyPowerAction(owner, owner, new FocusPower(owner, amount)));
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }
    
//    @Override
//    public void atEndOfRound() {
//        flash();
//        addToBot(new ApplyPowerAction(owner, owner, new FocusPower(owner, amount)));
//        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
//    }
    
    @Override
    public AbstractPower makeCopy() {
        return new GainFocusPower(owner, amount);
    }
}