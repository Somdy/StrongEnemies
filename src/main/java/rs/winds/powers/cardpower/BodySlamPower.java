package rs.winds.powers.cardpower;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class BodySlamPower extends AbstractSEPower {
    public static final String ID = King.MakeID("BodySlamPower");
    
    public BodySlamPower(AbstractCreature owner, int times) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(times);
        preloadString(s -> setAmtValue(0, amount));
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (owner.currentBlock > 0 && amount > 0) {
            int damage = owner.currentBlock / 2;
            for (int i = 0; i < amount; i++) {
                addToBot(new DamageAllEnemiesAction(owner, DamageInfo.createDamageMatrix(damage, true),
                        DamageInfo.DamageType.THORNS, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
            }
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}
