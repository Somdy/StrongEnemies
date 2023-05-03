package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class SpellWeakPower extends AbstractSEPower {
    public static final String ID = King.MakeID("SpellWeakPower");
    private boolean justApplied;
    
    public SpellWeakPower(AbstractCreature owner, int turns, boolean isSourceMstr) {
        super(ID, "spellweak", PowerType.BUFF, owner);
        setValues(turns);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
        justApplied = isSourceMstr;
    }
    
    public void atEndOfRound() {
        if (justApplied) {
            justApplied = false;
        } else {
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        }
    }
    
    @Override
    public int onAttackToChangeDamage(DamageInfo info, int damageAmount) {
        if (info.type != DamageInfo.DamageType.NORMAL)
            damageAmount *= 0.75F;
        return super.onAttackToChangeDamage(info, damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new SpellWeakPower(owner, amount, justApplied);
    }
}