package rs.winds.cards.watcher;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;
import rs.winds.powers.WatcherThreePower;

public class WatcherThree extends AbstractSECard {
    public static final String ID = King.MakeID("WatcherThree");
    
    public WatcherThree() {
        super(ID, "watcher_three", 3, CardType.POWER, CardColor.PURPLE, CardRarity.RARE, CardTarget.SELF);
        setMagicValue(1, true);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new ApplyPowerAction(s, s, new WatcherThreePower(s, magicNumber, magicNumber)));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeMagicNumber(1);
            upgradeTexts();
        }
    }
}