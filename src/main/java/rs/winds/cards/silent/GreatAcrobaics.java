package rs.winds.cards.silent;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

import java.util.HashMap;
import java.util.Map;

public class GreatAcrobaics extends AbstractSECard {
    public static final String ID = King.MakeID("GreatAcrobatics");
    private final Map<String, AbstractCard> map = new HashMap<>();
    
    public GreatAcrobaics() {
        super(ID, "acrobatics", 1, CardType.SKILL, CardColor.GREEN, CardRarity.RARE, CardTarget.NONE);
        setMagicValue(3, true);
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new DrawCardAction(s, magicNumber));
//        addToBot(new DamageAction(p, new DamageInfo(p, 4, DamageInfo.DamageType.THORNS)));
        addToBot(new SimpleHandCardSelectBuilder(ca -> true)
                .setCanPickZero(false).setAnyNumber(false)
                .setAmount(1).setMsg(DiscardAction.TEXT[0]).setManipulator(new HandCardManipulator() {
                    @Override
                    public boolean manipulate(AbstractCard card, int i) {
                        map.put("discarded", card);
                        cpr().hand.moveToDiscardPile(card);
                        card.triggerOnManualDiscard();
                        GameActionManager.incrementDiscard(false);
                        return false;
                    }
                }).setFollowUpAction(new QuickAction(() -> {
                    AbstractCard card = map.get("discarded");
                    map.clear();
                    if (card != null && cpr().discardPile.contains(card)) {
                        cpr().discardPile.removeCard(card);
                        addToTop(new NewQueueCardAction(card, true, true, true));
                    }
                })));
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }
}