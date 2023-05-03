package rs.winds.monsters.rootdepths;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.cards.status.Growth;
import rs.winds.core.King;
import rs.winds.monsters.SETool;
import rs.winds.powers.*;

import java.util.Collections;

public class TheHolyTree extends AbstractMonster {
    public static final String ID = King.MakeID("TheHolyTree");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte attack = 0;
    private static final byte multiatk = 1;
    private static final byte getgrowth = 5;
    private static final byte buffpoison = 6;
    private static final byte reborn = -9;
    private static final int multiCount = 9;
    private int atkStr = 0;
    private float leafFallTimer;
    private boolean secondStage;
    private boolean thirdStage;
    
    public TheHolyTree(float x, float y) {
        super(NAME, ID, 700, 0, 0, 328, 310, null, x, y);
        type = EnemyType.BOSS;
        loadAnimation("SEAssets/images/monsters/WisdomTree/WisdomTree.atlas", "SEAssets/images/monsters/WisdomTree/WisdomTree.json", 1F);
        AnimationState.TrackEntry e = state.setAnimation(0, "WickerUp", false);
        state.addAnimation(0, "IdleLeafBunch", true, 0F);
        e.setTime(e.getEndTime() * MathUtils.random());
        stateData.setMix("WickerDown", "IdleWicke", 0.5F);
        secondStage = false;
        thirdStage = false;
        addPower(new ArtifactPower(this, 3));
        addPower(new ToughPower(this, 5, 5));
        addPower(new InvinciblePower(this, 500));
        addPower(new TreeGrowthPower(this, 30));
        addPower(new TreeImmunePower(this, 300));
        damage.add(attack, new DamageInfo(this, 45 + atkStr));
        damage.add(multiatk, new DamageInfo(this, 10 + atkStr));
        Collections.sort(powers);
    }
    
    @Override
    public void usePreBattleAction() {
        AbstractDungeon.getCurrRoom().cannotLose = true;
        CardCrawlGame.music.unsilenceBGM();
        AbstractDungeon.scene.fadeOutAmbiance();
        CardCrawlGame.music.playTempBgmInstantly("SE_TheHolyTree_BGM.mp3", true);
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case getgrowth:
                if (secondStage) {
                    Growth growth = new Growth();
                    growth.upgrade();
                    addToBot(new MakeTempCardInDrawPileAction(growth, 5, true, true));
                    addToBot(new ApplyPowerAction(LMSK.Player(), this, new TreePoisonPower(LMSK.Player(), this, 10)));
                    addToBot(new RemoveAllBlockAction(LMSK.Player(), this));
                } else {
                    addToBot(new MakeTempCardInDrawPileAction(new Growth(), 3, true, true));
                }
                break;
            case attack:
                if (secondStage) {
                    addToBot(new ChangeStateAction(this, "attack"));
                }
                addToBot(new VFXAction(new WeightyImpactEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY)));
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack)));
                updateDamageInfo();
                applyEnchantmentTrigger(1);
                break;
            case multiatk:
                addToBot(new ChangeStateAction(this, "attack"));
                for (int i = 0; i < multiCount; i++) {
                    addToBot(new SFXAction("EVENT_VAMP_BITE", 0.05F));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(multiatk)));
                }
                if (thirdStage) {
                    Growth growth = new Growth();
                    growth.upgrade();
                    addToBot(new MakeTempCardInDrawPileAction(growth, 2, true, true));
                    addToBot(new HealAction(this, this, MathUtils.floor(maxHealth * 0.1F)));
                }
                updateDamageInfo();
                applyEnchantmentTrigger(multiCount);
                break;
            case buffpoison:
                if (secondStage) {
                    addToBot(new QuickAction(() -> {
                        if (powers.stream().anyMatch(p -> p instanceof WeakPower || (p instanceof StrengthPower && p.amount < 0))) {
                            powers.stream().filter(p -> p instanceof WeakPower || (p instanceof StrengthPower && p.amount < 0))
                                    .forEach(p -> addToTop(new RemoveSpecificPowerAction(this, this, p)));
                        }
                    }));
                }
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new TreePoisonPower(LMSK.Player(), this, 10)));
                addToBot(new HealAction(this, this, MathUtils.floor((maxHealth - currentHealth)
                        * (secondStage ? 0.4F : 0.2F))));
                break;
            case reborn:
                secondStage = true;
                halfDead = false;
                addToBot(new SFXAction("MONSTER_SNECKO_GLARE"));
                addToBot(new QuickAction(() -> {
                    state.setAnimation(0, "WickerDown", false);
                    state.addAnimation(0, "IdleWicke", true, 0F);
                }));
                addToBot(new HealAction(this, this, maxHealth));
                addToBot(new RemoveAllPowersAction(LMSK.Player(), false));
                addToBot(new RemoveAllBlockAction(LMSK.Player(), this));
//                addToBot(new ApplyPowerAction(this, this, new ToughPower(this, 5, 5)));
                addToBot(new QuickAction(() -> {
                    AbstractPower p = getPower(InvinciblePower.POWER_ID);
                    if (p instanceof InvinciblePower) {
                        p.amount = 400;
                        SETool.setField(InvinciblePower.class, p, "maxAmt", 400);
                    }
                    p = getPower(TreeGrowthPower.ID);
                    if (p instanceof TreeGrowthPower) {
                        ((TreeGrowthPower) p).modify(15);
                    }
                    p = getPower(TreeImmunePower.ID);
                    if (p instanceof TreeImmunePower) {
                        ((TreeImmunePower) p).modify(200);
                    }
                }));
                setMove(multiatk, Intent.ATTACK, damage.get(multiatk).base, multiCount, true);
                return;
        }
        addToBot(new RollMoveAction(this));
    }
    
    private void updateDamageInfo() {
        atkStr += 10;
        damage.clear();
        damage.add(attack, new DamageInfo(this, 45 + atkStr));
        damage.add(multiatk, new DamageInfo(this, 10));
    }
    
    @Override
    protected void getMove(int roll) {
        if (secondStage) {
            if (thirdStage) {
                setMove(multiatk, Intent.ATTACK, damage.get(multiatk).base, multiCount, true);
            } else {
                if (lastMove(getgrowth)) {
                    setMove(buffpoison, Intent.BUFF);
                } else if (lastMove(buffpoison)) {
                    setMove(multiatk, Intent.ATTACK, damage.get(multiatk).base, multiCount, true);
                } else {
                    setMove(getgrowth, Intent.STRONG_DEBUFF);
                }
            }
        } else {
            if (lastMove(getgrowth)) {
                setMove(attack, Intent.ATTACK, damage.get(attack).base);
            } else if (lastMove(attack)) {
                setMove(buffpoison, Intent.BUFF);
            } else {
                setMove(getgrowth, Intent.STRONG_DEBUFF);
            }
        }
    }
    
    @Override
    public void changeState(String stateName) {
        switch (stateName) {
            case "attack":
                state.setAnimation(0, "Attack", false);
                state.addAnimation(0, "IdleWicke", true, 0F);
                break;
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (leafFallTimer > 0) {
            leafFallTimer -= Gdx.graphics.getDeltaTime();
        }
    }
    
    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        if (currentHealth <= 0 && !halfDead && !secondStage) {
            halfDead = true;
            for (AbstractPower p : powers) {
                p.onDeath();
            }
            for (AbstractRelic r : LMSK.Player().relics) {
                r.onMonsterDeath(this);
            }
            setMove(reborn, Intent.BUFF);
            createIntent();
        } else {
            if (secondStage && currentHealth <= 200 && !thirdStage) {
                AbstractDungeon.getCurrRoom().cannotLose = false;
                thirdStage = true;
                for (AbstractPower p : powers) {
                    p.onDeath();
                }
                for (AbstractRelic r : LMSK.Player().relics) {
                    r.onMonsterDeath(this);
                }
                AbstractPower p = getPower(ToughPower.ID);
                if (p instanceof ToughPower) {
                    p.amount = 99;
                    ((ToughPower) p).extraAmt = 99;
                    p.updateDescription();
                }
                heal(maxHealth, true);
                addPower(new TreeEnchantmentPower(this));
                setMove(multiatk, Intent.ATTACK, damage.get(multiatk).base, multiCount, true);
                createIntent();
            }
            if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
                if (secondStage) {
                    state.addAnimation(1, "Hit", false, 0F);
                }
                if (leafFallTimer <= 0F) {
                    int roll = MathUtils.random(1, 3);
                    AnimationState.TrackEntry e = state.addAnimation(1, "LeafFall" + roll, false, 0F);
                    leafFallTimer = e.getEndTime() * MathUtils.random(0.25F, 0.75F);
                }
            }
        }
    }
    
    @Override
    public void die() {
        if (!AbstractDungeon.getCurrRoom().cannotLose) {
            super.die();
            onBossVictoryLogic();
            onFinalBossVictoryLogic();
            CardCrawlGame.stopClock = true;
        }
    }
    
    public void applyEnchantmentTrigger(int amount) {
        AbstractPower p = getPower(TreeEnchantmentPower.ID);
        if (p instanceof TreeEnchantmentPower) {
            ((TreeEnchantmentPower) p).onSpecificTrigger(amount);
        }
    }
}