package rs.winds.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class SlaverElitePower extends AbstractSEPower {
    public static final String ID = King.MakeID("SlaverElitePower");
    
    public SlaverElitePower(AbstractCreature owner, int damage) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(damage);
        preloadString(s -> setAmtValue(0, amount));
        updateDescription();
    }
    
    @Override
    public void onPlayerExhaustCard(AbstractCard card) {
        if (amount > 0) {
            flash();
            addToBot(new NullableSrcDamageAction(LMSK.Player(), crtDmgInfo(null, amount, DamageInfo.DamageType.THORNS),
                    AbstractGameAction.AttackEffect.NONE));
            addToBot(new MakeTempCardInDiscardAction(card.makeStatEquivalentCopy(), 1));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new SlaverElitePower(owner, amount);
    }
}