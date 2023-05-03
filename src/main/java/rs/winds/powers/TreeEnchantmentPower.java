package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.cards.colorless.SpecialTreeAttack;
import rs.winds.core.King;

public class TreeEnchantmentPower extends AbstractSEPower {
    public static final String ID = King.MakeID("EnchantmentPower");
    
    public TreeEnchantmentPower(AbstractCreature owner) {
        super(ID, "corruption", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
        stackable = false;
    }
    
    public void onSpecificTrigger(int amount) {
        if (amount > 0) {
            addToBot(new QuickAction(() -> {
                if (LMSK.Player().powers.stream().anyMatch(p -> isPowerTypeOf(p, PowerType.BUFF))) {
                    LMSK.Player().powers.stream().filter(p -> isPowerTypeOf(p, PowerType.BUFF) && p.amount > 0)
                            .forEach(p -> addToTop(new ReducePowerAction(LMSK.Player(), owner, p, amount)));
                }
            }));
            addToBot(new MakeTempCardInDrawPileAction(new SpecialTreeAttack(), 1, false, false));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new TreeEnchantmentPower(owner);
    }
}