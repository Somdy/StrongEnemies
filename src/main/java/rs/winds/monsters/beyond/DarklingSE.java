package rs.winds.monsters.beyond;

import basemod.interfaces.CloneablePowerInterface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.PhysicalReboundPower;
import rs.winds.powers.SpellReboundPower;
import rs.winds.vfx.SEAwakenedEyeParticle;

import java.util.Optional;

public class DarklingSE extends AbstractMonster implements LMGameGeneralUtils {
    public static final String ID = King.MakeID(Darkling.ID);
    private static final MonsterStrings strings = King.MonsterStrings(Darkling.ID);
    private static final byte reborn = -2;
    private static final byte dying = -1;
    private static final byte multi = 0;
    private static final byte smash = 1;
    private static final byte buff = 2;
    private static final int multiCount = 3;
    private float particleTimer = 0F;
    private final Bone eye;
    private boolean firstTurn;
    
    public DarklingSE(float x, float y) {
        super("[二名] 白疾风小黑", ID, 275, 0F, -20F, 260F, 200F, null, x, y + 20F);
        loadAnimation("SEAssets/images/monsters/darkling/skeleton.atlas", "SEAssets/images/monsters/darkling/skeleton.json", 1F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        e.setTimeScale(MathUtils.random(0.75F, 1.0F));
        eye = skeleton.findBone("eye_main_L");
        damage.add(new DamageInfo(this, 8));
        damage.add(new DamageInfo(this, 15));
        firstTurn = true;
        addPower(new RegrowPower(this));
        addPower(new RegenerateMonsterPower(this, 5));
        addPower(new BufferPower(this, 1));
        addPower(new PhysicalReboundPower(this));
        addPower(new SpellReboundPower(this));
        addPower(new TimeMazePower(this, 15));
    }
    
    @Override
    public void usePreBattleAction() {
        CardCrawlGame.music.unsilenceBGM();
        CardCrawlGame.music.playTempBgmInstantly("SE_Colossuem_2_BGM.mp3", true);
    }
    
    @Override
    public void takeTurn() {
        addToBot(new ApplyPowerAction(this, this, new BufferPower(this, 1)));
        switch (nextMove) {
            case multi:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                for (int i = 0; i < multiCount; i++) {
                    addToBot(new DamageAction(LMSK.Player(), damage.get(multi), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                }
                break;
            case smash:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                addToBot(new DamageAction(LMSK.Player(), damage.get(multi), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                break;
            case buff:
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 4)));
                addToBot(new GainBlockAction(this, 15));
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        isDone = true;
                        Optional<AbstractPower> p = getExptRandomPower(LMSK.Player().powers, monsterAiRng(), 
                                po -> isPowerTypeOf(po, AbstractPower.PowerType.BUFF));
                        p.ifPresent(po -> addToTop(new RemoveSpecificPowerAction(LMSK.Player(), DarklingSE.this, po)));
                    }
                });
                if (firstTurn) {
                    firstTurn = false;
                    setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
                    return;
                }
                break;
            case dying:
                addToBot(new TextAboveCreatureAction(this, strings.DIALOG[0]));
                setMove(reborn, Intent.BUFF);
                return;
            case reborn:
                if (MathUtils.randomBoolean()) {
                    addToBot(new SFXAction("DARKLING_REGROW_2", MathUtils.random(-0.1F, 0.1F)));
                } else {
                    addToBot(new SFXAction("DARKLING_REGROW_1", MathUtils.random(-0.1F, 0.1F)));
                }
                addToBot(new HealAction(this, this, maxHealth));
                addToBot(new ChangeStateAction(this, "REVIVE"));
                for (AbstractRelic r : LMSK.Player().relics)
                    r.onSpawnMonster(this);
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (firstTurn) {
            setMove(buff, Intent.STRONG_DEBUFF);
            return;
        }
        if (roll < 40) {
            if (!lastTwoMoves(multi)) {
                setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
            } else if (monsterAiRng().randomBoolean()) {
                setMove(smash, Intent.ATTACK, damage.get(smash).base);
            } else {
                setMove(buff, Intent.STRONG_DEBUFF);
            }
        } else if (roll < 70) {
            if (!lastMove(buff)) {
                setMove(buff, Intent.STRONG_DEBUFF);
            } else if (monsterAiRng().randomBoolean()) {
                setMove(smash, Intent.ATTACK, damage.get(smash).base);
            } else {
                setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
            }
        } else if (!lastTwoMoves(smash)) {
            setMove(smash, Intent.ATTACK, damage.get(smash).base);
        } else if (monsterAiRng().randomBoolean() && !lastMove(buff)) {
            setMove(buff, Intent.STRONG_DEBUFF);
        } else {
            setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
        }
    }
    
    @Override
    public void changeState(String key) {
        switch (key) {
            case "ATTACK":
                state.setAnimation(0, "Attack", false);
                state.addAnimation(0, "Idle", true, 0F);
                break;
            case "REVIVE":
                halfDead = false;
                break;
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (!isDying) {
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new SEAwakenedEyeParticle(skeleton.getX() + eye.getWorldX(), skeleton.getY() + eye.getWorldY(),
                        LMSK.Color(192, 0, 0)));
            }
        }
    }
    
    @Override
    public void die() {
        if (!AbstractDungeon.getCurrRoom().cannotLose)
            super.die();
    }
    
    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        if (currentHealth <= 0 && !halfDead) {
            halfDead = true;
            for (AbstractPower p : powers) {
                p.onDeath();
            }
            for (AbstractRelic r : LMSK.Player().relics) {
                r.onMonsterDeath(this);
            }
            boolean allDead = true;
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> m instanceof Darkling || m instanceof DarklingSE)) {
                if (!m.halfDead) {
                    allDead = false;
                    break;
                }
            }
            if (!allDead && nextMove != dying) {
                setMove(dying, Intent.UNKNOWN);
                createIntent();
            } else {
                AbstractDungeon.getCurrRoom().cannotLose = false;
                halfDead = false;
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    m.die();
                }
            }
        } else if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
            state.setAnimation(0, "Hit", false);
            state.addAnimation(0, "Idle", true, 0F);
        }
    }
    
    private void onDarklingDie() {
        decreaseMaxHealth(50);
    }
    
    @SpirePatch2(clz = Darkling.class, method = "damage")
    public static class DarklingDamagePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"allDead"})
        public static void Insert(@ByRef boolean[] allDead) {
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> m instanceof DarklingSE)) {
                if (!m.halfDead) {
                    allDead[0] = false;
                    ((DarklingSE) m).onDarklingDie();
                }
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(Logger.class, "info");
                int line = LineFinder.findAllInOrder(ctBehavior, matcher)[1];
                return new int[]{line};
            }
        }
    }
}