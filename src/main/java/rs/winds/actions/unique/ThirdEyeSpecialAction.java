package rs.winds.actions.unique;

import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import rs.lazymankits.abstracts.LMCustomGameAction;
import rs.lazymankits.utils.LMSK;

public class ThirdEyeSpecialAction extends LMCustomGameAction {
    private String msg;
    
    public ThirdEyeSpecialAction(int amount, String msg) {
        this.amount = amount;
        this.msg = msg;
        actionType = ActionType.CARD_MANIPULATION;
        duration = startDuration = Settings.ACTION_DUR_FAST;
    }
    
    @Override
    public void update() {
        if (duration == startDuration) {
            if (amount <= 0 || (LMSK.Player().drawPile.isEmpty() && LMSK.Player().discardPile.isEmpty())) {
                isDone = true;
                return;
            }
            if (LMSK.Player().drawPile.size() < amount) {
                if (!LMSK.Player().discardPile.isEmpty()) {
                    int discards = LMSK.Player().discardPile.size();
                    int draws = LMSK.Player().drawPile.size();
                    int total = discards + draws;
                    if (total < amount)
                        amount = total;
                    addToTop(new ThirdEyeSpecialAction(amount, msg));
                    addToTop(new EmptyDeckShuffleAction());
                    isDone = true;
                    return;
                }
                amount = LMSK.Player().drawPile.size();
                if (amount <= 0) {
                    isDone = true;
                    return;
                }
            }
            CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (int i = LMSK.Player().drawPile.size() - 1, j = 0; j < amount; j++, i--) {
                tmp.addToTop(LMSK.Player().drawPile.group.get(i));
            }
            AbstractDungeon.gridSelectScreen.open(tmp, amount, true, msg);
            tickDuration();
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
                if (LMSK.Player().drawPile.contains(card))
                    LMSK.Player().drawPile.moveToExhaustPile(card);
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            isDone = true;
        }
        tickDuration();
    }
}