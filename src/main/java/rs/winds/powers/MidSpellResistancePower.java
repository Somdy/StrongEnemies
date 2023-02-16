package rs.winds.powers;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

public class MidSpellResistancePower extends AbstractSEPower {
    public static final String ID = King.MakeID("MidSpellResistancePower");
    
    public MidSpellResistancePower(AbstractCreature owner) {
        super(ID, "resistance", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        if (SETool.SoCalledSpellDamage(info))
            damageAmount *= 0.5F;
        return super.onAttackedToChangeDamage(info, damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new MidSpellResistancePower(owner);
    }
}