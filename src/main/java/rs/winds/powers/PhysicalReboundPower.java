package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

public class PhysicalReboundPower extends AbstractSEPower {
    public static final String ID = King.MakeID("PhysicalReboundPower");
    
    public PhysicalReboundPower(AbstractCreature owner) {
        super(ID, "confusion", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (SETool.SoCalledPhysicalDamage(info)) {
            flash();
            addToTop(new ApplyPowerAction(LMSK.Player(), owner, new TempConfusionPower(LMSK.Player(), 1)));
        }
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}