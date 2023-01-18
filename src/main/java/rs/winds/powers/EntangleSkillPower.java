package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class EntangleSkillPower extends AbstractSEPower {
    public static final String ID = King.MakeID("EntangleSkillPower");
    
    public EntangleSkillPower(AbstractCreature owner, int turns) {
        super(ID, "entangle", PowerType.DEBUFF, owner);
        setValues(turns);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        addToBot(new ReducePowerAction(owner, owner, this, 1));
    }
    
    @Override
    public boolean canPlayCard(AbstractCard card) {
        if (owner.isPlayer && isCardTypeOf(card, AbstractCard.CardType.SKILL))
            return false;
        return super.canPlayCard(card);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new EntangleSkillPower(owner, amount);
    }
}