package rs.winds.monsters.rootdepths;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SnakePlant;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.vfx.combat.BiteEffect;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.lazymankits.utils.LMSK;
import rs.winds.actions.unique.ApplySECuriosityAction;
import rs.winds.core.King;
import rs.winds.powers.*;
import rs.winds.rewards.ApoReward;
import rs.winds.rewards.NightmareReward;

public class SnakePlantPurpleRD extends AbstractMonster {
    public static final String ID = King.MakeID(SnakePlant.ID);
    private static final MonsterStrings strings = King.MonsterStrings(SnakePlant.ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte megadebuff = -1;
    private static final byte attack = 0;
    private static final byte buff = 1;
    private boolean firstMove;
    
    public SnakePlantPurpleRD(float x, float y) {
        super("紫蛇花", ID, 275, 0F, 0F, 350F, 360F, null, x, y);
        type = EnemyType.ELITE;
        loadAnimation("SEAssets/images/monsters/snakePlantPurple/skeleton.atlas", "SEAssets/images/monsters/snakePlantPurple/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        stateData.setMix("Hit", "Idle", 0.1F);
        e.setTimeScale(0.8F);
        addPower(new ArtifactPower(this, 2));
        addPower(new ToughPower(this, 5, 5));
        addPower(new SlightImmunePower(this, 50));
        addPower(new SuppressPower(this, 2));
        damage.add(new DamageInfo(this, 45));
        firstMove = true;
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case megadebuff:
                firstMove = false;
                addToBot(new ApplySECuriosityAction(LMSK.Player(), this, 2));
                if (LMSK.MonsterRng().randomBoolean(0.5F)) {
                    setMove(attack, Intent.ATTACK, damage.get(0).base);
                } else {
                    setMove(buff, Intent.DEBUFF);
                }
                return;
            case attack:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F) * Settings.scale,
                        LMSK.Player().hb.cY + MathUtils.random(-50F, 50F) * Settings.scale, Color.CHARTREUSE.cpy()), 0.2F));
                addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.NONE));
                break;
            case buff:
                addToBot(new ApplyPowerToEnemiesAction(this, StrengthPower.class, ApplyPowerParam.ANY_OWNER, 5));
                addToBot(new MakeTempCardInDrawPileAction(new Slimed(), 3, true, true));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (firstMove) {
            setMove(megadebuff, Intent.STRONG_DEBUFF);
            return;
        }
        if (lastMove(attack)) {
            setMove(buff, Intent.DEBUFF);
        } else {
            setMove(attack, Intent.ATTACK, damage.get(0).base);
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
    
    @Override
    public void die() {
        super.die();
        AbstractDungeon.getCurrRoom().rewards.add(new NightmareReward());
    }
}