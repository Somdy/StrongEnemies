package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RegenerateMonsterPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class LifeCounterPower extends AbstractSEPower {
    public static final String ID = King.MakeID("LifeCounterPower");
    private final int baseCounter;
    private int loss;
    
    public LifeCounterPower(AbstractMonster owner, int counter) {
        super(ID, "heartDef", PowerType.BUFF, owner);
        setValues(0);
        baseCounter = counter;
        updateDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0) {
            amount += damageAmount;
            loss += damageAmount;
            int mod = loss % baseCounter;
            loss -= mod;
            int count = loss / baseCounter;
            loss = mod;
            if (count > 0) {
                addToBot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, 3 * count)));
                addToBot(new ApplyPowerAction(owner, owner, new RegenerateMonsterPower((AbstractMonster) owner, 3 * count)));
                addToBot(new GainBlockAction(owner, owner, 10 * count));
            }
        }
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public void atStartOfTurn() {
        amount = 0;
        loss = 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}