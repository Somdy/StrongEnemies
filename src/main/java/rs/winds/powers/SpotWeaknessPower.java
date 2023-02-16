package rs.winds.powers;

import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.citydepths.BossKing;

public class SpotWeaknessPower extends AbstractSEPower {
    public static final String ID = King.MakeID("SpotWeaknessPower");
    
    public SpotWeaknessPower(BossKing owner) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}