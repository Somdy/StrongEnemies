package rs.winds.powers.guniques;

import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.curses.CurseOfTheBell;
import com.megacrit.cardcrawl.cards.curses.Necronomicurse;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.cards.curse.CurseOfGod;
import rs.winds.core.King;

public class GodCursePower extends AbstractSEPower {
    public static final String ID = King.MakeID("CursePower");
    
    public GodCursePower(AbstractCreature owner) {
        super(ID, "corruption", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        trigger();
    }
    
    private void trigger() {
        addToBot(new MakeTempCardInDrawPileAction(new Necronomicurse(), 1, true, true));
        addToBot(new MakeTempCardInDrawPileAction(new CurseOfTheBell(), 1, true, true));
        addToBot(new MakeTempCardInDrawPileAction(new CurseOfGod(), 1, true, true));
        addToBot(new QuickAction(() -> {
            for (int i = 0; i < 3; i++) {
                int size = LMSK.Player().relics.size();
                if (size == 0) break;
                AbstractRelic r = LMSK.Player().relics.get(size - 1);
                LMSK.Player().loseRelic(r.relicId);
            }
        }));
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}
