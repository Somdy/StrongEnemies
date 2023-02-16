package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class VulnerablePlusPower extends AbstractSEPower {
    public static final String ID = King.MakeID("VulnerablePlusPower");
    private boolean justApplied = false;
    
    public VulnerablePlusPower(AbstractCreature owner, int amount, boolean isSourceMonster) {
        super(ID, "vulnerable", PowerType.DEBUFF, owner);
        setValues(amount);
        updateDescription();
        if (AbstractDungeon.actionManager.turnHasEnded && isSourceMonster) {
            justApplied = true;
        }
    }
    
    @Override
    public void atEndOfRound() {
        if (justApplied) {
            justApplied = false;
        } else {
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        }
    }
    
    @Override
    public float atDamageReceive(float damage, DamageInfo.DamageType damageType) {
        damage *= 1.8F;
        return super.atDamageReceive(damage, damageType);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}