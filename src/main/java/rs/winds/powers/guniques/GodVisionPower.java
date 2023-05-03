package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.listeners.ApplyPowerListener;
import rs.lazymankits.listeners.tools.PowerMplr;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class GodVisionPower extends AbstractSEPower {
    public static final String ID = King.MakeID("GodVisionPower");
    private int pID = -1;
    
    public GodVisionPower(AbstractCreature owner) {
        super(ID, "godvision", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    public GodVisionPower apply() {
        pID = ApplyPowerListener.AddNewManipulator(ID.length(), 0, e -> owner.hasPower(ID), (p, s, t) -> {
            if (s instanceof AbstractPlayer) {
                int buffTypes = s.powers.stream().filter(po -> isPowerTypeOf(po, PowerType.BUFF)).mapToInt(po -> 1).sum();
                GodVisionPower.this.amount = buffTypes;
                if (GodVisionPower.this.amount < 0) GodVisionPower.this.amount = 0;
                if (GodVisionPower.this.amount >= 5) p = null;
            }
            return p;
        });
        return this;
    }
    
    @Override
    public void onInitialApplication() {
        apply();
    }
    
    @Override
    public void onRemove() {
        ApplyPowerListener.RemoveManipulator(pID);
    }
    
    public void updateOnPowersModified() {
        amount = LMSK.Player().powers.stream().filter(p -> isPowerTypeOf(p, PowerType.BUFF)).mapToInt(p -> 1).sum();
        if (amount < 0) amount = 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}