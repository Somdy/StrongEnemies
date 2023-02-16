package rs.winds.monsters.citydepths;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Taskmaster;
import com.megacrit.cardcrawl.monsters.exordium.SlaverBlue;
import com.megacrit.cardcrawl.powers.EntanglePower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.listeners.TurnEventListener;
import rs.lazymankits.listeners.tools.TurnEvent;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.DeathrattlePower;
import rs.winds.powers.EntangleSkillPower;
import rs.winds.powers.VulnerablePlusPower;
import rs.winds.powers.WeakPlusPower;

public class SlaverBlueElite extends AbstractMonster {
    public static final String ID = King.MakeID(SlaverBlue.ID);
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte attack = 0;
    private static final byte debuff = 1;
    private boolean firstTurn;
    
    public SlaverBlueElite(float x, float y) {
        super(NAME, ID, 70, 0F, 0F, 170F, 230F, null, x, y);
        type = EnemyType.ELITE;
        loadAnimation("images/monsters/theBottom/blueSlaver/skeleton.atlas", "images/monsters/theBottom/blueSlaver/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        damage.add(new DamageInfo(this, 15));
        firstTurn = true;
    }
    
    @Override
    public void usePreBattleAction() {
        addToBot(new GainBlockAction(this, 30));
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case debuff:
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new EntangleSkillPower(LMSK.Player(), 1)));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new WeakPlusPower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 1)));
                break;
            case attack:
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int i) {
        if (firstTurn) {
            firstTurn = false;
            setMove(debuff, Intent.STRONG_DEBUFF);
        } else if (lastMove(debuff) || (lastMove(attack) && lastMoveBefore(debuff))) {
            setMove(attack, Intent.ATTACK, damage.get(attack).base);
        } else {
            setMove(debuff, Intent.STRONG_DEBUFF);
        }
    }
}
