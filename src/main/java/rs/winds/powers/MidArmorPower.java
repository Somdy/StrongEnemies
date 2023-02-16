package rs.winds.powers;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

public class MidArmorPower extends AbstractSEPower {
    public static final String ID = King.MakeID("MidArmorPower");
    
    public MidArmorPower(AbstractCreature owner) {
        super(ID, "lightarmor", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
        if (SETool.SuchCalledPhysicalDamage(type)) {
            damage *= 0.5F;
        }
        return super.atDamageFinalReceive(damage, type);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new MidArmorPower(owner);
    }
}