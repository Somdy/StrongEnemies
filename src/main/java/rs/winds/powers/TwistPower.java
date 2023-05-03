package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.lazymankits.listeners.ApplyPowerListener;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TwistPower extends AbstractSEPower {
    public static final String ID = King.MakeID("TwistPower");
    
    public TwistPower(AbstractCreature owner, int amount) {
        super(ID, "twist", PowerType.BUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (amount > 0 && isCardTypeOf(card, AbstractCard.CardType.SKILL)) {
            for (AbstractMonster m : LMSK.GetAllExptMstr(m -> true)) {
                addToBot(new GainBlockAction(m, owner, amount));
            }
            addToBot(new ApplyPowerToEnemiesAction(owner, PlatedArmorPower.class, ApplyPowerParam.ANY_OWNER, amount));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new TwistPower(owner, amount);
    }
}
