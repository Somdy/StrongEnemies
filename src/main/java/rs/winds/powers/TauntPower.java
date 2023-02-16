package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.interfaces.powers.CardTauntPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TauntPower extends AbstractSEPower implements CardTauntPower {
    public static final String ID = King.MakeID("TauntPower");
    
    public TauntPower(AbstractMonster owner, int turns) {
        super(ID, "taunt", PowerType.BUFF, owner);
        setValues(turns);
        updateDescription();
    }
    
    @Override
    public void atStartOfTurn() {
        addToBot(new ReducePowerAction(owner, owner, this, 1));
    }
    
    @Override
    public boolean canPlayerUseCardAtOthers(AbstractCard card, AbstractPlayer p, AbstractMonster m) {
        return m == null || mustTargetEnemy(card) && m.hasPower(ID);
    }
    
    private boolean mustTargetEnemy(AbstractCard card) {
        return isCardTargetOf(card, AbstractCard.CardTarget.ENEMY, AbstractCard.CardTarget.SELF_AND_ENEMY);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}