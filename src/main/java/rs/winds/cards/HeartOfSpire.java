package rs.winds.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

public class HeartOfSpire extends CustomCard {
    public static final String ID = King.MakeID("HeartOfSpire");
    public static final CardStrings strings = CardCrawlGame.languagePack.getCardStrings(ID);
    
    public HeartOfSpire() {
        super(ID, strings.NAME, "SEAssets/images/cards/heartofspire.png", 1, strings.DESCRIPTION, CardType.STATUS, 
                CardColor.COLORLESS, CardRarity.COMMON, CardTarget.SELF);
        exhaust = true;
    }
    
    @Override
    public void upgrade() {}
    
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}
    
    @Override
    public void triggerOnExhaust() {
        addToTop(new DamageAction(LMSK.Player(), new DamageInfo(LMSK.Player(), 10, DamageInfo.DamageType.THORNS), 
                AbstractGameAction.AttackEffect.FIRE));
    }
}