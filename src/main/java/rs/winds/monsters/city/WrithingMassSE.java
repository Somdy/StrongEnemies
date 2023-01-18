package rs.winds.monsters.city;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.monsters.beyond.WrithingMass;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;
import com.megacrit.cardcrawl.powers.MalleablePower;
import com.megacrit.cardcrawl.powers.ReactivePower;
import rs.winds.core.King;

public class WrithingMassSE extends WrithingMass {
    public static final String ID = King.MakeID(WrithingMass.ID);
    
    public WrithingMassSE() {
        super();
        id = ID;
        name = "幼年面团";
        setHp(150);
    }
    
    @Override
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new ReactivePower(this)));
        addToBot(new ApplyPowerAction(this, this, new MalleablePower(this)));
        addToBot(new GainBlockAction(this, 40));
    }
    
    @Override
    public void takeTurn() {
        super.takeTurn();
    }
    
    @Override
    protected void getMove(int i) {
        super.getMove(i);
    }
}