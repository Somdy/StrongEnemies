package rs.winds.powers;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

public class HighSpellResistancePower extends AbstractSEPower {
    public static final String ID = King.MakeID("HighSpellResistancePower");
    
    public HighSpellResistancePower(AbstractCreature owner) {
        super(ID, "resistance", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        if (SETool.SoCalledSpellDamage(info))
            damageAmount *= 0.2F;
        return super.onAttackedToChangeDamage(info, damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new HighSpellResistancePower(owner);
    }
}
