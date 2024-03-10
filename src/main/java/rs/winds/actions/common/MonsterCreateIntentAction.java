package rs.winds.actions.common;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import rs.lazymankits.abstracts.LMCustomGameAction;

public class MonsterCreateIntentAction extends LMCustomGameAction {
    private AbstractMonster m;
    
    public MonsterCreateIntentAction(AbstractMonster m) {
        this.m = m;
        this.target = m;
    }
    
    @Override
    public void update() {
        isDone = true;
        m.createIntent();
    }
}