package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import rs.lazymankits.listeners.ApplyPowerListener;
import rs.lazymankits.listeners.tools.PowerMplr;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class PoisonProofPower extends AbstractSEPower {
    public static final String ID = King.MakeID("PoisonProofPower");
    private int mplrID = -1;
    
    public PoisonProofPower(AbstractCreature owner, int turns) {
        super(ID, "poisonproof", PowerType.BUFF, owner);
        setValues(turns);
        updateDescription();
    }
    
    @Override
    public void atEndOfRound() {
        if (amount > 0) {
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        }
    }
    
    @Override
    public void onInitialApplication() {
        mplrID = ApplyPowerListener.AddNewManipulator(ID.length(), 0, m -> owner.hasPower(ID), (p, s, t) -> {
            if (p instanceof PoisonPower)
                p = null;
            return p;
        });
    }
    
    @Override
    public void onRemove() {
        if (mplrID != -1) {
            ApplyPowerListener.RemoveManipulator(mplrID);
        }
    }
    
    @Override
    public void update(int slot) {
        super.update(slot);
        if (owner.powers.stream().anyMatch(p -> p instanceof PoisonPower)) {
            owner.powers.stream().filter(p -> p instanceof PoisonPower)
                    .findAny()
                    .ifPresent(p -> owner.powers.remove(p));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new PoisonProofPower(owner, amount);
    }
}
