package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.*;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

public class WatcherThreePower extends AbstractSEPower {
    public static final String ID = King.MakeID("WatcherThreePower");
    
    public WatcherThreePower(AbstractCreature owner, int str, int dex) {
        super(ID, "master_reality", PowerType.BUFF, owner);
        setValues(str, dex);
        preloadString(s -> {
            setAmtValue(0, this.amount);
            setAmtValue(1, this.extraAmt);
        });
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (!AbstractDungeon.actionManager.turnHasEnded && !isCardTypeOf(card, AbstractCard.CardType.POWER)) {
            AbstractPower p = isCardTypeOf(card, AbstractCard.CardType.ATTACK)
                    ? new StrengthPower(owner, amount) : new DexterityPower(owner, extraAmt);
            AbstractPower tp = isCardTypeOf(card, AbstractCard.CardType.ATTACK)
                    ? new LoseStrengthPower(owner, amount) : new LoseDexterityPower(owner, extraAmt);
            addToBot(new ApplyPowerAction(owner, owner, p));
            addToBot(new ApplyPowerAction(owner, owner, tp));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new WatcherThreePower(owner, amount, extraAmt);
    }
}
