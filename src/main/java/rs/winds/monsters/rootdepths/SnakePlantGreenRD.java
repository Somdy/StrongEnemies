package rs.winds.monsters.rootdepths;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SnakePlant;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.vfx.combat.BiteEffect;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.PoisonProofPower;
import rs.winds.powers.ToughPower;
import rs.winds.powers.TwistPower;

public class SnakePlantGreenRD extends AbstractMonster {
    public static final String ID = King.MakeID(SnakePlant.ID);
    private static final MonsterStrings strings = King.MonsterStrings(SnakePlant.ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte attack = 0;
    private static final byte buff = 1;
    private static final int multi = 3;
    
    public SnakePlantGreenRD(float x, float y) {
        super("绿蛇花", ID, 275, 0F, 0F, 350F, 360F, null, x, y);
        type = EnemyType.ELITE;
        loadAnimation("images/monsters/theCity/snakePlant/skeleton.atlas", "images/monsters/theCity/snakePlant/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        stateData.setMix("Hit", "Idle", 0.1F);
        e.setTimeScale(0.8F);
        addPower(new ArtifactPower(this, 2));
        addPower(new ToughPower(this, 5, 5));
        addPower(new PoisonProofPower(this, 3));
        addPower(new TwistPower(this, 1));
        damage.add(new DamageInfo(this, 10));
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case attack:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                for (int i = 0; i < multi; i++) {
                    addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F) * Settings.scale, 
                            LMSK.Player().hb.cY + MathUtils.random(-50F, 50F) * Settings.scale, Color.CHARTREUSE.cpy()), 0.2F));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.NONE, true));
                }
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new VulnerablePower(this, 2, true)));
                break;
            case buff:
                for (AbstractMonster m : LMSK.GetAllExptMstr(m -> true)) {
                    addToBot(new GainBlockAction(m, this, 30));
                }
                addToBot(new ApplyPowerToEnemiesAction(this, ToughPower.class, ApplyPowerParam.ANY_OWNER, 2, 2));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (lastMove(attack)) {
            setMove(buff, Intent.DEFEND_BUFF);
        } else {
            setMove(attack, Intent.ATTACK_DEBUFF, damage.get(0).base, multi, true);
        }
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
            state.setAnimation(0, "Hit", false);
            state.addAnimation(0, "Idle", true, 0F);
        }
    }
}