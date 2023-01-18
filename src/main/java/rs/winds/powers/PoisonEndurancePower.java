package rs.winds.powers;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.PoisonLoseHpAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class PoisonEndurancePower extends AbstractSEPower {
    public static final String ID = King.MakeID("PoisonEndurancePower");
    
    public PoisonEndurancePower(AbstractCreature owner) {
        super(ID, "poison", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new PoisonEndurancePower(owner);
    }
    
    @SpirePatch2(clz = PoisonLoseHpAction.class, method = SpirePatch.CONSTRUCTOR)
    public static class PoisonLoseHpActionPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractCreature target, @ByRef int[] amount) {
            AbstractPower p = target.getPower(ID);
            if (p != null) {
                p.flashWithoutSound();
                amount[0] /= 2;
            }
        }
    }
}