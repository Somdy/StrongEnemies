package rs.winds.cards.colorless;

import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class Voidness extends AbstractSECard {
    public static final String ID = King.MakeID("Voidness");
    
    public Voidness() {
        super(ID, "voidness", 0, CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.SELF);
        isEthereal = true;
        exhaust = true;
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new RemoveAllPowersAction(s, false));
    }
    
    @Override
    public void upgrade() {
        
    }
}