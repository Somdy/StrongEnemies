package rs.winds.actions.common;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import rs.lazymankits.abstracts.LMCustomGameAction;

public class MonsterTakeTurnAction extends LMCustomGameAction {
    private AbstractMonster m;
    
    public MonsterTakeTurnAction(AbstractMonster m) {
        this.m = m;
        this.target = m;
    }
    
    @Override
    public void update() {
        isDone = true;
        m.takeTurn();
        addToBot(new MonsterCreateIntentAction(m));
    }
}