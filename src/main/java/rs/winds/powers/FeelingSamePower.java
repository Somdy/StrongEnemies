package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class FeelingSamePower extends AbstractSEPower {
    public static final String ID = King.MakeID("FeelingSamePower");
    
    public FeelingSamePower(AbstractCreature owner, int turns) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(turns);
        updateDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0 && info.owner != owner) {
            flash();
            addToBot(new DamageAction(info.owner, new DamageInfo(owner, damageAmount, info.type)));
        }
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        addToBot(new ReducePowerAction(owner, owner, this, 1));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new FeelingSamePower(owner, amount);
    }
}