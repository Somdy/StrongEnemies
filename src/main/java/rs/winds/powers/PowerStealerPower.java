package rs.winds.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

import java.util.Optional;

public class PowerStealerPower extends AbstractSEPower {
    public static final String ID = King.MakeID("PowerStealerPower");
    
    public PowerStealerPower(AbstractMonster owner) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if (info.owner == this.owner && info.type == DamageInfo.DamageType.NORMAL && target != null) {
            Optional<AbstractPower> p = getExptRandomPower(target.powers, monsterAiRng(), po -> isPowerTypeOf(po, PowerType.BUFF)
                    && po instanceof CloneablePowerInterface && ((CloneablePowerInterface) po).makeCopy() != null);
            p.ifPresent(po -> {
                AbstractPower power = ((CloneablePowerInterface) po).makeCopy();
                power.owner = this.owner;
                power.amount = po.amount;
                addToTop(new ApplyPowerAction(owner, owner, power));
                addToTop(new RemoveSpecificPowerAction(target, owner, po));
            });
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}