package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class SpecialFlightPower extends AbstractSEPower {
    public static final String ID = King.MakeID("SpecialFlightPower");
    private final int maxAmt;
    
    public SpecialFlightPower(AbstractCreature owner, int maxAmt) {
        super(ID, "flight", PowerType.BUFF, owner);
        setValues(maxAmt);
        this.maxAmt = maxAmt;
        updateDescription();
    }
    
    public void playApplyPowerSfx() {
        CardCrawlGame.sound.play("POWER_FLIGHT", 0.05F);
    }
    
    public void atStartOfTurn() {
        amount = maxAmt;
        updateDescription();
    }
    
    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        damageAmount /= 2F;
        return super.onAttackedToChangeDamage(info, damageAmount);
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        flash();
        addToBot(new ReducePowerAction(owner, owner, this, 1));
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public void onRemove() {
        addToBot(new ApplyPowerAction(owner, owner, new WeakPower(owner, 2, true)));
        addToBot(new ApplyPowerAction(owner, owner, new VulnerablePower(owner, 2, true)));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}