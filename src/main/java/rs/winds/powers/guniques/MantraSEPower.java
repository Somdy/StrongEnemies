package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.ending.EvilGod;

public class MantraSEPower extends AbstractSEPower {
    public static final String ID = King.MakeID("MantraPower");
    private boolean initialApply;
    
    public MantraSEPower(EvilGod owner, int amount, boolean initialApply) {
        super(ID, "mantra", PowerType.BUFF, owner);
        setValues(amount);
        updateDescription();
        this.initialApply = initialApply;
    }
    
    @Override
    public void playApplyPowerSfx() {
        playSound("POWER_MANTRA");
    }
    
    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        if (amount >= 10 && owner instanceof EvilGod) {
            ((EvilGod) owner).changeStance(new EvilGod.SEDivinityStance(owner));
            amount -= 10;
        }
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (owner instanceof EvilGod) {
            if (initialApply) {
                initialApply = false;
                return;
            }
            addToBot(new ApplyPowerAction(owner, owner, new MantraSEPower((EvilGod) owner, 4, false)));
        }
    }
    
    @Override
    public void update(int slot) {
        super.update(slot);
        if (amount >= 10 && owner instanceof EvilGod) {
            ((EvilGod) owner).changeStance(new EvilGod.SEDivinityStance(owner));
            amount -= 10;
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}
