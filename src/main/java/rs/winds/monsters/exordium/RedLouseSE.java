package rs.winds.monsters.exordium;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.AwakenedEyeParticle;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.PoisonEndurancePower;
import rs.winds.vfx.SEAwakenedEyeParticle;

public class RedLouseSE extends AbstractMonster {
    public static final String ID = King.MakeID(LouseNormal.ID);
    private static final int curlupAmt = 15;
    private static final byte attack = 0;
    private static final byte debuff = 1;
    private boolean isOpen = true;
    private float particleTimer = 0F;
    private final Bone eye;
    
    public RedLouseSE(float x, float y) {
        super("[二名] 矛碎虱虫", ID, 75, 0F, -5F, 180F, 140F, null, x, y);
        loadAnimation("SEAssets/images/monsters/redlouse/skeleton.atlas", "SEAssets/images/monsters/redlouse/skeleton.json", 1F);
        AnimationState.TrackEntry e = state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        eye = skeleton.findBone("antright");
        damage.add(new DamageInfo(this, 7));
        addPower(new BarricadePower(this));
        addPower(new ThornsPower(this, 3));
        addPower(new AngryPower(this, 3));
        addPower(new PoisonEndurancePower(this));
        addPower(new CurlUpPower(this, curlupAmt));
    }
    
    @Override
    public void takeTurn() {
        if (!hasPower(CurlUpPower.POWER_ID)) {
            addToBot(new ApplyPowerAction(this, this, new CurlUpPower(this, curlupAmt)));
        }
        switch (nextMove) {
            case attack:
                if (!isOpen) {
                    addToBot(new ChangeStateAction(this, "OPEN"));
                }
                for (int i = 0; i < 2; i++) {
                    addToBot(new AnimateFastAttackAction(this));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                }
                break;
            case debuff:
                if (!isOpen) {
                    addToBot(new ChangeStateAction(this, "REAR"));
                } else {
                    addToBot(new ChangeStateAction(this, "REAR_IDLE"));
                }
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new VulnerablePower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new WeakPower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new EntanglePower(LMSK.Player())));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int i) {
        if (lastMove(attack)) {
            setMove(debuff, Intent.STRONG_DEBUFF);
        } else {
            setMove(attack, Intent.ATTACK, damage.get(0).base, 2, true);
        }
    }
    
    public void changeState(String stateName) {
        if (stateName.equals("CLOSED")) {
            this.state.setAnimation(0, "transitiontoclosed", false);
            this.state.addAnimation(0, "idle closed", true, 0.0F);
            this.isOpen = false;
        } else if (stateName.equals("OPEN")) {
            this.state.setAnimation(0, "transitiontoopened", false);
            this.state.addAnimation(0, "idle", true, 0.0F);
            this.isOpen = true;
        } else if (stateName.equals("REAR_IDLE")) {
            this.state.setAnimation(0, "rear", false);
            this.state.addAnimation(0, "idle", true, 0.0F);
            this.isOpen = true;
        } else {
            this.state.setAnimation(0, "transitiontoopened", false);
            this.state.addAnimation(0, "rear", false, 0.0F);
            this.state.addAnimation(0, "idle", true, 0.0F);
            this.isOpen = true;
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (!isDying) {
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new SEAwakenedEyeParticle(skeleton.getX() + eye.getWorldX(), skeleton.getY() + eye.getWorldY(), Color.RED.cpy()));
            }
        }
    }
}
