package rs.winds.monsters.ending;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheEnding;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.localization.StanceStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.CultistMask;
import com.megacrit.cardcrawl.relics.RunicDome;
import com.megacrit.cardcrawl.stances.AbstractStance;
import com.megacrit.cardcrawl.stances.DivinityStance;
import com.megacrit.cardcrawl.stances.NeutralStance;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.LightningEffect;
import com.megacrit.cardcrawl.vfx.stance.DivinityParticleEffect;
import com.megacrit.cardcrawl.vfx.stance.StanceAuraEffect;
import com.megacrit.cardcrawl.vfx.stance.StanceChangeParticleGenerator;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.core.commands.CheatCMD;
import rs.winds.dungeons.CityDepths;
import rs.winds.dungeons.RootDepths;
import rs.winds.monsters.SETool;
import rs.winds.monsters.beyond.TestMonsterEx;
import rs.winds.powers.guniques.*;
import rs.winds.vfx.SEAwakenedEyeParticle;

import java.util.*;

public class EvilGod extends AbstractMonster implements LMGameGeneralUtils {
    public static final String ID = King.MakeID("EvilGod");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    public static final float MAIN_OFFSET_X = -150F;
    public static final float LEFT_OFFSET_X = MAIN_OFFSET_X + 280F;
    public static final float RIGHT_OFFSET_X = MAIN_OFFSET_X - 280F;
    public static boolean DEFEATED = false;
    private static final byte attack_0 = 0;
    private static final byte attack_1 = 1;
    private static final byte attack_2 = 2;
    private static final int intent_0 = 3;
    private static final int atkCount_0 = 7;
    private static final int atkCount_1 = 5;
    private static final int atkCount_2 = 3;
    private static final int bufferAmt = 7;
    private float particleTimer = 0F;
    private AbstractStance stance = new NeutralStance();
    private boolean beKilled;
    private boolean secondStage;
    private boolean[] talks = new boolean[]{false};
    private final Bone eye;
    private final List<AbstractPower> perpetuals = new ArrayList<>();
    private final List<Intent> intents = new ArrayList<>();
    private final Map<Vector2, AbstractMonster> minions = new HashMap<>();
    
    public EvilGod(float x, float y) {
        super(NAME, ID, 666, 0, 0, 230F, 240F, null, x, y);
        loadAnimation("images/monsters/theBottom/cultist/skeleton.atlas", "images/monsters/theBottom/cultist/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "waving", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        eye = skeleton.findBone("head");
        type = EnemyType.BOSS;
        DEFEATED = false;
        beKilled = secondStage = false;
        minions.put(new Vector2(LEFT_OFFSET_X, 50F), null);
        minions.put(new Vector2(RIGHT_OFFSET_X, 50F), null);
        addInitialPower(new ArtifactPower(this, 3){
            @Override
            public void onSpecificTrigger() {
                super.onSpecificTrigger();
                if (owner instanceof EvilGod) {
                    if (amount <= 1) {
                        ((EvilGod) owner).perpetuals.removeIf(p -> p instanceof ArtifactPower);
                    }
                }
            }
        });
        addInitialPower(new PowerLimitationPower(this).apply());
        addInitialPower(new CardLimitationPower(this));
        addInitialPower(new ConfessionPower(this, 200));
        addInitialPower(new ExtraIntentsPower(this));
        addInitialPower(new TrueGodPower(this));
        addInitialPower(new MantraSEPower(this, 4, false));
        addInitialPower(new InvinciblePower(this, 200));
        damage.add(attack_0, new DamageInfo(this, 7));
        damage.add(attack_1, new DamageInfo(this, 10));
        damage.add(attack_2, new DamageInfo(this, 30));
        damage.add(intent_0, new DamageInfo(this, getBlockDamage()));
        intents.add(new Intent(this, 0) {
            @Override
            public void takeTurn() {
                switch (nextMove) {
                    case 0:
                        addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                        break;
                }
            }
            
            @Override
            protected void updateMove() {
                if (LMSK.Player().hasPower(BarricadePower.POWER_ID)) {
                    damage.clear();
                    damage.add(new DamageInfo(source, source.getBlockDamage()));
                    source.damage.set(intent_0, new DamageInfo(source, source.getBlockDamage()));
                    setMove((byte) 0, Intent.ATTACK, damage.get(0).base);
                    applyPowers();
                } else {
                    setNoMove();
                }
                createIntent();
            }
        });
        intents.add(new Intent(this, 1) {
            private static final int atkTimes = 50;
            @Override
            public void takeTurn() {
                switch (nextMove) {
                    case 0:
                        for (int i = 0; i < atkTimes; i++) {
                            addToBot(new DamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                        }
                        break;
                }
            }
    
            @Override
            protected void updateMove() {
                if (LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID) || LMSK.Player().hasPower(IntangiblePower.POWER_ID)) {
                    damage.clear();
                    damage.add(new DamageInfo(source, 1));
                    setMove((byte) 0, Intent.ATTACK, damage.get(0).base, atkTimes, true);
                    applyPowers();
                } else {
                    setNoMove();
                }
                createIntent();
            }
        });
    }
    
    private void addInitialPower(AbstractPower powerToApply) {
        addPower(powerToApply);
        perpetuals.add(powerToApply);
    }
    
    @Override
    public void addPower(AbstractPower powerToApply) {
        super.addPower(powerToApply);
        if (perpetuals.stream().anyMatch(p -> p.ID.equals(powerToApply.ID))) {
            perpetuals.stream().filter(p -> p.ID.equals(powerToApply.ID))
                    .findFirst()
                    .ifPresent(p -> {
                        AbstractPower t = getPower(powerToApply.ID);
                        if (t != null) p.amount = t.amount;
                    });
        }
    }
    
    @Override
    public void usePreBattleAction() {
        DEFEATED = false;
        CardCrawlGame.music.unsilenceBGM();
        AbstractDungeon.scene.fadeOutAmbiance();
        CardCrawlGame.music.playTempBgmInstantly("SE_EG_BGM.mp3", true);
    }
    
    public void onPowersModified() {
        if (!perpetuals.isEmpty()) {
            perpetuals.forEach(p -> {
                AbstractPower po = getPower(p.ID);
                if (po == null) {
                    if (p instanceof PowerLimitationPower)
                        addPower(((PowerLimitationPower) p).apply());
                    else addPower(p);
                }
            });
        }
        intents.forEach(Intent::updateMove);
    }
    
    public void onInstantlyKilled() {
        if (CheatCMD.CHEATING) return;
        AbstractDungeon.getCurrRoom().cannotLose = true;
        beKilled = true;
    }
    
    private int getBlockDamage() {
        return King.PlayerBarricadeBlockLastTurn;
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case attack_0:
                if (!talks[0]) {
                    addToBot(new TalkAction(this, DIALOG[0]));
                }
                for (int i = 0; i < atkCount_0; i++) {
                    addToBot(new SFXAction("ORB_LIGHTNING_EVOKE"));
                    addToBot(new VFXAction(new LightningEffect(LMSK.Player().drawX, LMSK.Player().drawY)));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(attack_0), AbstractGameAction.AttackEffect.NONE));
                }
                addToBot(new ApplyPowerAction(this, this, new BufferPower(this, bufferAmt)));
                applyIntents();
                break;
            case attack_1:
                for (int i = 0; i < atkCount_1; i++) {
                    addToBot(new SFXAction("ORB_LIGHTNING_EVOKE"));
                    addToBot(new VFXAction(new LightningEffect(LMSK.Player().drawX, LMSK.Player().drawY)));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(attack_1), AbstractGameAction.AttackEffect.NONE));
                }
                applyIntents();
                if (secondStage) {
                    addToBot(new RemoveDebuffsAction(this));
                    addToBot(new ApplyPowerAction(this, this, new ArtifactPower(this, 3)));
                    addToBot(new ApplyPowerAction(this, this, new RitualPower(this, 3, false)));
                } else {
                    addToBot(new QuickAction(() -> {
                        for (int i = 0; i < 2; i++) {
                            Optional<AbstractPower> opt = getExptRandomPower(powers, monsterAiRng(), 
                                    p -> isPowerTypeOf(p, AbstractPower.PowerType.DEBUFF));
                            if (!opt.isPresent()) break;
                            addToTop(new RemoveSpecificPowerAction(this, this, opt.get()));
                        }
                    }));
                    addToBot(new ApplyPowerAction(this, this, new ArtifactPower(this, 2)));
                    addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 3)));
                }
                break;
            case attack_2:
                addToBot(new TalkAction(this, DIALOG[MathUtils.random(3, 5)]));
                for (int i = 0; i < atkCount_2; i++) {
                    addToBot(new SFXAction("ORB_LIGHTNING_EVOKE"));
                    addToBot(new VFXAction(new LightningEffect(LMSK.Player().drawX, LMSK.Player().drawY)));
                    addToBot(new DamageAction(LMSK.Player(), damage.get(attack_2), AbstractGameAction.AttackEffect.NONE));
                }
                applyIntents();
                if (secondStage) {
                    addToBot(new QuickAction(() -> {
                        for (AbstractMonster m : LMSK.GetAllExptMstr(m -> m != this)) {
                            addToTop(new InstantKillAction(m));
                        }
                    }));
                    AbstractMonster cultist = new Cultist(LEFT_OFFSET_X, 50F);
                    addToBot(new SpawnMonsterAction(cultist, false));
                    cultist = new TestMonsterEx(RIGHT_OFFSET_X, 50F);
                    addToBot(new SpawnMonsterAction(cultist, false));
                } else {
                    minions.forEach((k,v) -> {
                        if (v == null || v.isDeadOrEscaped()) {
                            addToBot(new QuickAction(() -> {
                                AbstractMonster cultist = new Cultist(k.x, k.y){
                                    @Override
                                    public void usePreBattleAction() {
                                        minions.put(k, this);
                                    }
                                    @Override
                                    public void die() {
                                        super.die();
                                        minions.replace(k, null);
                                    }
                                };
                                cultist.usePreBattleAction();
                                addToTop(new SpawnMonsterAction(cultist, false));
                            }));
                        }
                    });
                }
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    private void applyIntents() {
        intents.forEach(i -> {
            i.takeTurn();
            i.updateMove();
        });
    }
    
    @Override
    protected void getMove(int roll) {
        if (lastMove(attack_0)) {
            setMove(attack_1, AbstractMonster.Intent.ATTACK_BUFF, damage.get(attack_1).base, atkCount_1, true);
        } else if (lastMove(attack_1)) {
            setMove(attack_2, AbstractMonster.Intent.ATTACK, damage.get(attack_2).base, atkCount_2, true);
        } else {
            setMove(attack_0, AbstractMonster.Intent.ATTACK_BUFF, damage.get(attack_0).base, atkCount_0, true);
        }
    }
    
    public void changeStance(AbstractStance stance) {
        if (!this.stance.ID.equals(stance.ID)) {
            this.stance.onExitStance();
            this.stance = stance;
            this.stance.onEnterStance();
        }
    }
    
    @Override
    public void damage(DamageInfo info) {
        int tempHP = currentHealth;
        super.damage(info);
        if (tempHP > currentHealth && !isDying) {
            AbstractPower p = getPower(ConfessionPower.ID);
            if (p instanceof ConfessionPower) {
                p.amount -= tempHP - currentHealth;
                if (p.amount <= 0) {
                    p.amount = ((ConfessionPower) p).extraAmt;
                    p.onSpecificTrigger();
                }
            }
        }
        if (currentHealth <= 0 && beKilled) {
            beKilled = false;
            AbstractDungeon.getCurrRoom().cannotLose = false;
            if (!secondStage) {
                secondStage = true;
                addToTop(new TalkAction(this, DIALOG[1]));
                changeStance(new SEDivinityStance(this));
                increaseMaxHp(66666 - maxHealth, true);
                addInitialPower(new ArtifactPower(this, 10){
                    @Override
                    public void onSpecificTrigger() {
                        super.onSpecificTrigger();
                        if (owner instanceof EvilGod) {
                            if (amount <= 1) {
                                ((EvilGod) owner).perpetuals.removeIf(p -> p instanceof ArtifactPower);
                            }
                        }
                    }
                });
                addInitialPower(new GodCursePower(this));
                AbstractPower p = getPower(InvinciblePower.POWER_ID);
                if (p instanceof InvinciblePower) {
                    p.amount = 20000;
                    SETool.setField(InvinciblePower.class, p, "maxAmt", 20000);
                }
                perpetuals.removeIf(po -> po instanceof ConfessionPower);
                p = getPower(ConfessionPower.ID);
                if (p != null) {
                    powers.removeIf(po -> po instanceof ConfessionPower);
                    p.onRemove();
                    AbstractDungeon.onModifyPower();
                }
            }
            heal(maxHealth);
            setMove(attack_0, AbstractMonster.Intent.ATTACK_BUFF, damage.get(attack_0).base, atkCount_0, true);
            createIntent();
        }
    }
    
    @Override
    public void die() {
        if (!AbstractDungeon.getCurrRoom().cannotLose) {
            addToTop(new TalkAction(this, DIALOG[2]));
            super.die();
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> !m.isDeadOrEscaped())) {
                addToTop(new EscapeAction(m));
            }
            onBossVictoryLogic();
            onFinalBossVictoryLogic();
            CardCrawlGame.stopClock = true;
            DEFEATED = true;
        }
    }
    
    @Override
    public void update() {
        super.update();
        if (!isDying) {
            stance.update();
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new SEAwakenedEyeParticle(skeleton.getX() + eye.getWorldX() - scale(8F), 
                        skeleton.getY() + eye.getWorldY() + scale(8F), Color.PURPLE.cpy()));
            }
            intents.forEach(Intent::update);
        }
    }
    
    @Override
    public void render(SpriteBatch sb) {
        stance.render(sb);
        super.render(sb);
        intents.forEach(i -> i.render(sb));
    }
    
    @Override
    public void renderTip(SpriteBatch sb) {
        tips.clear();
        if (intentAlphaTarget == 1F && !LMSK.Player().hasRelic(RunicDome.ID) && intent != AbstractMonster.Intent.NONE) {
            PowerTip intentTip = SETool.getField(AbstractMonster.class, this, "intentTip");
            tips.add(intentTip);
            intents.forEach(i -> {
                if (i.nextMove != Intent.NONE) {
                    PowerTip iTip = SETool.getField(AbstractMonster.class, i, "intentTip");
                    tips.add(iTip);
                }
            });
        }
        if (!NeutralStance.STANCE_ID.equals(stance.ID))
            tips.add(new PowerTip(stance.name, stance.description));
        for (AbstractPower p : this.powers) {
            if (p.region48 != null) {
                tips.add(new PowerTip(p.name, p.description, p.region48));
                continue;
            }
            tips.add(new PowerTip(p.name, p.description, p.img));
        }
        if (!tips.isEmpty()) {
            if (this.hb.cX + this.hb.width / 2.0F < TIP_X_THRESHOLD) {
                TipHelper.queuePowerTips(this.hb.cX + this.hb.width / 2.0F + TIP_OFFSET_R_X, this.hb.cY +
                        TipHelper.calculateAdditionalOffset(tips, this.hb.cY), tips);
            } else {
                TipHelper.queuePowerTips(this.hb.cX - this.hb.width / 2.0F + TIP_OFFSET_L_X, this.hb.cY +
                        TipHelper.calculateAdditionalOffset(tips, this.hb.cY), tips);
            }
        }
    }
    
    @Override
    public void renderPowerTips(SpriteBatch sb) {
        tips.clear();
        if (!NeutralStance.STANCE_ID.equals(stance.ID))
            tips.add(new PowerTip(stance.name, stance.description));
        for (AbstractPower p : this.powers) {
            if (p.region48 != null) {
                tips.add(new PowerTip(p.name, p.description, p.region48));
                continue;
            }
            tips.add(new PowerTip(p.name, p.description, p.img));
        }
        if (!tips.isEmpty()) {
            if (this.hb.cX + this.hb.width / 2.0F < TIP_X_THRESHOLD) {
                TipHelper.queuePowerTips(this.hb.cX + this.hb.width / 2.0F + TIP_OFFSET_R_X, this.hb.cY +
                        TipHelper.calculateAdditionalOffset(tips, this.hb.cY), tips);
            } else {
                TipHelper.queuePowerTips(this.hb.cX - this.hb.width / 2.0F + TIP_OFFSET_L_X, this.hb.cY + 
                        TipHelper.calculateAdditionalOffset(tips, this.hb.cY), tips);
            }
        }
    }
    
    @Override
    public void applyStartOfTurnPowers() {
        stance.atStartOfTurn();
        super.applyStartOfTurnPowers();
    }
    
    @Override
    public void applyEndOfTurnTriggers() {
        stance.onEndOfTurn();
        super.applyEndOfTurnTriggers();
    }
    
    public static class SEDivinityStance extends AbstractStance {
        private static final StanceStrings strings = CardCrawlGame.languagePack.getStanceString(DivinityStance.STANCE_ID);
        private final AbstractCreature owner;
        private static long sfxId;
    
        public SEDivinityStance(AbstractCreature owner) {
            ID = King.MakeID(DivinityStance.STANCE_ID);
            name = strings.NAME;
            this.owner = owner;
            updateDescription();
        }
    
        @Override
        public float atDamageGive(float damage, DamageInfo.DamageType type) {
            return type == DamageInfo.DamageType.NORMAL ? damage * 3.0F : damage;
        }
    
        @Override
        public void onEndOfTurn() {
            if (owner instanceof EvilGod && !((EvilGod) owner).secondStage)
                ((EvilGod) owner).changeStance(new NeutralStance());
        }
    
        @Override
        public void updateDescription() {
            description = strings.DESCRIPTION[0];
        }
    
        @Override
        public void onEnterStance() {
            if (sfxId != -1L) {
                this.stopIdleSfx();
            }
            CardCrawlGame.sound.play("STANCE_ENTER_DIVINITY");
            sfxId = CardCrawlGame.sound.playAndLoop("STANCE_LOOP_DIVINITY");
            AbstractDungeon.effectsQueue.add(new BorderFlashEffect(Color.PINK, true));
            AbstractDungeon.effectsQueue.add(new StanceChangeParticleGenerator(owner.hb.cX, owner.hb.cY, "Divinity"));
        }
    
        @Override
        public void onExitStance() {
            stopIdleSfx();
        }
    
        @Override
        public void stopIdleSfx() {
            if (sfxId != -1L) {
                CardCrawlGame.sound.stop("STANCE_LOOP_DIVINITY", sfxId);
                sfxId = -1L;
            }
        }
    
        @Override
        public void updateAnimation() {
            if (!Settings.DISABLE_EFFECTS) {
                particleTimer -= Gdx.graphics.getDeltaTime();
                if (particleTimer < 0.0F) {
                    particleTimer = 0.2F;
                    AbstractDungeon.effectsQueue.add(new SEDivinityParticleEffect(owner));
                }
            }
    
            particleTimer2 -= Gdx.graphics.getDeltaTime();
            if (particleTimer2 < 0.0F) {
                particleTimer2 = MathUtils.random(0.45F, 0.55F);
                AbstractDungeon.effectsQueue.add(new SEStanceAuraEffect(owner, "Divinity"));
            }
        }
        
        public static class SEDivinityParticleEffect extends DivinityParticleEffect {
            public SEDivinityParticleEffect(@NotNull AbstractCreature owner) {
                super();
                TextureAtlas.AtlasRegion img = SETool.getField(DivinityParticleEffect.class, this, "img");
                float x = owner.hb.cX + MathUtils.random(-owner.hb.width / 2F - 50F * Settings.scale, 
                        owner.hb.width / 2.0F + 50.0F * Settings.scale);
                float y = owner.hb.cY + MathUtils.random(-owner.hb.height / 2F + 10F * Settings.scale, 
                        owner.hb.height / 2.0F - 20.0F * Settings.scale);
                if (x > owner.hb.cX) {
                    rotation = -rotation;
                }
                x -= img.packedWidth / 2F;
                y -= img.packedHeight / 2F;
                SETool.setField(DivinityParticleEffect.class, this, "x", x);
                SETool.setField(DivinityParticleEffect.class, this, "y", y);
            }
        }
        
        public static class SEStanceAuraEffect extends StanceAuraEffect {
            private static boolean switcher = true;
            
            public SEStanceAuraEffect(@NotNull AbstractCreature owner, String stanceId) {
                super(stanceId);
                TextureAtlas.AtlasRegion img = SETool.getField(StanceAuraEffect.class, this, "img");
                float x = owner.hb.cX + MathUtils.random(-owner.hb.width / 16F, owner.hb.width / 16F);
                float y = owner.hb.cY + MathUtils.random(-owner.hb.height / 16F, owner.hb.height / 12F);
                x -= img.packedWidth / 2F;
                y -= img.packedHeight / 2F;
                SETool.setField(StanceAuraEffect.class, this, "x", x);
                SETool.setField(StanceAuraEffect.class, this, "y", y);
                switcher = !switcher;
                renderBehind = true;
                rotation = MathUtils.random(360.0F);
                if (switcher) {
                    renderBehind = true;
                    SETool.setField(StanceAuraEffect.class, this, "vY", MathUtils.random(0.0F, 40.0F));
                } else {
                    renderBehind = false;
                    SETool.setField(StanceAuraEffect.class, this, "vY", MathUtils.random(0.0F, -40.0F));
                }
            }
        }
    }
    
    private static abstract class Intent extends AbstractMonster {
        private static final byte NONE = Byte.MIN_VALUE;
        protected EvilGod source;
        private final int index;
        
        public Intent(EvilGod source, int index) {
            super(source.name, source.id, 0, 0F, 0F, 0F, 0F, null, 0F, 0F);
            this.source = source;
            this.index = index;
            drawX = source.drawX + (hb.width / 2F + 50F * index) * Settings.scale;
            drawY = source.drawY;
            updateHitbox(0, 0, 230F, 240F);
            nextMove = NONE;
            powers = source.powers;
        }
    
        @SpireOverride
        protected void calculateDamage(int dmg) {
            SpireSuper.call(dmg);
            int intentDmg = SETool.getField(AbstractMonster.class, this, "intentDmg");
            float tmp = intentDmg;
            tmp = source.stance.atDamageGive(tmp, DamageInfo.DamageType.NORMAL);
            if (LMSK.Player().hasPower(IntangiblePower.POWER_ID) || LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID))
                tmp = Math.min(tmp, King.INTANGIBLE_FINAL_DAMAGE);
            if (tmp < 0) tmp = 0;
            intentDmg = MathUtils.floor(tmp);
            ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentDmg", intentDmg);
//            int sourceIntentDmg = SETool.getField(AbstractMonster.class, source, "intentDmg");
//            source.calculateDamage(dmg);
//            int tmp = SETool.getField(AbstractMonster.class, source, "intentDmg");
//            SETool.setField(AbstractMonster.class, source, "intentDmg", sourceIntentDmg);
//            ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentDmg", tmp);
        }
    
        @Override
        public void applyPowers() {
            boolean applyBackAttack = SETool.getMethod(AbstractMonster.class, "applyBackAttack").invoke(source);
            if (applyBackAttack && !hasPower("BackAttack"))
                AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(this, null, new BackAttackPower(source)));
            for (DamageInfo dmg : this.damage) {
                dmg.applyPowers(dmg.owner, AbstractDungeon.player);
                if (applyBackAttack)
                    dmg.output = (int)(dmg.output * 1.5F);
                dmg.output = (int) source.stance.atDamageGive(dmg.output, DamageInfo.DamageType.NORMAL);
                if (LMSK.Player().hasPower(IntangiblePower.POWER_ID) || LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID))
                    dmg.output = Math.min(dmg.output, King.INTANGIBLE_FINAL_DAMAGE);
            }
            EnemyMoveInfo move = SETool.getField(AbstractMonster.class, this, "move");
            if (move.baseDamage > -1)
                calculateDamage(move.baseDamage);
            Texture intentImg = SETool.getMethod(AbstractMonster.class, "getIntentImg").invoke(this);
            SETool.setField(AbstractMonster.class, this, "intentImg", intentImg);
            SETool.getMethod(AbstractMonster.class, "updateIntentTip").invoke(this);
        }
    
        @Override
        public void render(SpriteBatch sb) {
            if (!source.isDeadOrEscaped() && nextMove != NONE) {
                SETool.getMethod(AbstractMonster.class, "renderIntentVfxBehind", SpriteBatch.class).invoke(this, sb);
                SETool.getMethod(AbstractMonster.class, "renderIntent", SpriteBatch.class).invoke(this, sb);
                SETool.getMethod(AbstractMonster.class, "renderIntentVfxAfter", SpriteBatch.class).invoke(this, sb);
                SETool.getMethod(AbstractMonster.class, "renderDamageRange", SpriteBatch.class).invoke(this, sb);
                intentHb.render(sb);
            }
        }
    
        @Override
        public void update() {
            if (!source.isDeadOrEscaped()) {
                if (nextMove != NONE) 
                    SETool.getMethod(AbstractMonster.class, "updateIntent").invoke(this);
                drawX = source.drawX + (hb.width / 2F + 50F * index) * Settings.scale;
                drawY = source.drawY;
                updateHitbox(0, 0, 230F, 240F);
            }
        }
    
        protected abstract void updateMove();
    
        @Override
        protected void getMove(int i) {}
        
        protected void setNoMove() {
            setMove(NONE, Intent.UNKNOWN);
        }
    }
    
    @Override
    public void applyPowers() {
        boolean applyBackAttack = SETool.getMethod(AbstractMonster.class, "applyBackAttack").invoke(this);
        if (applyBackAttack && !hasPower("BackAttack"))
            AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(this, null, new BackAttackPower(this)));
        for (DamageInfo dmg : this.damage) {
            dmg.applyPowers(this, AbstractDungeon.player);
            if (applyBackAttack)
                dmg.output = (int)(dmg.output * 1.5F);
            dmg.output = (int) stance.atDamageGive(dmg.output, DamageInfo.DamageType.NORMAL);
            if (LMSK.Player().hasPower(IntangiblePower.POWER_ID) || LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID))
                dmg.output = Math.min(dmg.output, King.INTANGIBLE_FINAL_DAMAGE);
        }
        EnemyMoveInfo move = SETool.getField(AbstractMonster.class, this, "move");
        if (move.baseDamage > -1)
            calculateDamage(move.baseDamage);
        Texture intentImg = SETool.getMethod(AbstractMonster.class, "getIntentImg").invoke(this);
        SETool.setField(AbstractMonster.class, this, "intentImg", intentImg);
        SETool.getMethod(AbstractMonster.class, "updateIntentTip").invoke(this);
        intents.stream().filter(i -> i.nextMove != Intent.NONE).forEach(Intent::applyPowers);
    }
    
    @SpireOverride
    protected void calculateDamage(int dmg) {
        SpireSuper.call(dmg);
        int intentDmg = SETool.getField(AbstractMonster.class, this, "intentDmg");
        float tmp = intentDmg;
        tmp = stance.atDamageGive(tmp, DamageInfo.DamageType.NORMAL);
        if (LMSK.Player().hasPower(IntangiblePower.POWER_ID) || LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID))
            tmp = Math.min(tmp, King.INTANGIBLE_FINAL_DAMAGE);
        if (tmp < 0) tmp = 0;
        intentDmg = MathUtils.floor(tmp);
        ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentDmg", intentDmg);
    }
    
    @SpirePatch2(clz = RemoveSpecificPowerAction.class, method = "update")
    public static class RemoveSpecificPowerActionPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractGameAction __instance, String ___powerToRemove, AbstractPower ___powerInstance) {
            if (__instance.target instanceof EvilGod) {
                if (!((EvilGod) __instance.target).perpetuals.isEmpty()) {
                    List<AbstractPower> tmp = ((EvilGod) __instance.target).perpetuals;
                    if (___powerToRemove != null) 
                        __instance.isDone = tmp.stream().anyMatch(p -> p.ID.equals(___powerToRemove));
                    if (___powerInstance != null)
                        __instance.isDone = tmp.stream().anyMatch(p -> p.ID.equals(___powerInstance.ID));
                }
                if (__instance.isDone) 
                    return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch2(clz = ProceedButton.class, method = "update")
    public static class ProceedButtonEndingPatch {
        @SpireInstrumentPatch
        public static ExprEditor Instrument1() {
            return new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("equals".equals(m.getMethodName()) && m.getLineNumber() > 115) {
                        m.replace("{$_=" + ProceedButtonEndingPatch.class.getName() + ".FinalAct($0);}");
                    }
                }
            };
        }
        public static boolean FinalAct(String actID) {
            return Arrays.asList(TheEnding.ID, CityDepths.ID, RootDepths.ID).contains(actID);
        }
        
        @SpireInstrumentPatch
        public static ExprEditor Instrument2() {
            return new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("goToTrueVictoryRoom".equals(m.getMethodName())) {
                        m.replace("{if(" + ProceedButtonEndingPatch.class.getName() + ".DoubleBoss())" +
                                "{$0.goToDoubleBoss();}else{$_=$proceed($$);}}");
                    }
                }
            };
        }
        public static boolean DoubleBoss() {
            boolean db = AbstractDungeon.ascensionLevel >= 20 && LMSK.Player().hasRelic(CultistMask.ID);
            if (db && !AbstractDungeon.bossList.isEmpty()) {
                AbstractDungeon.bossList.clear();
                AbstractDungeon.bossList.add(ID);
                if (CardCrawlGame.stopClock)
                    CardCrawlGame.stopClock = false;
                return true;
            }
            return false;
        }
    }
}