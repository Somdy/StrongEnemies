package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class ExtraIntentsPower extends AbstractSEPower {
    public static final String ID = King.MakeID("ExtraIntentsPower");
    
    public ExtraIntentsPower(AbstractCreature owner) {
        super(ID, "godtouch", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}