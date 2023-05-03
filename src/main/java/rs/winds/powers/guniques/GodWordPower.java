package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.interfaces.powers.CardPlayablePower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

public class GodWordPower extends AbstractSEPower implements CardPlayablePower {
    public static final String ID = King.MakeID("GodWordPower");
    
    public GodWordPower(AbstractCreature owner) {
        super(ID, "godword", PowerType.BUFF, owner);
        setValues(0);
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, AbstractCard.CardType.SKILL)) {
            amount++;
        }
    }
    
    @Override
    public void atEndOfRound() {
        addToTop(new RemoveSpecificPowerAction(owner, owner, this));
        addToTop(new ApplyPowerAction(owner, owner, new CardLimitationPower(owner)));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
    
    @Override
    public boolean canUseCard(AbstractCard card, AbstractPlayer p, AbstractMonster m) {
        return isCardTypeOf(card, AbstractCard.CardType.SKILL) && amount < 5;
    }
}