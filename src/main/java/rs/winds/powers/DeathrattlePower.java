package rs.winds.powers;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

import java.util.function.Consumer;

public class DeathrattlePower extends AbstractSEPower {
    public static final String PID = King.MakeID("DeathrattlePower");
    private final Consumer<AbstractMonster> onDeathTrigger;
    
    public DeathrattlePower(AbstractMonster owner, String description, Consumer<AbstractMonster> onDeath) {
        super(PID, "deathrattle", PowerType.BUFF, owner);
        setValues(-1);
        preloadString(s -> s[0] = description);
        updateDescription();
        ID = PID + owner.id;
        this.onDeathTrigger = onDeath;
    }
    
    @Override
    public void onDeath() {
        if (onDeathTrigger != null && owner instanceof AbstractMonster)
            onDeathTrigger.accept((AbstractMonster) owner);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}