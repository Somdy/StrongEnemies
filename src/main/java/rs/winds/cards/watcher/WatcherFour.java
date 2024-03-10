package rs.winds.cards.watcher;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.watcher.PressEndTurnButtonAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;
import rs.winds.powers.WatcherThreePower;

import java.util.Optional;

public class WatcherFour extends AbstractSECard {
    public static final String ID = King.MakeID("WatcherFour");
    
    public WatcherFour() {
        super(ID, "watcher_four", 2, CardType.SKILL, CardColor.PURPLE, CardRarity.RARE, CardTarget.SELF);
        setDamageValue(10, true);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new DamageAction(s, new DamageInfo(s, 10, DamageInfo.DamageType.THORNS)));
        addToBot(new QuickAction(() -> {
            Optional<AbstractPower> opt = getExptPower(s.powers, p -> StrengthPower.POWER_ID.equals(p.ID));
            opt.ifPresent(p -> addToTop(new ApplyPowerAction(s, s, new StrengthPower(s, p.amount))));
            opt = getExptPower(s.powers, p -> DexterityPower.POWER_ID.equals(p.ID));
            opt.ifPresent(p -> addToTop(new ApplyPowerAction(s, s, new DexterityPower(s, p.amount))));
        }));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeBaseCost(1);
            upgradeTexts();
        }
    }
}
