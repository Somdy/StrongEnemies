package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class SlightImmunePower extends AbstractSEPower {
    public static final String ID = King.MakeID("SlightImmunePower");
    
    public SlightImmunePower(AbstractCreature owner, int amount) {
        super(ID, "immune", PowerType.BUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
        stackable = false;
    }
    
    @Override
    public void atStartOfTurn() {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        addToBot(new ApplyPowerAction(owner, owner, new HeavyImmunePower(owner, 100)));
    }
    
    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        if (damageAmount < amount)
            damageAmount = 0;
        return super.onAttackedToChangeDamage(info, damageAmount);
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount < amount)
            damageAmount = 0;
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public int onLoseHp(int damageAmount) {
        if (damageAmount < amount)
            damageAmount = 0;
        return super.onLoseHp(damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new SlightImmunePower(owner, amount);
    }
}
