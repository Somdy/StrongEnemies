package rs.winds.powers.guniques;

import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.listeners.ApplyPowerListener;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

import java.util.function.Predicate;

public class PowerLimitationPower extends AbstractSEPower {
    public static final String ID = King.MakeID("GodVisionPower");
    private int pID = -1;
    
    public PowerLimitationPower(AbstractCreature owner, int limitation) {
        super(ID, "godvision", PowerType.BUFF, owner);
        setValues(-1, limitation);
        preloadString(s -> setAmtValue(0, extraAmt));
        updateDescription();
    }
    
    public PowerLimitationPower(AbstractCreature owner) {
        this(owner, 5);
    }
    
    public void updateLimitation(int newLimit) {
        setValues(amount, newLimit);
        updateDescription();
    }
    
    public PowerLimitationPower apply() {
        Predicate<AbstractPower> invisible = p -> Loader.isModLoadedOrSideloaded("stslib") && p instanceof InvisiblePower;
        ApplyPowerListener.RemoveManipulator(pID);
        pID = ApplyPowerListener.AddNewManipulator(ID.length(), 0, e -> owner.hasPower(ID), (p, t, s) -> {
            if (t instanceof AbstractPlayer && isPowerTypeOf(p, PowerType.BUFF)) {
                int buffTypes = t.powers.stream()
                        .filter(po -> isPowerTypeOf(po, PowerType.BUFF) && !invisible.test(po))
                        .mapToInt(po -> 1)
                        .sum();
                PowerLimitationPower.this.amount = buffTypes;
                King.Log("Max buff allowed: " + PowerLimitationPower.this.extraAmt);
                if (PowerLimitationPower.this.amount < 0) PowerLimitationPower.this.amount = 0;
                if (PowerLimitationPower.this.amount >= PowerLimitationPower.this.extraAmt) {
                    p = t.hasPower(p.ID) ? p : null;
                }
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
    public void onVictory() {
        ApplyPowerListener.RemoveManipulator(pID);
    }
    
    @Override
    public void onRemove() {
        ApplyPowerListener.RemoveManipulator(pID);
    }
    
    public void updateOnPowersModified() {
        Predicate<AbstractPower> invisible = p -> Loader.isModLoadedOrSideloaded("stslib") && p instanceof InvisiblePower;
        amount = LMSK.Player().powers.stream().filter(p -> isPowerTypeOf(p, PowerType.BUFF) && !invisible.test(p)).mapToInt(p -> 1).sum();
        if (amount < 0) amount = 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}