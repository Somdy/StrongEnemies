package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class TempConfusionPower extends AbstractSEPower {
    public static final String ID = King.MakeID("TempConfusionPower");
    
    public TempConfusionPower(AbstractCreature owner, int turns) {
        super(ID, "confusion", PowerType.DEBUFF, owner);
        setValues(turns);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    public void playApplyPowerSfx() {
        CardCrawlGame.sound.play("POWER_CONFUSION", 0.05F);
    }
    
    public void onCardDraw(AbstractCard card) {
        if (card.cost >= 0) {
            int newCost = AbstractDungeon.cardRandomRng.random(3);
            if (card.cost != newCost) {
                card.cost = newCost;
                card.costForTurn = card.cost;
                card.isCostModified = true;
            }
            card.freeToPlayOnce = false;
        }
    }
    
    @Override
    public void atEndOfRound() {
        if (amount > 0) {
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}