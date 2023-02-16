package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class WeakPlusPower extends AbstractSEPower {
    public static final String ID = King.MakeID("WeakPlusPower");
    private boolean justApplied = false;
    
    public WeakPlusPower(AbstractCreature owner, int amount, boolean isSourceMonster) {
        super(ID, "weak", PowerType.DEBUFF, owner);
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
    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        damage *= 0.4F;
        return super.atDamageGive(damage, type);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}