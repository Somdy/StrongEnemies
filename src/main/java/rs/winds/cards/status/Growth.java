package rs.winds.cards.status;

import basemod.abstracts.CustomCard;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.rootdepths.TheHolyTree;

public class Growth extends CustomCard {
    public static final String ID = King.MakeID("Growth");
    public static final CardStrings strings = CardCrawlGame.languagePack.getCardStrings(ID);
    
    public Growth() {
        super(ID, strings.NAME, "SEAssets/images/cards/growth.png", -2, strings.DESCRIPTION, CardType.STATUS, 
                CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.SELF);
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            rawDescription = strings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
    
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        if (dontTriggerOnUseCard) {
            for (AbstractMonster tree : LMSK.GetAllExptMstr(mo -> mo instanceof TheHolyTree)) {
                addToBot(new HealAction(tree, tree, MathUtils.floor(tree.maxHealth * (upgraded ? 0.02F : 0.01F))));
            }
        }
    }
    
    public void triggerOnEndOfTurnForPlayingCard() {
        dontTriggerOnUseCard = true;
        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(this, true));
    }
    
    @Override
    public void triggerOnExhaust() {
        for (AbstractMonster tree : LMSK.GetAllExptMstr(mo -> mo instanceof TheHolyTree)) {
            addToBot(new HealAction(tree, tree, MathUtils.floor(tree.maxHealth * (upgraded ? 0.02F : 0.01F))));
        }
    }
}