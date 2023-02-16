package rs.winds.powers;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.BufferPower;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class PaladinCardCounterPower extends AbstractSEPower {
    public static final String ID = King.MakeID("PaladinCardCounterPower");
    private final int counter;
    private final int buffers;
    
    public PaladinCardCounterPower(AbstractCreature owner, int counter, int buffers) {
        super(ID, "draw", PowerType.BUFF, owner);
        setValues(0);
        this.counter = counter;
        this.buffers = buffers;
        preloadString(s -> {
            setAmtValue(0, this.counter);
            setAmtValue(1, this.buffers);
        });
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card != null) {
            amount++;
        }
        if (amount % counter == 0) {
            amount = 0;
            addToBot(new ApplyPowerToEnemiesAction(owner, BufferPower.class, ApplyPowerParam.ANY_OWNER, buffers));
        }
    }
    
    @Override
    public void atStartOfTurn() {
        amount = 0;
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}