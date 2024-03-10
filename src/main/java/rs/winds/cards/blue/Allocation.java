package rs.winds.cards.blue;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class Allocation extends AbstractSECard {
    public static final String ID = King.MakeID("Allocation");
    
    public Allocation() {
        super(ID, "beta", 2, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        magicNumber = baseMagicNumber = 1;
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new ApplyPowerAction(s, s, new FocusPower(s, magicNumber)));
        addToBot(new ApplyPowerAction(s, s, new StrengthPower(s, -magicNumber)));
        addToBot(new ApplyPowerAction(s, s, new DexterityPower(s, -magicNumber)));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }
}
