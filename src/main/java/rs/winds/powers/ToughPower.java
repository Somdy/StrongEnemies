package rs.winds.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class ToughPower extends AbstractSEPower {
    public static final String ID = King.MakeID("ToughPower");
    
    public ToughPower(AbstractCreature owner, int startingBlock, int baseBlock) {
        super(ID, "malleable", PowerType.BUFF, owner);
        setValues(startingBlock, baseBlock);
        preloadString(s -> {
            setAmtValue(0, amount);
            setAmtValue(1, extraAmt);
        });
        updateDescription();
        isExtraAmtFixed = false;
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0 && owner != null) {
            effectToList(new FlashAtkImgEffect(owner.hb.cX, owner.hb.cY, AbstractGameAction.AttackEffect.SHIELD));
            owner.addBlock(amount);
            amount++;
            updateDescription();
        }
        return super.onAttacked(info, damageAmount);
    }
    
    public void atEndOfTurn(boolean isPlayer) {
        if (!owner.isPlayer) {
            amount = extraAmt;
            updateDescription();
        }
    }
    
    public void atEndOfRound() {
        if (owner.isPlayer) {
            amount = extraAmt;
            updateDescription();
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new ToughPower(owner, amount, extraAmt);
    }
}
