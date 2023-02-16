package rs.winds.cards.watcher;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.powers.watcher.BlockReturnPower;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class WatcherOne extends AbstractSECard {
    public static final String ID = King.MakeID("WatcherOne");
    
    public WatcherOne() {
        super(ID, "watcher_one", 1, CardType.ATTACK, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.ENEMY);
        setMagicValue(1, true);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new ApplyPowerAction(t, s, new WeakPower(t, magicNumber, !s.isPlayer)));
        addToBot(new ApplyPowerAction(t, s, new VulnerablePower(t, magicNumber, !s.isPlayer)));
        if (upgraded) {
            addToBot(new ApplyPowerAction(t, s, new BlockReturnPower(t, 3)));
        }
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeTexts();
        }
    }
}
