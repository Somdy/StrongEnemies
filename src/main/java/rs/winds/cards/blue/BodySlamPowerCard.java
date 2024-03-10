package rs.winds.cards.blue;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;
import rs.winds.powers.cardpower.BodySlamPower;

public class BodySlamPowerCard extends AbstractSECard {
    public static final String ID = King.MakeID("BSPC");
    
    public BodySlamPowerCard() {
        super(ID, "beta", 2, CardType.POWER, CardColor.BLUE, CardRarity.RARE, CardTarget.SELF);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new ApplyPowerAction(s, s, new BodySlamPower(s, 1)));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }
}