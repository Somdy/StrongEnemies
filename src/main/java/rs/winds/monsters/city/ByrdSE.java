package rs.winds.monsters.city;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Byrd;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.AwakenedEyeParticle;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.FrustratedPower;
import rs.winds.powers.SpecialFlightPower;
import rs.winds.vfx.SEAwakenedEyeParticle;

public class ByrdSE extends AbstractMonster {
    public static final String ID = King.MakeID(Byrd.ID);
    private static final MonsterStrings strings = King.MonsterStrings(Byrd.ID);
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte multi = 0;
    private static final byte attack = 1;
    private static final byte groundbuff = 2;
    private static final byte buff = 3;
    private static final byte getup = 4;
    private static final byte knockedown = 5;
    private static final int multiCount = 10;
    private static final int flightAmt = 10;
    private boolean grounded;
    private float particleTimer = 0F;
    private final Bone eye;
    
    public ByrdSE(float x, float y) {
        super("[二名] 天空亡者异鸟", ID, 125, 0F, 50F, 240F, 180F, null, x, y);
        loadAnimation("images/monsters/theCity/byrd/flying.atlas", "images/monsters/theCity/byrd/flying.json", 1F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle_flap", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        eye = skeleton.findBone("eye");
        grounded = false;
        damage.add(new DamageInfo(this, 1));
        damage.add(new DamageInfo(this, 15));
        damage.add(new DamageInfo(this, 10));
        addPower(new SpecialFlightPower(this, flightAmt));
        addPower(new FrustratedPower(this));
        addPower(new RegenerateMonsterPower(this, 5));
    }
    
    @Override
    public void usePreBattleAction() {
        CardCrawlGame.music.unsilenceBGM();
        CardCrawlGame.music.playTempBgmInstantly("SE_Colossuem_2_BGM.mp3", true);
    }
    
    @Override
    public void takeTurn() {
        if (!grounded && !hasPower(SpecialFlightPower.ID)) {
            addToBot(new ApplyPowerAction(this, this, new SpecialFlightPower(this, flightAmt)));
        }
        switch (nextMove) {
            case multi:
                addToBot(new AnimateFastAttackAction(this));
                for (int i = 0; i < multiCount; i++) {
                    addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.BLUNT_LIGHT, true));
                }
                break;
            case attack:
                addToBot(new AnimateFastAttackAction(this));
                addToBot(new DamageAction(LMSK.Player(), damage.get(1), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
            case buff:
                addToBot(new SFXAction("BYRD_DEATH"));
                addToBot(new TalkAction(this, DIALOG[0], 1.2F, 1.2F));
                addToBot(new ApplyPowerAction(this, this, new ThornsPower(this, 2)));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 2)));
                break;
            case groundbuff:
                addToBot(new SetAnimationAction(this, "head_lift"));
                addToBot(new DamageAction(LMSK.Player(), damage.get(groundbuff), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                setMove(getup, Intent.UNKNOWN);
                return;
            case getup:
                grounded = false;
                addToBot(new ChangeStateAction(this, "FLYING"));
                addToBot(new ApplyPowerAction(this, this, new SpecialFlightPower(this, flightAmt)));
                setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
                return;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int i) {
        if (lastMove(multi)) {
            setMove(buff, Intent.BUFF);
        } else {
            setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
        }
    }
    
    @Override
    public void changeState(String stateName) {
        AnimationState.TrackEntry e;
        switch (stateName) {
            case "FLYING":
                grounded = false;
                loadAnimation("images/monsters/theCity/byrd/flying.atlas", "images/monsters/theCity/byrd/flying.json", 1.0F);
                e = this.state.setAnimation(0, "idle_flap", true);
                e.setTime(e.getEndTime() * MathUtils.random());
                updateHitbox(0.0F, 50.0F, 240.0F, 180.0F);
                break;
            case "GROUNDED":
                setMove(groundbuff, Intent.ATTACK, damage.get(groundbuff).base);
                createIntent();
                grounded = true;
                addToBot(new GainBlockAction(this, 20));
                addToBot(new RemoveSpecificPowerAction(this, this, SpecialFlightPower.ID));
                addToBot(new RemoveSpecificPowerAction(this, this, VulnerablePower.POWER_ID));
                addToBot(new RemoveSpecificPowerAction(this, this, WeakPower.POWER_ID));
                loadAnimation("images/monsters/theCity/byrd/grounded.atlas", "images/monsters/theCity/byrd/grounded.json", 1.0F);
                e = this.state.setAnimation(0, "idle", true);
                e.setTime(e.getEndTime() * MathUtils.random());
                updateHitbox(10.0F, -50.0F, 240.0F, 180.0F);
                break;
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (!grounded && !isDying) {
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new AwakenedEyeParticle(skeleton.getX() + eye.getWorldX(), skeleton.getY() + eye.getWorldY()));
            }
        }
    }
    
    public void getKnockedDown() {
        changeState("GROUNDED");
    }
}