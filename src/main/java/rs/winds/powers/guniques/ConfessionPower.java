package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Regret;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

import java.util.Optional;

public class ConfessionPower extends AbstractSEPower {
    public static final String ID = King.MakeID("ConfessionPower");
    
    public ConfessionPower(AbstractCreature owner, int initial) {
        super(ID, "epoison", PowerType.BUFF, owner);
        setValues(initial, initial);
        updateDescription();
    }
    
    @Override
    public void onSpecificTrigger() {
        flash();
        addToTop(new MakeTempCardInDrawPileAction(new Regret(), 1, true, true));
        addToTop(new QuickAction(() -> {
            if (cpr().hand.group.stream().anyMatch(c -> !(c instanceof Regret))) {
                getExptRandomCard(cardRandomRng(), c -> !(c instanceof Regret), cpr().hand.group)
                        .map(c -> cpr().hand.group.indexOf(c))
                        .ifPresent(i -> {
                            cpr().hand.group.set(i, new Regret());
                            cpr().hand.refreshHandLayout();
                            cpr().hand.glowCheck();
                        });
            }
        }));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}