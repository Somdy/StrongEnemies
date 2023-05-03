package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.interfaces.powers.CardPlayablePower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class CardLimitationPower extends AbstractSEPower implements CardPlayablePower {
    public static final String ID = King.MakeID("GodTouchPower");
    private static final PowerStrings strings = King.PowerStrings(GodWordPower.ID);
    private AbstractCard.CardType targetType;
    
    public CardLimitationPower(AbstractCreature owner) {
        super(ID, "godtouch", PowerType.BUFF, owner);
        setValues(0);
        updateDescription();
        targetType = AbstractCard.CardType.ATTACK;
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, targetType)) {
            amount++;
        }
    }
    
    @Override
    public void atEndOfRound() {
        amount = 0;
        if (targetType == AbstractCard.CardType.ATTACK) {
            targetType = AbstractCard.CardType.SKILL;
            preloadString(s -> s[0] = strings.DESCRIPTIONS[0]);
            name = strings.NAME;
        }
        else if (targetType == AbstractCard.CardType.SKILL) {
            targetType = AbstractCard.CardType.ATTACK;
            preloadString(s -> s[0] = DESCRIPTIONS[0]);
            name = NAME;
        }
        updateDescription();
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
    
    @Override
    public boolean canUseCard(AbstractCard card, AbstractPlayer p, AbstractMonster m) {
        return !isCardTypeOf(card, targetType) || amount < 5;
    }
}