package rs.winds.powers;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TreeGrowthPower extends AbstractSEPower {
    public static final String ID = King.MakeID("GrowthPower");
    private int baseAmt;
    
    public TreeGrowthPower(AbstractCreature owner, int amount) {
        super(ID, "regen", PowerType.BUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
        baseAmt = amount;
    }
    
    public void modify(int newBaseAmt) {
        baseAmt = newBaseAmt;
        amount = baseAmt;
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (amount > 0) {
            amount--;
        }
        if (amount <= 0) {
            amount = baseAmt;
            addToBot(new QuickAction(() -> owner.increaseMaxHp(100, true)));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new TreeGrowthPower(owner, amount);
    }
}