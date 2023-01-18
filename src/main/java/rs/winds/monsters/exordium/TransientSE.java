package rs.winds.monsters.exordium;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Transient;
import com.megacrit.cardcrawl.powers.MetallicizePower;
import com.megacrit.cardcrawl.powers.ShiftingPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

public class TransientSE extends AbstractMonster {
    public static final String ID = King.MakeID(Transient.ID);
    private static final MonsterStrings strings = King.MonsterStrings(Transient.ID);
    private int turnCount;
    
    public TransientSE() {
        super("小倏忽魔", ID, 99, 0F, -15F, 370F, 340F, null, 0F, 20F);
        loadAnimation("images/monsters/theForest/transient/skeleton.atlas", "images/monsters/theForest/transient/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        turnCount = 0;
        damage.add(new DamageInfo(this, 25));
    }
    
    @Override
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new ShiftingPower(this)));
        addToBot(new ApplyPowerAction(this, this, new MetallicizePower(this, 9)));
        addToBot(new GainBlockAction(this, 9));
    }
    
    @Override
    public void takeTurn() {
        if (nextMove == 0) {
            addToBot(new ChangeStateAction(this, "ATTACK"));
            addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
            turnCount++;
            damage.set(0, new DamageInfo(this, 25 + 10 * turnCount));
            setMove((byte) 0, Intent.ATTACK, damage.get(0).base);
        }
    }
    
    @Override
    protected void getMove(int i) {
        setMove((byte) 0, Intent.ATTACK, damage.get(0).base);
    }
    
    @Override
    public void changeState(String stateName) {
        switch (stateName) {
            case "ATTACK": 
                state.setAnimation(0, "Attack", false);
                state.addAnimation(0, "Idle", true, 0F);
                break;
        }
    }
    
    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
            state.setAnimation(0, "Hurt", false);
            state.addAnimation(0, "Idle", true, 0F);
        }
    }
}