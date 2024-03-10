package rs.winds.powers.dups;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeOrb;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.actions.common.MonsterTakeTurnAction;
import rs.winds.core.King;

public class BronzeLifeCounterPower extends AbstractSEPower {
    public static final String ID = King.MakeID("BronzeLifeCounterPower");
    private final int baseCounter;
    private int loss;
    
    public BronzeLifeCounterPower(AbstractMonster owner, int counter) {
        super(ID, "heartDef", PowerType.BUFF, owner);
        setValues(0);
        baseCounter = counter;
        updateDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0) {
            amount += damageAmount;
            loss += damageAmount;
            int mod = loss % baseCounter;
            loss -= mod;
            int count = loss / baseCounter;
            loss = mod;
            if (count > 0 && !owner.isDeadOrEscaped() && owner instanceof AbstractMonster) {
                amount = loss;
                addToBot(new MonsterTakeTurnAction((AbstractMonster) owner));
            }
        }
        return super.onAttacked(info, damageAmount);
    }
    
    public void onBronzeDie(BronzeOrb orb) {
        if (!owner.isDeadOrEscaped() && owner instanceof AbstractMonster) {
            King.Log("take turn on orb dies");
            addToBot(new MonsterTakeTurnAction((AbstractMonster) owner));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}