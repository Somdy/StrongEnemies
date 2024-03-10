package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class LoseEnergyPower extends AbstractSEPower {
    public static final String ID = King.MakeID("LoseEnergyPower");
    
    public LoseEnergyPower(AbstractPlayer owner, int amt) {
        super(ID, "powerstealer", PowerType.DEBUFF, owner);
        setValues(amt);
        updateDescription();
    }
    
    @Override
    public void onEnergyRecharge() {
        LMSK.Player().loseEnergy(amount);
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}