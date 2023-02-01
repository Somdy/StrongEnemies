package rs.winds.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.unique.IncreaseMaxHpAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Byrd;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.city.ByrdSE;

public class FrustratedPower extends AbstractSEPower {
    public static final String ID = King.MakeID("FrustratedPower");
    private boolean triggered;
    
    public FrustratedPower(ByrdSE owner) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
        triggered = false;
    }
    
    @Override
    public void onMonsterDeath(AbstractMonster target) {
        if (target instanceof Byrd) {
            addToBot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, 1)));
            addToBot(new QuickAction(() -> owner.increaseMaxHp(25, true)));
        }
    }
    
    @Override
    public void update(int slot) {
        super.update(slot);
//        if (!triggered && alone() && owner instanceof ByrdSE) {
//            triggered = true;
//            ((ByrdSE) owner).getKnockedDown();
//            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
//        }
    }
    
    private boolean alone() {
        return LMSK.GetAllExptMstr(m -> m != owner).size() <= 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}