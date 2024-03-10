package rs.winds.powers.dups;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.actions.common.MonsterTakeTurnAction;
import rs.winds.core.King;

public class DonuCardCounterPower extends AbstractSEPower {
    public static final String ID = King.MakeID("DonuCardCounterPower");
    private final int counter;
    
    public DonuCardCounterPower(AbstractCreature owner, int counter) {
        super(ID, "draw", PowerType.BUFF, owner);
        setValues(0);
        this.counter = counter;
        preloadString(s -> setAmtValue(0, counter));
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card != null && isCardTypeOf(card, AbstractCard.CardType.POWER)) {
            amount++;
        }
        if (amount > 0 && amount % counter == 0 && owner instanceof AbstractMonster) {
            amount = 0;
            addToBot(new MonsterTakeTurnAction((AbstractMonster) owner));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}