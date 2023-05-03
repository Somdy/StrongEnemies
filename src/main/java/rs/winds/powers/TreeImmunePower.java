package rs.winds.powers;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TreeImmunePower extends AbstractSEPower {
    public static final String ID = King.MakeID("TreeImmunePower");
    
    public TreeImmunePower(AbstractCreature owner, int amount) {
        super(ID, "immune", PowerType.BUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
        stackable = false;
        priority = 0;
    }
    
    public void modify(int newAmt) {
        this.amount = newAmt;
        updateDescription();
    }
    
    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        if (damageAmount > amount)
            damageAmount = amount;
        return super.onAttackedToChangeDamage(info, damageAmount);
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > amount)
            damageAmount = amount;
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public int onLoseHp(int damageAmount) {
        if (damageAmount > amount)
            damageAmount = amount;
        return super.onLoseHp(damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new TreeImmunePower(owner, amount);
    }
}