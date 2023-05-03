package rs.winds.monsters.beyond;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.BufferPower;
import com.megacrit.cardcrawl.vfx.AwakenedEyeParticle;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.CardCounterPower;
import rs.winds.powers.LifeCounterPower;

public class TestMonsterEx extends AbstractMonster implements LMGameGeneralUtils {
    public static final String ID = King.MakeID("TestMonster");
    private static final byte attack = 0;
    private static final byte smash = 1;
    private static final byte debuff = 2;
    private static final byte buff = 3;
    private static final int atkCount = 3;
    private float particleTimer = 0F;
    private final Bone eye;
    
    public TestMonsterEx(float x, float y) {
        super("【二名】天眼邪教徒", ID, 300, -8.0F, 10.0F, 230F, 240F, null, x, y);
        loadAnimation("images/monsters/theBottom/cultist/skeleton.atlas", "images/monsters/theBottom/cultist/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "waving", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        eye = skeleton.findBone("head");
        damage.add(new DamageInfo(this, 5));
        damage.add(new DamageInfo(this, 45));
        addPower(new LifeCounterPower(this, 30));
        addPower(new CardCounterPower(this, 7));
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case attack:
                for (int i = 0; i < atkCount; i++) {
                    addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                }
                break;
            case smash:
                addToBot(new DamageAction(LMSK.Player(), damage.get(1), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
            case buff:
                addToBot(new GainBlockAction(this, this, 20));
                addToBot(new ApplyPowerAction(this, this, new BufferPower(this, 3)));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (lastMove(attack)) {
            setMove(smash, Intent.ATTACK, damage.get(1).base);
        } else if (lastMove(smash)) {
            setMove(buff, Intent.BUFF);
        } else {
            setMove(attack, Intent.ATTACK, damage.get(0).base, atkCount, true);
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (!isDying) {
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new AwakenedEyeParticle(skeleton.getX() + eye.getWorldX() - scale(8F),
                        skeleton.getY() + eye.getWorldY() + scale(8F)));
            }
        }
    }
}