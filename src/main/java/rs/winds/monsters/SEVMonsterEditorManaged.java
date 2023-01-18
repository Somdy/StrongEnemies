package rs.winds.monsters;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.ClearCardQueueAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.*;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.blights.Shield;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.beyond.*;
import com.megacrit.cardcrawl.monsters.city.*;
import com.megacrit.cardcrawl.monsters.ending.CorruptHeart;
import com.megacrit.cardcrawl.monsters.ending.SpireShield;
import com.megacrit.cardcrawl.monsters.ending.SpireSpear;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.*;
import javassist.*;
import javassist.bytecode.*;
import javassist.convert.Transformer;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.cards.HeartOfSpire;
import rs.winds.core.King;
import rs.winds.patches.DropRewardPatch;
import rs.winds.powers.EntangleSkillPower;
import rs.winds.powers.MawAngerPower;
import rs.winds.powers.PowerStealerPower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.winds.monsters.SETool.*;

@SuppressWarnings("unused")
public class SEVMonsterEditorManaged {
    // Exordium
    @SEMonsterEditor(m = AcidSlime_S.class)
    public static class AcidSlimeSSE {
        private static final byte attack = 1;
        private static final byte weak = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.putBool("firstTurn", true);
            e.initFunc = m -> {
                setMonsterHp(m, 13);
                m.damage.get(0).base = 6;
                m.damage.get(0).output = 6;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), BLUNT_HEAVY));
                        e.setBool("firstTurn", false);
                        break;
                    case weak:
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 1, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                } else {
                    if (lastMove(m, attack)) {
                        m.setMove(weak, AbstractMonster.Intent.DEBUFF);
                    } else {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = AcidSlime_M.class)
    public static class AcidSlimeMSE {
        private static final byte slime_attack = 0;
        private static final byte weaken_attack = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                m.damage.clear();
                m.damage.add(slime_attack, new DamageInfo(m, 9));
                m.damage.add(weaken_attack, new DamageInfo(m, 11));
            };
            e.initThis = m -> {
                setMonsterHp(m, 35);
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case slime_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), BLUNT_LIGHT));
                        m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 1));
                        break;
                    case weaken_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(1), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 50 && !lastTwoMoves(m, slime_attack)) {
                    m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                } else {
                    if (!lastTwoMoves(m, weaken_attack)) {
                        m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                    } else {
                        m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = AcidSlime_L.class)
    public static class AcidSlimeLSE {
        private static final byte slime_attack = 0;
        private static final byte weaken_attack = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                m.damage.clear();
                m.damage.add(slime_attack, new DamageInfo(m, 12));
                m.damage.add(weaken_attack, new DamageInfo(m, 16));
            };
            e.initThis = m -> {
                setMonsterHp(m, 75);
            };
            e.takeTurn = m -> {
                if (getBool(AcidSlime_L.class, m, "splitTriggered") && m.nextMove == 3) {
                    return false;
                }
                switch (m.nextMove) {
                    case slime_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), BLUNT_LIGHT));
                        m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 2));
                        break;
                    case weaken_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(1), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 3, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 60) {
                    m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                } else {
                    m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                }
                return true;
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 3)));
                m.addToBot(new GainBlockAction(m, m, 10));
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SpikeSlime_S.class)
    public static class SpikeSlimeSSE {
        private static final byte slime_attack = 0;
        private static final byte frail_attack = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 19);
                m.damage.clear();
                m.damage.add(slime_attack, new DamageInfo(m, 6));
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case slime_attack:
                        e.setBool("firstTurn", false);
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), BLUNT_HEAVY));
                        break;
                    case frail_attack:
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 1, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(slime_attack, AbstractMonster.Intent.ATTACK, m.damage.get(slime_attack).base);
                } else {
                    if (lastMove(m, slime_attack)) {
                        m.setMove(frail_attack, AbstractMonster.Intent.DEBUFF);
                    } else {
                        m.setMove(slime_attack, AbstractMonster.Intent.ATTACK, m.damage.get(slime_attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SpikeSlime_M.class)
    public static class SpikeSlimeMSE {
        private static final byte slime_attack = 0;
        private static final byte frail_attack = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                m.damage.clear();
                m.damage.add(slime_attack, new DamageInfo(m, 9));
                m.damage.add(frail_attack, new DamageInfo(m, 11));
            };
            e.initThis = m -> {
                setMonsterHp(m, 36);
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case slime_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(slime_attack), BLUNT_HEAVY));
                        m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 1));
                        break;
                    case frail_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(frail_attack), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 2, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 50 && !lastTwoMoves(m, slime_attack)) {
                    m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                } else {
                    if (!lastTwoMoves(m, frail_attack)) {
                        m.setMove(frail_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(frail_attack).base);
                    } else {
                        m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SpikeSlime_L.class)
    public static class SpikeSlimeLSE {
        private static final byte slime_attack = 0;
        private static final byte frail_attack = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                m.damage.clear();
                m.damage.add(slime_attack, new DamageInfo(m, 12));
                m.damage.add(frail_attack, new DamageInfo(m, 16));
            };
            e.initThis = m -> {
                setMonsterHp(m, 75);
            };
            e.takeTurn = m -> {
                if (getBool(SpikeSlime_L.class, m, "splitTriggered") && m.nextMove == 3) {
                    return false;
                }
                switch (m.nextMove) {
                    case slime_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(slime_attack), BLUNT_HEAVY));
                        m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 2));
                        break;
                    case frail_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(frail_attack), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 3, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 60) {
                    m.setMove(slime_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(slime_attack).base);
                } else {
                    m.setMove(frail_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(frail_attack).base);
                }
                return true;
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 3)));
                m.addToBot(new GainBlockAction(m, m, 10));
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Cultist.class)
    public static class CultistSE {
        private static final byte attack = 0;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 62);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 8));
                m.addPower(new RitualPower(m, 5, false){
                    @Override
                    public void atEndOfRound() {
                        flash();
                        addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, this.amount)));
                    }
                });
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_HORIZONTAL));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = LouseDefensive.class)
    public static class LouseDefensiveSE {
        private static final byte attack = 0;
        private static final byte grow = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 15);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 5));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new CurlUpPower(m, 15)));
                return true;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        if (!getBool(LouseDefensive.class, m, "isOpen")) {
                            m.addToBot(new ChangeStateAction(m, "REAR"));
                        } else {
                            m.addToBot(new ChangeStateAction(m, "REAR_IDLE"));
                        }
                        m.addToBot(new WaitAction(0.5F));
                        m.addToBot(new SFXAction("ATTACK_MAGIC_FAST_3", 0.6F));
                        m.addToBot(new VFXAction(new WebEffect(LMSK.Player(), m.hb.cX - 70F * Settings.scale, 
                                m.hb.cY + 10F * Settings.scale)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), BLUNT_LIGHT));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        break;
                    case grow:
                        if (!getBool(LouseDefensive.class, m, "isOpen")) {
                            m.addToBot(new ChangeStateAction(m, "OPEN"));
                            m.addToBot(new WaitAction(0.5F));
                        }
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 65 && !lastTwoMoves(m, attack)) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                } else {
                    if (!lastMove(m, grow)) {
                        m.setMove(grow, AbstractMonster.Intent.BUFF);
                    } else {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = LouseNormal.class)
    public static class LouseNormalSE {
        private static final byte attack = 0;
        private static final byte grow = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 15);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 5));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new CurlUpPower(m, 15)));
                return true;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        if (!getBool(LouseNormal.class, m, "isOpen")) {
                            m.addToBot(new ChangeStateAction(m, "REAR"));
                        } else {
                            m.addToBot(new ChangeStateAction(m, "REAR_IDLE"));
                        }
                        m.addToBot(new WaitAction(0.5F));
                        m.addToBot(new SFXAction("ATTACK_MAGIC_FAST_3", 0.6F));
                        m.addToBot(new VFXAction(new WebEffect(LMSK.Player(), m.hb.cX - 70F * Settings.scale,
                                m.hb.cY + 10F * Settings.scale)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), BLUNT_LIGHT));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        break;
                    case grow:
                        if (!getBool(LouseNormal.class, m, "isOpen")) {
                            m.addToBot(new ChangeStateAction(m, "OPEN"));
                            m.addToBot(new WaitAction(0.5F));
                        }
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (roll < 65 && !lastTwoMoves(m, attack)) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                } else {
                    if (!lastMove(m, grow)) {
                        m.setMove(grow, AbstractMonster.Intent.BUFF);
                    } else {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = JawWorm.class)
    public static class JawWormSE {
        private static final byte attack_defend = 0;
        private static final byte attack_vul = 1;
        private static final byte pure_attack = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 50);
                m.damage.clear();
                m.damage.add(attack_defend, new DamageInfo(m, 0));
                m.damage.add(attack_vul, new DamageInfo(m, 7));
                m.damage.add(pure_attack, new DamageInfo(m, 12));
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack_defend:
                        e.setBool("firstTurn", false);
                        m.state.setAnimation(0, "tailslam", false);
                        m.state.addAnimation(0, "idle", true, 0F);
                        m.addToBot(new SFXAction("MONSTER_JAW_WORM_BELLOW"));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 5)));
                        m.addToBot(new GainBlockAction(m, m, 10));
                        break;
                    case attack_vul:
                        m.addToBot(new SetAnimationAction(m, "chomp"));
                        m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack_vul), NONE));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        break;
                    case pure_attack:
                        m.addToBot(new AnimateHopAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(pure_attack), BLUNT_LIGHT));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(attack_defend, AbstractMonster.Intent.DEFEND_BUFF);
                } else {
                    if (lastMove(m, attack_defend)) {
                        m.setMove(attack_vul, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack_vul).base);
                    } else if (lastMove(m, attack_vul)) {
                        m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                    } else {
                        m.setMove(attack_defend, AbstractMonster.Intent.DEFEND_BUFF);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = FungiBeast.class)
    public static class FungiBeastSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 30);
                m.damage.get(0).base = 6;
                m.damage.get(0).output = 6;
                m.addPower(new MetallicizePower(m, 5));
                m.addPower(new StrengthPower(m, 1));
            };
        }
    }
    @SEMonsterEditor(m = GremlinThief.class)
    public static class GremlinThiefSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 15);
            };
            e.takeTurn = m -> {
                if (m.nextMove == 10) {
                    m.addToBot(new AnimateSlowAttackAction(m));
                    m.addToBot(new VampireDamageAction(LMSK.Player(), m.damage.get(0), SLASH_HORIZONTAL));
                    if (m.escapeNext) {
                        m.addToBot(new SetMoveAction(m, (byte) 99, AbstractMonster.Intent.ESCAPE));
                    } else {
                        m.addToBot(new SetMoveAction(m, (byte) 10, AbstractMonster.Intent.ATTACK_BUFF, m.damage.get(0).base));
                    }
                    return true;
                }
                return false;
            };
            e.getMove = (m, roll) -> {
                m.setMove((byte) 10, AbstractMonster.Intent.ATTACK_BUFF, m.damage.get(0).base);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = GremlinWizard.class)
    public static class GremlinWizardSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 26);
            };
        }
    }
    @SEMonsterEditor(m = GremlinTsundere.class)
    public static class GremlinTsundereSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 15);
            };
            e.takeTurn = m -> {
              if (m.nextMove == 10) {
                  m.addToBot(new QuickAction(() -> {
                      for (AbstractMonster mo : LMSK.GetAllExptMstr(mo -> true)) {
                          m.addToTop(new GainBlockAction(mo, m, 7));
                      }
                  }));
                  if (m.escapeNext) {
                      m.addToBot(new SetMoveAction(m, (byte) 99, AbstractMonster.Intent.ESCAPE));
                  } else {
                      int aliveCount = LMSK.GetAllExptMstr(mo -> true).size();
                      if (aliveCount > 1) {
                          m.addToBot(new SetMoveAction(m, GremlinTsundere.MOVES[0], (byte) 10, AbstractMonster.Intent.DEFEND));
                      } else {
                          m.addToBot(new SetMoveAction(m, GremlinTsundere.MOVES[1], (byte) 2, AbstractMonster.Intent.ATTACK, m.damage.get(0).base));
                      }
                  }
                  return true;
              }
              return false;
            };
            e.getMove = (m, roll) -> {
                m.setMove(GremlinTsundere.MOVES[0], (byte) 10, AbstractMonster.Intent.DEFEND);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = GremlinWarrior.class)
    public static class GremlinWarriorSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 25);
                m.damage.get(0).base = 7;
                m.damage.get(0).output = 7;
                m.addPower(new RitualPower(m, 2, false){
                    @Override
                    public void atEndOfRound() {
                        flash();
                        addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, this.amount)));
                    }
                });
            };
        }
    }
    @SEMonsterEditor(m = GremlinFat.class)
    public static class GremlinFatSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 18);
                m.damage.get(0).base = 6;
                m.damage.get(0).output = 6;
            };
        }
    }
    @SEMonsterEditor(m = Looter.class)
    public static class LooterSE {
        private static final byte attack = 0;
        private static final byte escape = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 50);
                m.damage.clear();
                m.damage.add(0, new DamageInfo(m, 12));
                m.addPower(new MetallicizePower(m, 5));
            };
            e.putInt("attackCount", 0);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        int attackCount = e.getInt("attackCount");
                        attackCount++;
                        int goldAmt = getField(Looter.class, m, "goldAmt");
                        getMethod(Looter.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new TalkAction(m, Looter.DIALOG[attackCount < 3 ? 0 : 2], 0.3F, 2F));
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new AbstractGameAction() {
                            @Override
                            public void update() {
                                isDone = true;
                                int stolenGold = getField(Looter.class, m, "stolenGold");
                                stolenGold += Math.min(goldAmt, LMSK.Player().gold);
                                setField(Looter.class, m, "stolenGold", stolenGold);
                            }
                        });
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), goldAmt));
                        if (attackCount >= 3) {
                            m.setMove(escape, AbstractMonster.Intent.ESCAPE);
                        } else {
                            m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                        }
                        e.setInt("attackCount", attackCount);
                        break;
                    case escape:
                        m.addToBot(new TalkAction(m, Looter.DIALOG[3], 0.3F, 2.5F));
                        AbstractDungeon.getCurrRoom().mugged = true;
                        m.addToBot(new VFXAction(new SmokeBombEffect(m.hb.cX, m.hb.cY)));
                        m.addToBot(new EscapeAction(m));
                        m.setMove(escape, AbstractMonster.Intent.ESCAPE);
                        break;
                }
                return true;
            };
            e.getMove = (m, roll) -> {
                m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SlaverBlue.class)
    public static class SlaverBlueSE {
        private static final byte weaken = 0;
        private static final byte pure_attack = 1;
        private static final byte entangle = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 50);
                m.damage.clear();
                m.damage.add(weaken, new DamageInfo(m, 8));
                m.damage.add(pure_attack, new DamageInfo(m, 13));
                m.addPower(new StrengthPower(m, 2));
                m.addPower(new RegenerateMonsterPower(m, 3));
            };
            e.putBool("firstTurn", true);
            e.putBool("entangled", false);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case weaken:
                        e.setBool("firstTurn", false);
                        getMethod(SlaverBlue.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(weaken)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        break;
                    case pure_attack:
                        getMethod(SlaverBlue.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(pure_attack)));
                        break;
                    case entangle:
                        getMethod(SlaverBlue.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new EntangleSkillPower(LMSK.Player(), 1)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(weaken, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken).base);
                } else {
                    if (lastMove(m, weaken)) {
                        if (!e.getBool("entangled")) {
                            e.setBool("entangled", true);
                            m.setMove(entangle, AbstractMonster.Intent.STRONG_DEBUFF);
                        } else {
                            m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                        }
                    } else if (lastMove(m, entangle)) {
                        m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                    } else {
                        m.setMove(weaken, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = GremlinNob.class)
    public static class GremlinNobSE {
        private static final byte attack = 0;
        private static final byte great_attack = 1;
        private static final byte anger = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 100);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 8));
                m.damage.add(great_attack, new DamageInfo(m, 13));
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 1, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 99, true)));
                        m.addToBot(new RollMoveAction(m));
                        break;
                    case great_attack:
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(great_attack), BLUNT_HEAVY));
                        m.setMove(great_attack, AbstractMonster.Intent.ATTACK, m.damage.get(great_attack).base);
                        break;
                    case anger:
                        e.setBool("firstTurn", false);
                        getMethod(GremlinNob.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new TalkAction(m, GremlinNob.DIALOG[0], 1F, 3F));
                        m.addToBot(new ApplyPowerAction(m, m, new AngerPower(m, 3)));
                        m.addToBot(new RollMoveAction(m));
                        break;
                }
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(anger, AbstractMonster.Intent.BUFF);
                } else {
                    if (lastMove(m, anger)) {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                    } else {
                        m.setMove(great_attack, AbstractMonster.Intent.ATTACK, m.damage.get(great_attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Sentry.class)
    public static class SentrySE {
        private static final byte attack = 0;
        private static final byte great_attack = 1;
        private static final byte anger = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 45);
                m.damage.get(0).base = 10;
                m.damage.get(0).output = 10;
                m.addPower(new RegenerateMonsterPower(m, 6));
                m.addPower(new StrengthPower(m, 2));
                setField(Sentry.class, m, "dazedAmt", 1);
            };
        }
        @SpirePatch2(clz = Sentry.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireInsertPatch(rloc = 18)
            public static void Insert(AbstractMonster __instance) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify()) {
                    LMSK.AddToBot(new MakeTempCardInDiscardAction(new VoidCard(), 1));
                }
            }
        }
    }
    @SEMonsterEditor(m = Lagavulin.class)
    public static class LagavulinSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 100);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 18));
                setField(Lagavulin.class, m, "debuff", -1);
                m.addPower(new MetallicizePower(m, 10));
            };
            e.putBool("firstMove", true);
            e.getMove = (m, roll) -> {
                if (e.getBool("firstMove")) {
                    e.setBool("firstMove", false);
                    m.setMove((byte) 3, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                } else if (lastTwoMoves(m, (byte) 3)) {
                    m.setMove(Lagavulin.MOVES[0], (byte) 1, AbstractMonster.Intent.STRONG_DEBUFF);
                } else {
                    m.setMove((byte) 3, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                }
                return true;
            };
        }
        @SpirePatch2(clz = Lagavulin.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireInsertPatch(rloc = 7)
            public static void Insert(AbstractMonster __instance) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify()) {
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new WeakPower(LMSK.Player(), 2, true)));
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new FrailPower(LMSK.Player(), 2, true)));
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new FocusPower(LMSK.Player(), -1)));
                }
            }
        }
        @SpirePatch2(clz = Lagavulin.class, method = SpirePatch.CONSTRUCTOR)
        public static class ConstructorPatch {
            @SpirePrefixPatch
            public static void Prefix(AbstractMonster __instance, @ByRef boolean[] setAsleep) {
                if (GetModifierEditor(__instance).canModify())
                    setAsleep[0] = false;
            }
        }
        @SpirePatch2(clz = Lagavulin.class, method = "usePreBattleAction")
        public static class SkipSetMove {
            @SpireInstrumentPatch
            public static ExprEditor Instrument() {
                return new ExprEditor(){
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if ("setMove".equals(m.getMethodName())) {
                            m.replace("{if(!" + SETool.class.getName() +".GetEditor($0).canModify())$_=$proceed($$);}");
                        }
                    }
                };
            };
        }
    }
    @SEMonsterEditor(m = TheGuardian.class)
    public static class TheGuardianSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 250);
                m.addPower(new BarricadePower(m));
                m.addPower(new StrengthPower(m, 2));
                m.addPower(new ThornsPower(m, 2));
                m.addPower(new MetallicizePower(m, 5));
            };
        }
        @SpirePatch2(clz = TheGuardian.class, method = "changeState")
        public static class ChangeStatePatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(GameActionManager.class, "addToBottom");
                int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
                ctBehavior.instrument(new ExprEditor(){
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if ("addToBottom".equals(m.getMethodName()) && m.getLineNumber() >= lines[lines.length - 1]) {
                            m.replace("{if(" + ChangeStatePatch.class.getName() + ".canLoseBlock(this)){$_=$proceed($$);}}");
                        }
                    }
                });
            }
            public static boolean canLoseBlock(AbstractMonster m) {
                MonsterEditor e = GetEditor(m);
                return !e.canModify();
            }
        }
    }
    @SEMonsterEditor(m = SlimeBoss.class)
    public static class SlimeBossSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 160);
                m.addPower(new RegenerateMonsterPower(m, 3));
            };
        }
        @SpirePatch2(clz = SlimeBoss.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireInsertPatch(rloc = 11)
            public static void Insert(SlimeBoss __instance) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify()) {
                    __instance.addToBot(new MakeTempCardInDiscardAction(new Burn(), 2));
                }
            }
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                ClassPool pool = ctBehavior.getDeclaringClass().getClassPool();
                CtClass bossType = pool.get(SlimeBoss.class.getName());
                ctBehavior.instrument(new CodeConverter(){{
                    this.transformers = new Transformer(this.transformers) {
                        @Override
                        public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool) 
                                throws CannotCompileException, BadBytecode {
                            int pushcode = iterator.byteAt(index);
                            if (pushcode == ICONST_5) {
                                int nextcode = iterator.byteAt(index + 1);
                                int cpcode = iterator.byteAt(index - 1);
                                if (nextcode == INVOKESPECIAL && cpcode == 57) {
                                    Bytecode bc = new Bytecode(constPool);
                                    bc.addAload(0);
                                    bc.addInvokestatic(TakeTurnPatch.class.getName(), "GetSlimeAmount", 
                                            Descriptor.ofMethod(CtClass.intType, new CtClass[]{CtClass.intType, bossType}));
                                    iterator.insert(index + 1, bc.get());
                                }
                            }
                            return index;
                        }
                    };
                }});
            }
            public static int GetSlimeAmount(int defaultValue, SlimeBoss boss) {
                MonsterEditor e = GetEditor(boss);
                if (e.canModify()) {
                    return 3;
                }
                return defaultValue;
            };
        }
    }
    @SEMonsterEditor(m = Hexaghost.class)
    public static class HexaghostSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 250);
                m.addPower(new StrengthPower(m, 1));
                m.addPower(new PlatedArmorPower(m, 5));
                m.addPower(new IntangiblePlayerPower(m, 1));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 5));
                return false;
            };
            e.putInt("turnCount", 0);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                if (turnCount > 0 && turnCount % 7 == 0) {
                    turnCount = 0;
                    m.addToBot(new ApplyPowerAction(m, m, new IntangiblePower(m, 1){
                        @Override
                        public void updateDescription() {
                            description = IntangiblePlayerPower.DESCRIPTIONS[0];
                        }
                    }));
                } else {
                    turnCount++;
                }
                e.setInt("turnCount", turnCount);
                return false;
            };
        }
        @SpirePatch2(clz = Hexaghost.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireInsertPatch(locator = Locator.class, localvars = {"d"})
            public static void Insert(AbstractMonster __instance, @ByRef int[] d) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify()) {
                    d[0] = 6;
                }
            }
            private static class Locator extends SpireInsertLocator {
                @Override
                public int[] Locate(CtBehavior ctBehavior) throws Exception {
                    Matcher.FieldAccessMatcher matcher = new Matcher.FieldAccessMatcher(DamageInfo.class, "base");
                    return LineFinder.findInOrder(ctBehavior, matcher);
                }
            }
        }
    }
    // City
    @SEMonsterEditor(m = Mugger.class)
    public static class MuggerSE {
        private static final byte attack = 0;
        private static final byte escape = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 60);
                m.damage.clear();
                m.damage.add(0, new DamageInfo(m, 12));
                m.addPower(new MetallicizePower(m, 5));
                m.addPower(new PowerStealerPower(m));
            };
            e.putInt("attackCount", 0);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        int attackCount = e.getInt("attackCount");
                        attackCount++;
                        int goldAmt = getField(Mugger.class, m, "goldAmt");
                        getMethod(Mugger.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new TalkAction(m, Mugger.DIALOG[0], 0.3F, 2F));
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new AbstractGameAction() {
                            @Override
                            public void update() {
                                isDone = true;
                                int stolenGold = getField(Mugger.class, m, "stolenGold");
                                stolenGold += Math.min(goldAmt, LMSK.Player().gold);
                                setField(Mugger.class, m, "stolenGold", stolenGold);
                            }
                        });
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), goldAmt));
                        if (attackCount >= 3) {
                            m.setMove(escape, AbstractMonster.Intent.ESCAPE);
                        } else {
                            m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                        }
                        e.setInt("attackCount", attackCount);
                        break;
                    case escape:
                        m.addToBot(new TalkAction(m, Mugger.DIALOG[1], 0.3F, 2.5F));
                        AbstractDungeon.getCurrRoom().mugged = true;
                        m.addToBot(new VFXAction(new SmokeBombEffect(m.hb.cX, m.hb.cY)));
                        m.addToBot(new EscapeAction(m));
                        m.setMove(escape, AbstractMonster.Intent.ESCAPE);
                        break;
                }
                return true;
            };
            e.getMove = (m, roll) -> {
                m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Byrd.class)
    public static class ByrdSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 30);
                m.addPower(new RegenerateMonsterPower(m, 3));
                setField(Byrd.class, m, "flightAmt", 5);
            };
        }
        @SpirePatch2(clz = Byrd.class, method = "changeState")
        public static class GroundedPatch {
            @SpireInsertPatch(locator = Locator.class)
            public static void Insert(AbstractMonster __instance) {
                __instance.addToBot(new GainBlockAction(__instance, __instance, 10));
                __instance.addToBot(new ApplyPowerAction(__instance, __instance, new StrengthPower(__instance, 1)));
            }
            private static class Locator extends SpireInsertLocator {
                @Override
                public int[] Locate(CtBehavior ctBehavior) throws Exception {
                    Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(Byrd.class, "createIntent");
                    return LineFinder.findInOrder(ctBehavior, matcher);
                }
            }
        }
    }
    @SEMonsterEditor(m = Chosen.class)
    public static class ChosenSE {
        private static final byte multi = 0;
        private static final byte fire = 1;
        private static final byte apply = 2;
        private static final byte hex = 4;
        private static final int multiCount = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 100);
                m.damage.clear();
                m.damage.add(multi, new DamageInfo(m, 6));
                m.damage.add(fire, new DamageInfo(m, 21));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new RitualPower(m, 2, false){
                    @Override
                    public void atEndOfRound() {
                        flash();
                        addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, this.amount)));
                    }
                });
                m.addPower(new MetallicizePower(m, 5));
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                if (e.getBool("firstTurn") && m.nextMove == hex) {
                    e.setBool("firstTurn", false);
                    return false;
                }
                switch (m.nextMove) {
                    case apply:
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        break;
                    case fire:
                        m.addToBot(new FastShakeAction(m, 0.3F, 0.5F));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(fire), FIRE));
                        break;
                    case multi:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(multi), SLASH_HORIZONTAL));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(multi), SLASH_VERTICAL));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    setField(Chosen.class, m, "usedHex", true);
                    m.setMove(hex, AbstractMonster.Intent.STRONG_DEBUFF);
                } else {
                    if (roll < 20 && !lastMove(m, apply)) {
                        m.setMove(apply, AbstractMonster.Intent.DEBUFF);
                    } else if (roll < 60 && !lastTwoMoves(m, multi)) {
                        m.setMove(multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                    } else {
                        if (!lastTwoMoves(m, fire)) {
                            m.setMove(fire, AbstractMonster.Intent.ATTACK, m.damage.get(fire).base);
                        } else {
                            m.rollMove();
                        }
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = ShelledParasite.class)
    public static class ShelledParasiteSE {
        private static final byte massive = 0;
        private static final byte multi = 1;
        private static final byte vampire = 2;
        private static final int multiCount = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 71);
                m.damage.clear();
                m.damage.add(massive, new DamageInfo(m, 21));
                m.damage.add(multi, new DamageInfo(m, 8));
                m.damage.add(vampire, new DamageInfo(m, 14));
                m.addPower(new PlatedArmorPower(m, 15));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 20));
                return true;
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case 4:
                        m.addToBot(new TextAboveCreatureAction(m, TextAboveCreatureAction.TextType.STUNNED));
                        break;
                    case massive:
                        e.setBool("firstTurn", false);
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(massive), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 2, true)));
                        break;
                    case multi:
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new AnimateHopAction(m));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(multi), BLUNT_LIGHT));
                        }
                        break;
                    case vampire:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY, Color.GOLD.cpy())));
                        m.addToBot(new VampireDamageAction(LMSK.Player(), m.damage.get(vampire), NONE));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(massive, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(massive).base);
                } else {
                    if (lastMove(m, massive)) {
                        m.setMove(multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                    } else if (lastMove(m, multi)) {
                        m.setMove(vampire, AbstractMonster.Intent.ATTACK_BUFF, m.damage.get(vampire).base);
                    } else {
                        m.setMove(massive, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(massive).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SphericGuardian.class)
    public static class SphericGuardianSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 20);
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 2)));
                m.addToBot(new GainBlockAction(m, m, 30));
                return false;
            };
        }
        @SpirePatch2(clz = SphericGuardian.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                CtClass guardian = ctBehavior.getDeclaringClass().getClassPool().get(SphericGuardian.class.getName());
                ctBehavior.instrument(new CodeConverter(){{
                    this.transformers = new Transformer(this.transformers) {
                        @Override
                        public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool)
                                throws CannotCompileException, BadBytecode {
                            int pushcode = iterator.byteAt(index);
                            if (pushcode == BIPUSH) {
                                int nextcode = iterator.byteAt(index + 1);
                                int invokecode = iterator.byteAt(index + 2);
                                if (nextcode == 15 && invokecode == INVOKESPECIAL) {
                                    Bytecode bc = new Bytecode(constPool);
                                    bc.addAload(0);
                                    bc.addInvokestatic(TakeTurnPatch.class.getName(), "GetGuardianExtraBlock", 
                                            Descriptor.ofMethod(CtClass.intType, new CtClass[]{guardian}));
                                    bc.add(Opcode.IADD);
                                    iterator.insert(index + 2, bc.get());
                                }
                            }
                            return index;
                        }
                    };
                }});
            }
            public static int GetGuardianExtraBlock(SphericGuardian guardian) {
                MonsterEditor e = GetEditor(guardian);
                if (e.canModify()) {
                    return 5;
                }
                return 0;
            }
        }
    }
    @SEMonsterEditor(m = Centurion.class)
    public static class CenturionSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 80);
                m.addPower(new RegenerateMonsterPower(m, 5));
                m.addPower(new ArtifactPower(m, 1));
            };
        }
    }
    @SEMonsterEditor(m = Healer.class)
    public static class HealerSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 60);
                m.addPower(new MetallicizePower(m, 5));
                m.addPower(new ArtifactPower(m, 1));
            };
        }
    }
    @SEMonsterEditor(m = SnakePlant.class)
    public static class SnakePlantSE {
        private static final byte attack = 0;
        private static final byte weaken = 1;
        private static final int multiCount = 3;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 80);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 6));
                m.addPower(new ArtifactPower(m, 1));
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX
                                    + MathUtils.random(-50F, 50F) * Settings.scale, LMSK.Player().hb.cY
                                    + MathUtils.random(-50F, 50F) * Settings.scale, Color.CHARTREUSE.cpy())));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), NONE));
                        }
                        break;
                    case weaken:
                        e.setBool("firstTurn", false);
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn") || lastTwoMoves(m, attack)) {
                    m.setMove(SnakePlant.MOVES[0], weaken, AbstractMonster.Intent.STRONG_DEBUFF);
                } else {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                }
                return true;
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new MalleablePower(m, 2)));
                return false;
            };
        }
    }
    @SEMonsterEditor(m = Snecko.class)
    public static class SneckoSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 120);
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new BufferPower(m, 2));
            };
        }
    }
    @SEMonsterEditor(m = SlaverRed.class)
    public static class SlaverRedSE {
        private static final byte weaken = 0;
        private static final byte pure_attack = 1;
        private static final byte entangle = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 50);
                m.damage.clear();
                m.damage.add(weaken, new DamageInfo(m, 9));
                m.damage.add(pure_attack, new DamageInfo(m, 14));
            };
            e.putBool("firstTurn", true);
            e.putBool("entangled", false);
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 10));
                return false;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case weaken:
                        e.setBool("firstTurn", false);
                        getMethod(SlaverRed.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(weaken)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        break;
                    case pure_attack:
                        getMethod(SlaverRed.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(pure_attack)));
                        break;
                    case entangle:
                        getMethod(SlaverRed.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new EntanglePower(LMSK.Player())));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(weaken, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken).base);
                } else {
                    if (!e.getBool("entangled")) {
                        e.setBool("entangled", true);
                        m.setMove(entangle, AbstractMonster.Intent.STRONG_DEBUFF);
                    } else if (roll < 40) {
                        m.setMove(weaken, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken).base);
                    } else {
                        m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Taskmaster.class)
    public static class TaskmasterSE {
        private static final byte attack = 0;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 70);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 8));
                m.addPower(new ArtifactPower(m, 1));
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        getMethod(Taskmaster.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new VampireDamageAction(LMSK.Player(), m.damage.get(attack), SLASH_HEAVY));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 1)));
                        m.addToBot(new MakeTempCardInDiscardAction(new Wound(), 3));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                return true;
            };
        }
    }
    @SEMonsterEditor(m = GremlinLeader.class)
    public static class GremlinLeaderSE {
        private static final byte attack = 0;
        private static final byte buff = 1;
        private static final byte rally = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 155);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 9));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new PlatedArmorPower(m, 10));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 10));
                if (m instanceof GremlinLeader) {
                    ((GremlinLeader) m).gremlins[0] = AbstractDungeon.getMonsters().monsters.get(1);
                    ((GremlinLeader) m).gremlins[1] = AbstractDungeon.getMonsters().monsters.get(2);
                    ((GremlinLeader) m).gremlins[2] = AbstractDungeon.getMonsters().monsters.get(0);
                    for (AbstractMonster gremlin : ((GremlinLeader) m).gremlins) {
                        m.addToBot(new ApplyPowerAction(gremlin, m, new MinionPower(gremlin)));
                    }
                    return true;
                }
                return false;
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case buff:
                        m.addToBot(new ShoutAction(m, getMethod(GremlinLeader.class, "getEncourageQuote", new Class[0]).invoke(m)));
                        for (AbstractMonster mo : LMSK.GetAllExptMstr(mo -> !mo.isDeadOrEscaped())) {
                            m.addToBot(new ApplyPowerAction(mo, m, new StrengthPower(mo, 3)));
                            m.addToBot(new GainBlockAction(mo, m, 10));
                        }
                        if (e.getBool("firstTurn")) {
                            e.setBool("firstTurn", false);
                            m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, 3, true);
                            return true;
                        }
                        break;
                    case rally:
                        m.addToBot(new ChangeStateAction(m, "CALL"));
                        if (m instanceof GremlinLeader) {
                            m.addToBot(new SummonGremlinAction(((GremlinLeader) m).gremlins));
                            m.addToBot(new SummonGremlinAction(((GremlinLeader) m).gremlins));
                        }
                        break;
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_HORIZONTAL, true));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_VERTICAL, true));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_HEAVY, true));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(buff, AbstractMonster.Intent.BUFF);
                } else {
                    int livings = getMethod(GremlinLeader.class, "numAliveGremlins", new Class[0]).invoke(m);
                    if (roll < 20) {
                        if (livings < 3 && !lastMove(m, rally)) {
                            m.setMove(GremlinLeader.MOVES[0], rally, AbstractMonster.Intent.UNKNOWN);
                        } else if (MonsterAIRng().randomBoolean(0.5F) && !lastMove(m, buff)) {
                            m.setMove(buff, AbstractMonster.Intent.BUFF);
                        } else {
                            m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, 3, true);
                        }
                    } else if (roll < 60 && !lastMove(m, buff)) {
                        m.setMove(buff, AbstractMonster.Intent.BUFF);
                    } else {
                       if (!lastTwoMoves(m, attack)) {
                           m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, 3, true);
                       } else {
                           m.setMove(buff, AbstractMonster.Intent.BUFF);
                       }
                    }
                }
                return true;
            };
        }
        @SpirePatch2(clz = SummonGremlinAction.class, method = "update")
        public static class SummonActionPatch {
            @SpirePrefixPatch
            public static SpireReturn PrefixGet(AbstractGameAction __instance, AbstractMonster ___m) {
                if (___m == null) {
                    BaseMod.logger.info("Cleaning incorrect summon gremlin action");
                    __instance.isDone = true;
                    return SpireReturn.Return();
                }
                return SpireReturn.Continue();
            }
        }
    }
    @SEMonsterEditor(m = BookOfStabbing.class)
    public static class BookOfStabbingSE {
        private static final byte attack_1 = 0;
        private static final byte attack_2 = 1;
        private static final byte attack_3 = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 170);
                m.damage.clear();
                m.damage.add(attack_1, new DamageInfo(m, 1));
                m.damage.add(attack_2, new DamageInfo(m, 1));
                m.damage.add(attack_3, new DamageInfo(m, 25));
                m.addPower(new ThornsPower(m, 2));
            };
            e.putInt("multiCount", 20);
            e.takeTurn = m -> {
                int multiCount = e.getInt("multiCount");
                switch (m.nextMove) {
                    case attack_1:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new SFXAction("MONSTER_BOOK_STAB_" + MathUtils.random(0, 3)));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack_1), SLASH_VERTICAL));
                        }
                        multiCount += 10;
                        break;
                    case attack_2:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new SFXAction("MONSTER_BOOK_STAB_" + MathUtils.random(0, 3)));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack_2), SLASH_VERTICAL));
                        }
                        multiCount += 10;
                        break;
                    case attack_3:
                        AbstractPower p = m.getPower(StrengthPower.POWER_ID);
                        if (p != null && p.amount < 0) {
                            m.addToBot(new RemoveSpecificPowerAction(m, m, p));
                        }
                        m.addToBot(new ChangeStateAction(m, "ATTACK_2"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack_3), SLASH_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 1, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 1, true)));
                        break;
                }
                e.setInt("multiCount", multiCount);
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                int multiCount = e.getInt("multiCount");
                if (lastMove(m, attack_1)) {
                    m.setMove(attack_2, AbstractMonster.Intent.ATTACK, m.damage.get(attack_2).base, multiCount, true);
                } else if (lastMove(m, attack_2)) {
                    m.setMove(attack_3, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack_3).base);
                } else {
                    m.setMove(attack_1, AbstractMonster.Intent.ATTACK, m.damage.get(attack_1).base, multiCount, true);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = TheCollector.class)
    public static class TheCollectorSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 300);
                m.damage.get(0).base = 20;
                m.damage.get(0).output = 20;
            };
            e.putBool("firstTurn", true);
            e.preBattle = m -> {
                float spawnX = getField(TheCollector.class, m, "spawnX");
                HashMap<Integer, AbstractMonster> enemySlots = getField(TheCollector.class, m, "enemySlots");
                for (int i = 0; i < 2; i++) {
                    AbstractMonster mo = new TorchHead(spawnX - 185F * i, MathUtils.random(-5F, 25F));
                    m.addToBot(new SFXAction("MONSTER_COLLECTOR_SUMMON"));
                    m.addToBot(new SpawnMonsterAction(mo, true));
                    enemySlots.put(i, mo);
                }
                return false;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    setField(TheCollector.class, m, "initialSpawn", false);
                    e.setBool("firstTurn", false);
                    m.setMove((byte) 4, AbstractMonster.Intent.STRONG_DEBUFF);
                    return true;
                }
                return false;
            };
        }
        @SpirePatch2(clz = TheCollector.class, method = "takeTurn")
        public static class TakeTurnPatch {
            @SpireInsertPatch(locator = Locator.class)
            public static void Insert(AbstractMonster __instance) {
                if (GetEditor(__instance).canModify()) {
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new StrengthPower(LMSK.Player(), -2)));
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new DexterityPower(LMSK.Player(), -2)));
                    LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), __instance, new FocusPower(LMSK.Player(), -2)));
                }
            }
            private static class Locator extends SpireInsertLocator {
                @Override
                public int[] Locate(CtBehavior ctBehavior) throws Exception {
                    Matcher.FieldAccessMatcher matcher = new Matcher.FieldAccessMatcher(TheCollector.class, "ultUsed");
                    return LineFinder.findInOrder(ctBehavior, matcher);
                }
            }
        }
    }
    @SEMonsterEditor(m = BronzeAutomaton.class)
    public static class BronzeAutomatonSE {
        private static final byte special_summon = -1;
        private static final byte special_buff = -2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 300);
                m.addPower(new PlatedArmorPower(m, 5));
                m.addPower(new MetallicizePower(m, 5));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 5));
                return false;
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case special_summon:
                        if (MathUtils.randomBoolean()) {
                            m.addToBot(new SFXAction("AUTOMATON_ORB_SPAWN", MathUtils.random(-0.1F, 0.1F)));
                        } else {
                            m.addToBot(new SFXAction("MONSTER_AUTOMATON_SUMMON", MathUtils.random(-0.1F, 0.1F)));
                        }
                        m.addToBot(new SpawnMonsterAction(new BronzeOrb(-300F, 200F, 0), true));
                        m.addToBot(new SpawnMonsterAction(new BronzeOrb(200F, 130F, 1), true));
                        m.addToBot(new SpawnMonsterAction(new BronzeOrb(-300F, 100F, 2), true));
                        m.addToBot(new RollMoveAction(m));
                        return true;
                    case special_buff:
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                        m.addToBot(new ApplyPowerAction(m, m, new MetallicizePower(m, 3)));
                        m.addToBot(new RollMoveAction(m));
                        return true;
                    default:
                        return false;
                }
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    setField(BronzeAutomaton.class, m, "firstTurn", false);
                    e.setBool("firstTurn", false);
                    m.setMove(special_summon, AbstractMonster.Intent.UNKNOWN);
                    return true;
                } else {
                    if (lastMove(m, special_buff)) {
                        if (!lastTwoMoves(m, (byte) 1)) {
                            m.setMove((byte) 1, AbstractMonster.Intent.ATTACK, m.damage.get(0).base, 2, true);
                        } else {
                            String[] MOVES = getStaticField(BronzeAutomaton.class, "MOVES");
                            m.setMove(MOVES[0], (byte) 2, AbstractMonster.Intent.ATTACK, m.damage.get(1).base);
                        }
                    } else if (lastMove(m, (byte) 1)) {
                        if (lastMoveBefore(m, (byte) 1)) {
                            String[] MOVES = getStaticField(BronzeAutomaton.class, "MOVES");
                            m.setMove(MOVES[0], (byte) 2, AbstractMonster.Intent.ATTACK, m.damage.get(1).base);
                        } else {
                            m.setMove((byte) 1, AbstractMonster.Intent.ATTACK, m.damage.get(0).base, 2, true);
                        }
                    } else {
                        m.setMove(special_buff, AbstractMonster.Intent.BUFF);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Champ.class)
    public static class ChampSE {
        private static final byte pure_attack = 0;
        private static final byte weaken_attack = 1;
        private static final byte execution = 2;
        private static final byte megabuff = 3;
        private static final byte buff = 4;
        private static final byte heal = 5;
        private static final int exeCount = 3;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 450);
                m.damage.clear();
                m.damage.add(pure_attack, new DamageInfo(m, 18));
                m.damage.add(weaken_attack, new DamageInfo(m, 14));
                m.damage.add(execution, new DamageInfo(m, 10));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new MetallicizePower(m, 10));
            };
            e.putBool("firstTurn", true);
            e.putBool("megabuff", false);
            e.putInt("turnCount", 1);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                turnCount++;
                e.setInt("turnCount", turnCount);
                switch (m.nextMove) {
                    case pure_attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new VFXAction(new GoldenSlashEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY, false)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(pure_attack), NONE));
                        break;
                    case weaken_attack:
                        m.addToBot(new SFXAction("MONSTER_CHAMP_SLAP"));
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(weaken_attack), BLUNT_LIGHT));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        break;
                    case execution:
                        m.addToBot(new AnimateJumpAction(m));
                        m.addToBot(new VFXAction(new GoldenSlashEffect(LMSK.Player().hb.cX - 50F * Settings.scale, 
                                LMSK.Player().hb.cY, true)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(execution), NONE));
                        m.addToBot(new VFXAction(new GoldenSlashEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY, true)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(execution), NONE));
                        m.addToBot(new VFXAction(new GoldenSlashEffect(LMSK.Player().hb.cX + 50F * Settings.scale, 
                                LMSK.Player().hb.cY, true)));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(execution), NONE));
                        if (AbstractDungeon.aiRng.randomBoolean(0.5F)) {
                            m.setMove(Champ.MOVES[2], weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                        } else {
                            m.setMove(heal, AbstractMonster.Intent.BUFF);
                        }
                        return true;
                    case megabuff:
                        e.setInt("turnCount", 0);
                        e.setBool("megabuff", true);
                        m.addToBot(new SFXAction("MONSTER_CHAMP_CHARGE"));
                        m.addToBot(new ShoutAction(m, getMethod(Champ.class, "getLimitBreak", new Class[0]).invoke(m), 
                                2F, 3F));
                        m.addToBot(new VFXAction(m, new InflameEffect(m), 0.25F));
                        m.addToBot(new VFXAction(m, new InflameEffect(m), 0.25F));
                        m.addToBot(new VFXAction(m, new InflameEffect(m), 0.25F));
                        m.addToBot(new RemoveDebuffsAction(m));
                        m.addToBot(new RemoveSpecificPowerAction(m, m, GainStrengthPower.POWER_ID));
                        m.addToBot(new ApplyPowerAction(m, m, new PlatedArmorPower(m, 7)));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Wound(), 3, true, true));
                        break;
                    case buff:
                        e.setBool("firstTurn", false);
                        m.addToBot(new ApplyPowerAction(m, m, new MetallicizePower(m, 7)));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                        break;
                    case heal:
                        m.addToBot(new HealAction(m, m, 10));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 3)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(buff, AbstractMonster.Intent.BUFF);
                    return true;
                }
                int turnCount = e.getInt("turnCount");
                if (turnCount == 4) {
                    e.setInt("turnCount", turnCount);
                    m.setMove(megabuff, AbstractMonster.Intent.BUFF);
                    return true;
                }
                if (!e.getBool("megabuff")) {
                    if (lastMove(m, buff)) {
                        if (roll < 50) {
                            m.setMove(Champ.MOVES[2], weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                        } else {
                            m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                        }
                    } else if (lastMove(m, weaken_attack)) {
                        m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                    } else {
                        m.setMove(Champ.MOVES[2], weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                    }
                } else {
                    if (lastMove(m, megabuff) || turnCount % 3 == 0) {
                        m.setMove(Champ.MOVES[1], execution, AbstractMonster.Intent.ATTACK, m.damage.get(execution).base, exeCount, 
                                true);
                        return true;
                    } else if (lastMove(m, weaken_attack)) {
                        m.setMove(heal, AbstractMonster.Intent.BUFF);
                    } else if (lastMove(m, heal)) {
                        m.setMove(Champ.MOVES[2], weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base);
                    } else {
                        King.Log("[" + m.name + "] rolled a strange move");
                        m.setMove(Champ.MOVES[1], execution, AbstractMonster.Intent.ATTACK, m.damage.get(execution).base, exeCount,
                                true);
                    }
                }
                return true;
            };
        }
    }
    // Beyond
    @SEMonsterEditor(m = Darkling.class)
    public static class DarklingSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 70);
                m.damage.get(0).base = 8;
                m.damage.get(0).output = 8;
                m.damage.get(1).base = 17;
                m.damage.get(1).output = 17;
                m.addPower(new BufferPower(m, 1));
                m.addPower(new TimeMazePower(m, 15));
            };
            e.takeTurn = m -> {
                if (m.nextMove == 2) {
                    m.addToBot(new GainBlockAction(m, m, 20));
                    m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 4)));
                    m.addToBot(new RollMoveAction(m));
                    return true;
                }
                return false;
            };
        }
        @SpirePatch2(clz = Darkling.class, method = "damage")
        public static class DamagePatch {
            @SpireInstrumentPatch
            public static ExprEditor Instrument() {
                return new ExprEditor(){
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if ("clear".equals(m.getMethodName())) {
                            m.replace("{if(" + DamagePatch.class.getName() + ".ClearPowers(this)){$_=$proceed($$);}}");
                        }
                    }
                };
            }
            public static boolean ClearPowers(Darkling m) {
                return !GetEditor(m).canModify();
            }
        }
    }
    @SEMonsterEditor(m = OrbWalker.class)
    public static class OrbWalkerSE {
        private static final byte unique = 0;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 120);
                m.damage.clear();
                m.damage.add(unique, new DamageInfo(m, 17));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new IntangiblePlayerPower(m, 1));
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case unique:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), FIRE));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Burn(), 2, true, true));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
              m.setMove(unique, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(0).base);
              return true;
            };
        }
    }
    @SEMonsterEditor(m = Spiker.class)
    public static class SpikerSE {
        private static final byte unique = 0;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 70);
                m.damage.get(0).base = 10;
                m.damage.get(0).output = 10;
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new RegenerateMonsterPower(m, 5));
                m.addPower(new ThornsPower(m, 6));
            };
            e.preBattle = m -> true;
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                if (m.nextMove == 2) {
                    int thornsCount = getField(Spiker.class, m, "thornsCount");
                    thornsCount++;
                    setField(Spiker.class, m, "thornsCount", thornsCount);
                    m.addToBot(new ApplyPowerAction(m, m, new ThornsPower(m, 3)));
                    m.addToBot(new RollMoveAction(m));
                    return true;
                }
                if (e.getBool("firstTurn") && m.nextMove == 1) {
                    e.setBool("firstTurn", false);
                }
                return false;
            };
            e.getMove = (m, roll) -> {
                int thornsCount = getField(Spiker.class, m, "thornsCount");
                if (e.getBool("firstTurn") || roll < 35 || thornsCount > 7) {
                    m.setMove((byte) 1, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                } else {
                    m.setMove((byte) 2, AbstractMonster.Intent.BUFF);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Exploder.class)
    public static class ExploderSE {
        private static final byte attack = 0;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 50);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 20));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new ExplosivePower(m, 5){
                    @Override
                    public void updateDescription() {
                        if (this.amount == 1) {
                            this.description = DESCRIPTIONS[3] + 50 + DESCRIPTIONS[2];
                        } else {
                            this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1] + 50 + DESCRIPTIONS[2];
                        }
                    }
    
                    @Override
                    public void duringTurn() {
                        if (this.amount == 1 && !this.owner.isDying) {
                            addToBot(new VFXAction(new ExplosionSmallEffect(this.owner.hb.cX, this.owner.hb.cY), 0.1F));
                            addToBot(new SuicideAction((AbstractMonster)this.owner));
                            addToBot(new DamageAction(LMSK.Player(), new DamageInfo(this.owner, 50, DamageInfo.DamageType.THORNS), 
                                    FIRE, true));
                        } else {
                            addToBot(new ReducePowerAction(this.owner, this.owner, this, 1));
                            this.updateDescription();
                        }
                    }
                });
                m.addPower(new TimeWarpPower(m){
                    @Override
                    public void updateDescription() {
                        this.description = DESC[0] + 12 + DESC[1].replace(TimeEater.NAME, this.owner.name) + 2 + DESC[2];
                    }
        
                    @Override
                    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
                        flashWithoutSound();
                        this.amount++;
                        if (this.amount == 12) {
                            this.amount = 0;
                            this.playApplyPowerSfx();
                            AbstractDungeon.actionManager.callEndTurnEarlySequence();
                            CardCrawlGame.sound.play("POWER_TIME_WARP", 0.05F);
                            AbstractDungeon.effectsQueue.add(new BorderFlashEffect(Color.GOLD, true));
                            AbstractDungeon.topLevelEffectsQueue.add(new TimeWarpTurnEndEffect());
                            addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, 2)));
                        }
                        this.updateDescription();
                    }
                });
            };
            e.preBattle = m -> true;
            e.putInt("turnCount", 1);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                turnCount++;
                e.setInt("turnCount", turnCount);
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), FIRE));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                int turnCount = e.getInt("turnCount");
                if (turnCount >= 5) {
                    m.setMove((byte) 2, AbstractMonster.Intent.UNKNOWN);
                } else {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Repulsor.class)
    public static class RepulsorSE {
        private static final byte attack = 0;
        private static final byte debuff = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 55);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 10));
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new PlatedArmorPower(m, 10));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 10));
                return true;
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        e.setBool("firstTurn", false);
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), SLASH_HORIZONTAL));
                        break;
                    case debuff:
                        m.addToBot(new MakeTempCardInDrawPileAction(new VoidCard(), 1, false, true));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Dazed(), 1, false, true));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn") || roll < 20) {
                    if (!lastTwoMoves(m, attack)) {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                    } else {
                        m.setMove(debuff, AbstractMonster.Intent.DEBUFF);
                    }
                } else if (!lastTwoMoves(m, debuff)) {
                    m.setMove(debuff, AbstractMonster.Intent.DEBUFF);
                } else {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(0).base);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Maw.class)
    public static class MawSE {
        private static final byte debuff = -1;
        private static final byte attack = 0;
        private static final byte multi = 1;
        private static final byte buff = 2;
        private static final int multiCount = 5;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 330);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 30));
                m.damage.add(multi, new DamageInfo(m, 4));
                m.addPower(new MawAngerPower(m, 2));
                m.addPower(new ArtifactPower(m, 1));
            };
            e.putBool("firstTurn", true);
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 20));
                return true;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case debuff:
                        e.setBool("firstTurn", false);
                        m.addToBot(new SFXAction("MAW_DEATH", 0.1F));
                        m.addToBot(new ShoutAction(m, Maw.DIALOG[0], 1F, 2F));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 5, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 5, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new StrengthPower(LMSK.Player(), -2)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new DexterityPower(LMSK.Player(), -2)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FocusPower(LMSK.Player(), -2)));
                        break;
                    case attack:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), BLUNT_HEAVY));
                        break;
                    case multi:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F), 
                                    LMSK.Player().hb.cY + MathUtils.random(-50F, 50F), Color.SKY.cpy())));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(multi), NONE));
                        }
                        break;
                    case buff:
                        m.addToBot(new ApplyPowerAction(m, m, new IntangiblePower(m, 1){
                            @Override
                            public void updateDescription() {
                                description = IntangiblePlayerPower.DESCRIPTIONS[0];
                            }
                        }));
                        m.addToBot(new ApplyPowerAction(m, m, new BufferPower(m, 2)));
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 5)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(debuff, AbstractMonster.Intent.STRONG_DEBUFF);
                } else {
                    if (roll < 20) {
                        m.setMove(buff, AbstractMonster.Intent.BUFF);
                    } else if (roll < 60) {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base);
                    } else {
                        m.setMove(multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SpireGrowth.class)
    public static class SpireGrowthSE {
        private static final byte attack = 0;
        private static final byte debuff = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 210);
                m.powers.add(new ArtifactPower(m, 2));
                m.powers.add(new InvinciblePower(m, 60));
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(1), BLUNT_HEAVY));
                        break;
                    case debuff:
                        m.addToBot(new AnimateSlowAttackAction(m));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new ConstrictedPower(LMSK.Player(), m, 10)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (lastMove(m, debuff)) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(1).base);
                } else {
                    m.setMove(debuff, AbstractMonster.Intent.STRONG_DEBUFF);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = WrithingMass.class)
    public static class WrithingMassSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 200);
            };
            e.putBool("firstTurn", true);
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new IntangiblePlayerPower(m, 1)));
                m.addToBot(new ApplyPowerAction(m, m, new RegenerateMonsterPower(m, 10)));
                m.addToBot(new GainBlockAction(m, m, 100));
                m.addToBot(new ApplyPowerAction(m, m, new MalleablePower(m, 2)));
                return false;
            };
            e.takeTurn = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 1)));
                m.addToBot(new ApplyPowerAction(m, m, new PlatedArmorPower(m, 5)));
                return false;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    e.setBool("firstTurn", false);
                    setField(WrithingMass.class, m, "usedMegaDebuff", false);
                    m.setMove((byte) 4, AbstractMonster.Intent.STRONG_DEBUFF);
                    return true;
                }
                return false;
            };
        }
    }
    @SEMonsterEditor(m = Transient.class)
    public static class TransientSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 999);
                m.powers.add(new ArtifactPower(m, 2));
            };
            e.preBattle = m -> {
                m.addToBot(new ApplyPowerAction(m, m, new ShiftingPower(m)));
                m.addToBot(new ApplyPowerAction(m, m, new MalleablePower(m, 5)));
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Reptomancer.class)
    public static class ReptomancerSE {
        private static final byte stunned = -1;
        private static final byte weaken_attack = 0;
        private static final byte pure_attack = 1;
        private static final byte summon = 2;
        private static final int weakenCount = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.putObj("lastMoveInfo", null);
            e.putObj("daggerList", new ArrayList<SnakeDagger>());
            e.initFunc = m -> {
                setMonsterHp(m, 200);
                m.damage.clear();
                m.damage.add(weaken_attack, new DamageInfo(m, 16));
                m.damage.add(pure_attack, new DamageInfo(m, 30));
                m.addPower(new FlightPower(m, 5){
                    @Override
                    public void onRemove() {
                        if (owner instanceof Reptomancer) {
                            EnemyMoveInfo moveInfo = getField(AbstractMonster.class, owner, "move");
                            e.setObj("lastMoveInfo", new EnemyMoveInfo(moveInfo.nextMove, moveInfo.intent, moveInfo.baseDamage, 
                                    moveInfo.multiplier, moveInfo.isMultiDamage));
                            King.Log("Storing [" + owner.name + "] next move: " + moveInfo.nextMove + ", " + moveInfo.intent);
                            ((Reptomancer) owner).setMove(stunned, AbstractMonster.Intent.STUN);
                            ((Reptomancer) owner).createIntent();
                        }
                    }
                });
            };
            e.preBattle = m -> {
                ArrayList<SnakeDagger> daggerList = e.getObj("daggerList");
                List<AbstractMonster> monsters = LMSK.GetAllExptMstr(mo -> mo instanceof SnakeDagger);
                for (AbstractMonster mo : monsters) {
                    m.addToBot(new ApplyPowerAction(mo, m, new MinionPower(mo)));
                    daggerList.add((SnakeDagger) mo);
                }
                return true;
            };
            e.takeTurn = m -> {
                if (!m.hasPower(FlightPower.POWER_ID)) {
                    m.addToBot(new ApplyPowerAction(m, m, new FlightPower(m, 5){
                        @Override
                        public void onRemove() {
                            if (owner instanceof Reptomancer) {
                                EnemyMoveInfo moveInfo = getField(AbstractMonster.class, owner, "move");
                                e.setObj("lastMoveInfo", new EnemyMoveInfo(moveInfo.nextMove, moveInfo.intent, moveInfo.baseDamage,
                                        moveInfo.multiplier, moveInfo.isMultiDamage));
                                King.Log("Storing [" + owner.name + "] next move: " + moveInfo.nextMove + ", " + moveInfo.intent);
                                ((Reptomancer) owner).setMove(stunned, AbstractMonster.Intent.STUN);
                                ((Reptomancer) owner).createIntent();
                            }
                        }
                    }));
                }
                m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 1)));
                switch (m.nextMove) {
                    case weaken_attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F) * Settings.scale, 
                                LMSK.Player().hb.cY + MathUtils.random(-50F, 50F) * Settings.scale, Color.ORANGE.cpy()), 0.1F));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(0), NONE));
                        m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F) * Settings.scale,
                                LMSK.Player().hb.cY + MathUtils.random(-50F, 50F) * Settings.scale, Color.ORANGE.cpy()), 0.1F));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(weaken_attack), NONE));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), weakenCount, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), weakenCount, true)));
                        break;
                    case pure_attack:
                        m.addToBot(new AnimateFastAttackAction(m));
                        m.addToBot(new VFXAction(new BiteEffect(LMSK.Player().hb.cX + MathUtils.random(-50F, 50F) * Settings.scale,
                                LMSK.Player().hb.cY + MathUtils.random(-50F, 50F) * Settings.scale, Color.CHARTREUSE.cpy()), 0.1F));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(pure_attack), NONE));
                        break;
                    case summon:
                        int daggersPerSpawn = getField(Reptomancer.class, m, "daggersPerSpawn");
                        m.addToBot(new ChangeStateAction(m, "SUMMON"));
                        ArrayList<SnakeDagger> daggerList = e.getObj("daggerList");
                        int newSize = daggerList.size() + daggersPerSpawn;
                        int oldSize = Reptomancer.POSX.length;
                        float[][] pos = new float[Math.max(newSize, oldSize)][2];
                        for (int i = 0; i < pos.length; i++) {
                            if (i < oldSize) {
                                pos[i] = new float[]{Reptomancer.POSX[i], Reptomancer.POSY[i]};
                            }
                            else {
                                float x = 360F + (i - oldSize) * MathUtils.random(120F, 140F);
                                float y = 75F + (i % 2 == 0 ? 0 : MathUtils.random(35F, 45F));
                                pos[i] = new float[]{x, y};
                            }
                        }
                        for (int i = daggerList.size(); i < newSize; i++) {
                            SnakeDagger dagger = new SnakeDagger(pos[i][0], pos[i][1]);
                            daggerList.add(dagger);
                            m.addToBot(new SpawnMonsterAction(dagger, true));
                        }
                        break;
                    case stunned:
                        EnemyMoveInfo moveInfo = e.getObj("lastMoveInfo");
                        if (moveInfo != null) {
                            m.setMove(moveInfo.nextMove, moveInfo.intent, moveInfo.baseDamage, moveInfo.multiplier, moveInfo.isMultiDamage);
                            King.Log("reseting [" + m.name + "] next move: " + moveInfo.nextMove + ", " + moveInfo.intent);
                            return true;
                        } else {
                            King.Log("[" + m.name + "] has no stored move info");
                        }
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                ArrayList<SnakeDagger> daggerList = e.getObj("daggerList");
                daggerList.removeIf(AbstractCreature::isDeadOrEscaped);
                int livings = daggerList.size();
                if (livings < 4) {
                    if (roll < 30 && !lastMove(m, summon)) {
                        m.setMove(summon, AbstractMonster.Intent.UNKNOWN);
                    } else if (AbstractDungeon.aiRng.randomBoolean(0.5F) && !lastTwoMoves(m, weaken_attack)) {
                        m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base, weakenCount, true);
                    } else {
                        if (!lastTwoMoves(m, pure_attack)) {
                            m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                        } else {
                            m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base, weakenCount, true);
                        }
                    }
                } else if (roll < 50 && !lastTwoMoves(m, weaken_attack)) {
                    m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base, weakenCount, true);
                } else {
                    if (!lastTwoMoves(m, pure_attack)) {
                        m.setMove(pure_attack, AbstractMonster.Intent.ATTACK, m.damage.get(pure_attack).base);
                    } else {
                        m.setMove(weaken_attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(weaken_attack).base, weakenCount, true);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = SnakeDagger.class)
    public static class SnakeDaggerSE {
        private static final byte attack = 0;
        private static final byte suicide = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 25);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 19));
                m.addPower(new FlightPower(m, 3){
                    @Override
                    public void atStartOfTurn() {}
                });
            };
            e.putInt("turnCount", 0);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                turnCount++;
                e.setInt("turnCount", turnCount);
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_HORIZONTAL));
                        m.addToBot(new MakeTempCardInDiscardAction(new Wound(), 1));
                        break;
                    case suicide:
                        m.addToBot(new SuicideAction(m));
                        for (AbstractMonster mo : LMSK.GetAllExptMstr(mo -> mo instanceof Reptomancer)) {
                            m.addToBot(new HealAction(mo, m, 5));
                        }
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                int turnCount = e.getInt("turnCount");
                if (turnCount < 2) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                } else {
                    m.setMove(suicide, AbstractMonster.Intent.UNKNOWN);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = Nemesis.class)
    public static class NemesisSE {
        private static final byte scythe = 0;
        private static final byte burn = 1;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 200);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 45));
                m.addPower(new IntangiblePlayerPower(m, 1));
                m.addPower(new BufferPower(m, 3));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, 20));
                return false;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case scythe:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(scythe), SLASH_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 1, true)));
                        break;
                    case burn:
                        m.addToBot(new SFXAction("VO_NEMESIS_1C"));
                        m.addToBot(new VFXAction(new ShockWaveEffect(m.hb.cX, m.hb.cY, Settings.GREEN_TEXT_COLOR.cpy(), 
                                ShockWaveEffect.ShockWaveType.CHAOTIC), 1.5F));
                        Burn burn = new Burn();
                        burn.upgrade();
                        m.addToBot(new MakeTempCardInDiscardAction(burn, 3));
                        m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 2));
                        break;
                }
                if (!m.hasPower(IntangiblePower.POWER_ID) && !m.hasPower(IntangiblePlayerPower.POWER_ID)) {
                    m.addToBot(new ApplyPowerAction(m, m, new IntangiblePower(m, 1)));
                    m.addToBot(new ApplyPowerAction(m, m, new BufferPower(m, 3)));
                    m.addToBot(new GainBlockAction(m, 20));
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (lastMove(m, burn) || lastMove(m, scythe)) {
                    if (!lastTwoMoves(m, scythe)) {
                        m.setMove(scythe, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(scythe).base);
                    } else {
                        m.setMove(burn, AbstractMonster.Intent.STRONG_DEBUFF);
                    }
                } else {
                    m.setMove(burn, AbstractMonster.Intent.STRONG_DEBUFF);
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = GiantHead.class)
    public static class GiantHeadSE {
        private static final byte scythe = 0;
        private static final byte burn = 1;
        private static final byte summon = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 550);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 50));
                m.addPower(new ThornsPower(m, 3));
                m.addPower(new TimeWarpPower(m){
                    @Override
                    public void updateDescription() {
                        this.description = DESC[0] + 12 + DESC[1].replace(TimeEater.NAME, this.owner.name) + 2 + DESC[2];
                    }
    
                    @Override
                    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
                        flashWithoutSound();
                        this.amount++;
                        if (this.amount == 12) {
                            this.amount = 0;
                            this.playApplyPowerSfx();
                            AbstractDungeon.actionManager.callEndTurnEarlySequence();
                            CardCrawlGame.sound.play("POWER_TIME_WARP", 0.05F);
                            AbstractDungeon.effectsQueue.add(new BorderFlashEffect(Color.GOLD, true));
                            AbstractDungeon.topLevelEffectsQueue.add(new TimeWarpTurnEndEffect());
                            addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, 2)));
                        }
                        this.updateDescription();
                    }
                });
            };
            e.putBool("firstTurn", true);
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case scythe:
                        getMethod(GiantHead.class, "playSfx", new Class[0]).invoke(m);
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(scythe), SMASH));
                        break;
                    case burn:
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 5)));
                        m.addToBot(new GainBlockAction(m, 10));
                        break;
                    case summon:
                        e.setBool("firstTurn", false);
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 5, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 5, true)));
                        FungiBeast fungi2 = new FungiBeast(-450F, MathUtils.random(20F, 35F));
                        FungiBeast fungi1 = new FungiBeast(-220F, 345F + MathUtils.random(20F, 35F));
                        fungi1.usePreBattleAction();
                        fungi2.usePreBattleAction();
                        m.addToBot(new SpawnMonsterAction(fungi1, false));
                        m.addToBot(new SpawnMonsterAction(fungi2, false));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(summon, AbstractMonster.Intent.UNKNOWN);
                } else {
                    if (roll < 50 && !lastTwoMoves(m, scythe)) {
                        m.setMove(scythe, AbstractMonster.Intent.ATTACK, m.damage.get(scythe).base);
                    } else if (!lastMove(m, burn)) {
                        m.setMove(burn, AbstractMonster.Intent.BUFF);
                    } else {
                        m.setMove(scythe, AbstractMonster.Intent.ATTACK, m.damage.get(scythe).base);
                    }
                }
                return true;
            };
        }
    }
    @SEMonsterEditor(m = TimeEater.class)
    public static class TimeEaterSE {
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 500);
                m.damage.get(0).base = 7;
                m.damage.get(0).output = 7;
                m.addPower(new ArtifactPower(m, 1));
                m.addPower(new InvinciblePower(m, 100));
            };
            e.takeTurn = m -> {
                if (m.nextMove == 5) {
                    m.addToBot(new ShoutAction(m, TimeEater.DIALOG[1], 0.5F, 2F));
                    m.addToBot(new RemoveDebuffsAction(m));
                    m.addToBot(new RemoveSpecificPowerAction(m, m, GainStrengthPower.POWER_ID));
                    m.addToBot(new RemoveSpecificPowerAction(m, m, InvinciblePower.POWER_ID));
                    m.addToBot(new HealAction(m, m, m.maxHealth));
                    m.addToBot(new GainBlockAction(m, m, 40));
                    m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 5, true)));
                    m.addToBot(new MakeTempCardInDiscardAction(new Slimed(), 5));
                    m.addToBot(new RollMoveAction(m));
                    return true;
                }
                return false;
            };
        }
    }
    @SEMonsterEditor(m = AwakenedOne.class)
    public static class AwakenedOneSE {
        private static final byte reborn = -1;
        private static final byte attack = 0;
        private static final byte multi = 1;
        private static final byte buff = 2;
        private static final int multiCount = 5;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 250);
                m.addPower(new RegenerateMonsterPower(m, 5));
                m.addPower(new MetallicizePower(m, 5));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 10));
                return false;
            };
            e.putBool("form3", false);
            e.takeTurn = m -> {
                if (e.getBool("form3")) {
                    m.addToBot(new ApplyPowerAction(m, m, new BufferPower(m, 1)));
                    switch (m.nextMove) {
                        case reborn:
                            m.addToBot(new SFXAction("VO_AWAKENEDONE_1"));
                            m.addToBot(new VFXAction(m, new IntenseZoomEffect(m.hb.cX, m.hb.cY, true), 0.5F, true));
                            m.addToBot(new ChangeStateAction(m, "REBIRTH"));
                            m.addToBot(new ApplyPowerAction(m, m, new InvinciblePower(m, 200)));
                            break;
                        case attack:
                            m.addToBot(new ChangeStateAction(m, "ATTACK_2"));
                            m.addToBot(new SFXAction("MONSTER_AWAKENED_ATTACK"));
                            m.addToBot(new VFXAction(new ShockWaveEffect(m.hb.cX, m.hb.cY, new Color(0.1F, 0F, 0.2F, 1F),
                                    ShockWaveEffect.ShockWaveType.CHAOTIC), 0.3F));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), FIRE));
                            m.addToBot(new MakeTempCardInDiscardAction(new VoidCard(), 1));
                            break;
                        case multi:
                            m.addToBot(new SFXAction("MONSTER_AWAKENED_ATTACK"));
                            for (int i = 0; i < multiCount; i++) {
                                m.addToBot(new VFXAction(new ShockWaveEffect(m.hb.cX, m.hb.cY, new Color(0.3F, 0.2F, 0.4F, 1F),
                                        ShockWaveEffect.ShockWaveType.CHAOTIC), 0.3F));
                                m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(multi), BLUNT_HEAVY));
                            }
                            break;
                        case buff:
                            m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 3)));
                            m.addToBot(new ApplyPowerAction(m, m, new MetallicizePower(m, 3)));
                            break;
                    }
                    m.addToBot(new RollMoveAction(m));
                    return true;
                }
                if (m.nextMove == 3) {
                    m.addToBot(new SFXAction("VO_AWAKENEDONE_1"));
                    m.addToBot(new VFXAction(m, new IntenseZoomEffect(m.hb.cX, m.hb.cY, true), 0.5F, true));
                    m.addToBot(new ChangeStateAction(m, "REBIRTH"));
                    m.addToBot(new ApplyPowerAction(m, m, new RegenerateMonsterPower(m, 10)));
                    m.addToBot(new RollMoveAction(m));
                    return true;
                }
                return false;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("form3")) {
                    if (roll < 20) {
                        if (!lastMove(m, buff)) {
                            m.setMove(buff, AbstractMonster.Intent.BUFF);
                        } else if (AbstractDungeon.aiRng.randomBoolean(0.5F)) {
                            m.setMove(AwakenedOne.MOVES[1], multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                        } else {
                            m.setMove(AwakenedOne.MOVES[0], attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                        }
                    } else if (roll < 60 && !lastTwoMoves(m, multi)) {
                        m.setMove(AwakenedOne.MOVES[1], multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                    } else if (!lastTwoMoves(m, attack)) {
                        m.setMove(AwakenedOne.MOVES[0], attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                    } else {
                        m.setMove(AwakenedOne.MOVES[1], multi, AbstractMonster.Intent.ATTACK, m.damage.get(multi).base, multiCount, true);
                    }
                    return true;
                }
                return false;
            };
        }
        @SpirePatch2(clz = AwakenedOne.class, method = "changeState")
        public static class ChangeStatePatch {
            @SpirePrefixPatch
            public static SpireReturn PrefixGet(AwakenedOne __instance, String key) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify() && "REBIRTH".equals(key)) {
                    if (e.getBool("form3")) {
                        __instance.maxHealth = 350;
                    } else {
                        __instance.maxHealth = 300;
                    }
                    if (Settings.isEndless && LMSK.Player().hasBlight(Shield.ID)) {
                        float mod = LMSK.Player().getBlight(Shield.ID).effectFloat();
                        __instance.maxHealth *= mod;
                    }
                    if (ModHelper.isModEnabled("MonsterHunter"))
                        __instance.currentHealth *= 1.5F;
                    __instance.state.setAnimation(0, "Idle_2", true);
                    __instance.halfDead = false;
                    setField(AwakenedOne.class, __instance, "animateParticles", true);
                    __instance.addToBot(new HealAction(__instance, __instance, __instance.maxHealth));
                    if (e.getBool("form3")) {
                        __instance.addToBot(new ApplyPowerAction(__instance, __instance, new RegenerateMonsterPower(__instance, 5)));
                        __instance.addToBot(new ApplyPowerAction(__instance, __instance, new ThornsPower(__instance, 3)));
                        __instance.addToBot(new CanLoseAction());
                    }
                    return SpireReturn.Return();
                }
                return SpireReturn.Continue();
            }
        }
        @SpirePatch2(clz = AwakenedOne.class, method = "damage")
        public static class DamagePatch {
            @SpireInsertPatch(locator = Locator.class)
            public static SpireReturn Insert(AwakenedOne __instance) {
                MonsterEditor e = GetEditor(__instance);
                if (e.canModify()) {
                    if (__instance.currentHealth <= 0 && !getBool(AwakenedOne.class, __instance, "form1")) {
                        if (!e.getBool("form3")) {
                            e.setBool("form3", true);
                            AbstractDungeon.getCurrRoom().cannotLose = true;
                            __instance.halfDead = true;
                            for (AbstractPower p : __instance.powers) {
                                p.onDeath();
                            }
                            for (AbstractRelic r : LMSK.Player().relics) {
                                r.onMonsterDeath(__instance);
                            }
                            __instance.addToTop(new ClearCardQueueAction());
                            __instance.powers.removeIf(p -> p.type == AbstractPower.PowerType.DEBUFF 
                                    || UnawakenedPower.POWER_ID.equals(p.ID));
                            __instance.damage.clear();
                            __instance.damage.add(attack, new DamageInfo(__instance, 40));
                            __instance.damage.add(multi, new DamageInfo(__instance, 5));
                            __instance.setMove(reborn, AbstractMonster.Intent.UNKNOWN);
                            __instance.createIntent();
                            __instance.applyPowers();
                            return SpireReturn.Return();
                        }
                    }
                }
                return SpireReturn.Continue();
            }
            private static class Locator extends SpireInsertLocator {
                @Override
                public int[] Locate(CtBehavior ctBehavior) throws Exception {
                    Matcher.FieldAccessMatcher matcher = new Matcher.FieldAccessMatcher(AwakenedOne.class, "currentHealth");
                    return LineFinder.findInOrder(ctBehavior, matcher);
                }
            }
        }
        @SpirePatch2(clz = AwakenedOne.class, method = "die")
        public static class DiePatch {
            @SpirePostfixPatch
            public static void Postfix() {
                if (!AbstractDungeon.getCurrRoom().cannotLose) {
                    for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                        if (!m.isDying && !(m instanceof Cultist)) {
                            m.addToBot(new EscapeAction(m));
                        }
                    }
                }
            }
        }
    }
    @SEMonsterEditor(m = Deca.class)
    public static class DecaSE {
        private static final byte regrowing = -2;
        private static final byte reborn = -1;
        private static final byte attack = 0;
        private static final byte buff = 1;
        private static final int multiCount = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 270);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 10));
                m.addPower(new InvinciblePower(m, 150));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                CardCrawlGame.music.unsilenceBGM();
                AbstractDungeon.scene.fadeOutAmbiance();
                AbstractDungeon.getCurrRoom().playBgmInstantly("BOSS_BEYOND");
                UnlockTracker.markBossAsSeen("DONUT");
                AbstractDungeon.getCurrRoom().cannotLose = true;
                m.addToBot(new ApplyPowerAction(m, m, new RegrowPower(m)));
                m.addToBot(new ApplyPowerAction(m, m, new ArtifactPower(m, 2)));
                return true;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case regrowing:
                        m.addToBot(new TextAboveCreatureAction(m, Darkling.DIALOG[0]));
                        m.setMove(reborn, AbstractMonster.Intent.BUFF);
                        return true;
                    case reborn:
                        m.halfDead = false;
                        m.addToBot(new HealAction(m, m, m.maxHealth / 2));
                        if (lastMoveBefore(m, buff)) {
                            m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base, multiCount, true);
                        } else {
                            m.setMove(buff, AbstractMonster.Intent.DEFEND_BUFF);
                        }
                        return true;
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), FIRE));
                        }
                        m.addToBot(new MakeTempCardInDiscardAction(new Dazed(), 3));
                        break;
                    case buff:
                        for (AbstractMonster mo : LMSK.GetAllExptMonsters(mo -> !mo.isDeadOrEscaped() && !mo.halfDead)) {
                            m.addToBot(new GainBlockAction(mo, m, 20));
                            m.addToBot(new ApplyPowerAction(mo, m, new PlatedArmorPower(mo, 5)));
                        }
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (lastMove(m, attack)) {
                    m.setMove(buff, AbstractMonster.Intent.DEFEND_BUFF);
                } else {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base, multiCount, true);
                }
                return true;
            };
        }
        @SpirePatch2(clz = Deca.class, method = "damage")
        public static class DamagePatch {
            public static void Postfix(Deca __instance, DamageInfo info) {
                MonsterEditor e = GetEditor(__instance);
                if (__instance.currentHealth <= 0 && e.canModify()) {
                    __instance.halfDead = true;
                    for (AbstractPower p : __instance.powers) {
                        p.onDeath();
                    }
                    for (AbstractRelic r : LMSK.Player().relics) {
                        r.onMonsterDeath(__instance);
                    }
                    List<AbstractMonster> monsterList = AbstractDungeon.getMonsters().monsters;
                    boolean allDead = true;
                    for (AbstractMonster m : monsterList) {
                        if (Donu.ID.equals(m.id) && !m.halfDead) {
                            allDead = false;
                            break;
                        }
                    }
                    if (!allDead) {
                        if (__instance.nextMove != regrowing) {
                            __instance.setMove(regrowing, AbstractMonster.Intent.UNKNOWN);
                            __instance.createIntent();
                        }
                    } else {
                        AbstractDungeon.getCurrRoom().cannotLose = false;
                        __instance.halfDead = false;
                        for (AbstractMonster m : monsterList) {
                            m.die();
                        }
                    }
                }
            }
        }
        @SpirePatches2({
                @SpirePatch2(clz = Deca.class, method = "die"),
                @SpirePatch2(clz = Donu.class, method = "die"),
        })
        public static class DiePatch {
            @SpirePrefixPatch
            public static SpireReturn PrefixGet(AbstractMonster __instance) {
                if (AbstractDungeon.getCurrRoom().cannotLose)
                    return SpireReturn.Return();
                return SpireReturn.Continue();
            }
        }
    }
    @SEMonsterEditor(m = Donu.class)
    public static class DonuSE {
        private static final byte regrowing = -2;
        private static final byte reborn = -1;
        private static final byte attack = 0;
        private static final byte buff = 1;
        private static final int multiCount = 2;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 270);
                m.damage.clear();
                m.damage.add(new DamageInfo(m, 10));
                m.addPower(new InvinciblePower(m, 150));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                AbstractDungeon.getCurrRoom().cannotLose = true;
                m.addToBot(new ApplyPowerAction(m, m, new RegrowPower(m)));
                m.addToBot(new ApplyPowerAction(m, m, new ArtifactPower(m, 2)));
                return true;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case regrowing:
                        m.addToBot(new TextAboveCreatureAction(m, Darkling.DIALOG[0]));
                        m.setMove(reborn, AbstractMonster.Intent.BUFF);
                        return true;
                    case reborn:
                        m.halfDead = false;
                        m.addToBot(new HealAction(m, m, m.maxHealth / 2));
                        if (lastMoveBefore(m, buff)) {
                            m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                        } else {
                            m.setMove(buff, AbstractMonster.Intent.DEBUFF);
                        }
                        return true;
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), FIRE));
                        }
                        m.addToBot(new MakeTempCardInDiscardAction(new Dazed(), 3));
                        break;
                    case buff:
                        for (AbstractMonster mo : LMSK.GetAllExptMonsters(mo -> !mo.isDeadOrEscaped() && !mo.halfDead)) {
                            m.addToBot(new ApplyPowerAction(mo, m, new StrengthPower(mo, 4)));
                        }
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 5, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new StrengthPower(LMSK.Player(), -1)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new DexterityPower(LMSK.Player(), -1)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FocusPower(LMSK.Player(), -1)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (lastMove(m, buff)) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                } else {
                    m.setMove(buff, AbstractMonster.Intent.DEBUFF);
                }
                return true;
            };
        }
        @SpirePatch2(clz = Donu.class, method = "damage")
        public static class DamagePatch {
            public static void Postfix(Donu __instance, DamageInfo info) {
                MonsterEditor e = GetEditor(__instance);
                if (__instance.currentHealth <= 0 && e.canModify()) {
                    __instance.halfDead = true;
                    for (AbstractPower p : __instance.powers) {
                        p.onDeath();
                    }
                    for (AbstractRelic r : LMSK.Player().relics) {
                        r.onMonsterDeath(__instance);
                    }
                    List<AbstractMonster> monsterList = AbstractDungeon.getMonsters().monsters;
                    boolean allDead = true;
                    for (AbstractMonster m : monsterList) {
                        if (Deca.ID.equals(m.id) && !m.halfDead) {
                            allDead = false;
                            break;
                        }
                    }
                    if (!allDead) {
                        if (__instance.nextMove != regrowing) {
                            __instance.setMove(regrowing, AbstractMonster.Intent.UNKNOWN);
                            __instance.createIntent();
                        }
                    } else {
                        AbstractDungeon.getCurrRoom().cannotLose = false;
                        __instance.halfDead = false;
                        for (AbstractMonster m : monsterList) {
                            m.die();
                        }
                    }
                }
            }
        }
    }
    // Ending
    @SEMonsterEditor(m = SpireSpear.class, hasExtraFunctions = true)
    public static class SpireSpearSE {
        private static final byte first_atk = 0;
        private static final byte attack = 1;
        private static final byte buff = 3;
        private static final byte alone_2 = 4;
        private static final byte count = 3;
        private static final int multiCount = 6;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 220);
                m.damage.clear();
                m.damage.add(first_atk, new DamageInfo(m, 5));
                m.damage.add(attack, new DamageInfo(m, 5));
                m.addPower(new BufferPower(m, 3));
            };
            e.putBool("firstTurn", true);
            e.putBool("aloneBuff", false);
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 20));
                King.ShieldAndSpearExisting = true;
                return false;
            };
            e.takeTurn = m -> {
                switch (m.nextMove) {
                    case first_atk:
                        e.setBool("firstTurn", false);
                        for (byte i = 0; i < count; i++) {
                            m.addToBot(new ChangeStateAction(m, "ATTACK"));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(first_atk), FIRE));
                        }
                        m.addToBot(new MakeTempCardInDrawPileAction(new Burn(), count, true, true));
                        break;
                    case attack:
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new ChangeStateAction(m, "ATTACK"));
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), SLASH_DIAGONAL, true));
                        }
                        break;
                    case buff:
                        for (AbstractMonster mo : LMSK.GetAllExptMonsters(mo -> !mo.isDeadOrEscaped())) {
                            m.addToBot(new ApplyPowerAction(mo, m, new StrengthPower(mo, 3)));
                        }
                        break;
                    case alone_2:
                        m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 2)));
                        m.addToBot(new ApplyPowerAction(m, m, new BufferPower(m, 3)));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("aloneBuff")) {
                    if (lastMove(m, attack)) {
                        m.setMove(alone_2, AbstractMonster.Intent.BUFF);
                    } else {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                    }
                    return true;
                }
                if (e.getBool("firstTurn")) {
                    m.setMove(first_atk, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(first_atk).base, count, true);
                } else {
                    if (lastMove(m, attack)) {
                        m.setMove(buff, AbstractMonster.Intent.BUFF);
                    } else {
                        m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                    }
                }
                return true;
            };
            e.postUpdate = m -> {
                if (!e.getBool("aloneBuff") && alone(m)) {
                    e.setBool("aloneBuff", true);
                    m.addToTop(new ApplyPowerAction(m, m, new StrengthPower(m, 5)));
                    m.addToTop(new ApplyPowerAction(m, m, new BufferPower(m, 10)));
                    m.setMove(attack, AbstractMonster.Intent.ATTACK, m.damage.get(attack).base, multiCount, true);
                    m.createIntent();
                    m.applyPowers();
                }
            };
        }
        private static boolean alone(AbstractMonster self) {
            return LMSK.GetAllExptMstr(m -> m != self).size() <= 0;
        }
    }
    @SEMonsterEditor(m = SpireShield.class, hasExtraFunctions = true)
    public static class SpireShieldSE {
        private static final byte attack = 0;
        private static final byte smash = 1;
        private static final byte buff = 3;
        private static final byte alone_1 = 2;
        private static final byte alone_2 = 4;
        private static final int multiCount = 6;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 150);
                m.damage.clear();
                m.damage.add(attack, new DamageInfo(m, 15));
                m.damage.add(smash, new DamageInfo(m, 40));
                m.damage.add(alone_1, new DamageInfo(m, 0));
                m.addPower(new MetallicizePower(m, 10));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                m.addToBot(new GainBlockAction(m, m, 50));
                King.ShieldAndSpearExisting = true;
                return false;
            };
            e.putInt("turnCount", 0);
            e.putBool("aloneBuff", false);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                turnCount++;
                switch (m.nextMove) {
                    case attack:
                        m.addToBot(new ChangeStateAction(m, "ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(attack), BLUNT_HEAVY));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new StrengthPower(LMSK.Player(), -1)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new DexterityPower(LMSK.Player(), -1)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FocusPower(LMSK.Player(), -1)));
                        if (turnCount % 2 == 0) {
                            m.addToBot(new SetMoveAction(m, smash, AbstractMonster.Intent.ATTACK_DEFEND, m.damage.get(smash).base));
                            turnCount = 0;
                        } else {
                            m.addToBot(new SetMoveAction(m, buff, AbstractMonster.Intent.DEFEND));
                        }
                        e.setInt("turnCount", turnCount);
                        return true;
                    case buff:
                        for (AbstractMonster mo : LMSK.GetAllExptMonsters(mo -> !mo.isDeadOrEscaped())) {
                            m.addToBot(new GainBlockAction(mo, m, 30));
                        }
                        if (turnCount % 3 == 0) {
                            m.addToBot(new SetMoveAction(m, smash, AbstractMonster.Intent.ATTACK_DEFEND, m.damage.get(smash).base));
                            turnCount = 0;
                        } else {
                            m.addToBot(new SetMoveAction(m, attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base));
                        }
                        e.setInt("turnCount", turnCount);
                        return true;
                    case smash:
                        e.setInt("turnCount", turnCount);
                        m.addToBot(new ChangeStateAction(m, "OLD_ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(smash), BLUNT_HEAVY, true));
                        m.addToBot(new GainBlockAction(m, m, 50));
                        m.addToBot(new RollMoveAction(m));
                        return true;
                    case alone_1:
                        m.addToBot(new ChangeStateAction(m, "OLD_ATTACK"));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(alone_1), BLUNT_HEAVY, true));
                        m.addToBot(new RollMoveAction(m));
                        return true;
                    case alone_2:
                        m.addToBot(new GainBlockAction(m, 50));
                        m.addToBot(new RollMoveAction(m));
                        return true;
                }
                return false;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("aloneBuff")) {
                    if (lastMove(m, alone_1)) {
                        m.setMove(alone_2, AbstractMonster.Intent.DEFEND);
                    } else {
                        m.damage.get(alone_1).base = m.currentBlock + 10;
                        m.setMove(alone_1, AbstractMonster.Intent.ATTACK, m.damage.get(alone_1).base);
                        m.applyPowers();
                    }
                    return true;
                }
                if (roll < 50) {
                    m.setMove(attack, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(attack).base);
                } else {
                    m.setMove(buff, AbstractMonster.Intent.DEFEND);
                }
                return true;
            };
            e.postUpdate = m -> {
                if (!e.getBool("aloneBuff") && alone(m)) {
                    e.setBool("aloneBuff", true);
                    m.addToTop(new QuickAction(() -> {
                        m.damage.get(alone_1).base = m.currentBlock + 10;
                        m.setMove(alone_1, AbstractMonster.Intent.ATTACK, m.damage.get(alone_1).base);
                        m.createIntent();
                        m.applyPowers();
                    }));
                    m.addToTop(new QuickAction(() -> m.increaseMaxHp(100, true)));
                    m.addToTop(new GainBlockAction(m, 100));
                }
            };
        }
        private static boolean alone(AbstractMonster self) {
            return LMSK.GetAllExptMstr(m -> m != self).size() <= 0;
        }
        @SpirePatch2(clz = SpireShield.class, method = "takeTurn")
        public static class ShieldBlockPatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws CannotCompileException {
                CtClass clz = ctBehavior.getDeclaringClass();
                CtMethod ab = CtNewMethod.make(CtClass.voidType, "addBlock", new CtClass[]{CtClass.intType}, null, 
                        "{super.addBlock($1);" + ShieldBlockPatch.class.getName() + ".applyPowers($0);}", clz);
                CtMethod lb = CtNewMethod.make(CtClass.voidType, "loseBlock", new CtClass[]{CtClass.intType, CtClass.booleanType},
                        null, "{super.loseBlock($1,$2);" + ShieldBlockPatch.class.getName() + ".applyPowers($0);}", clz);
                clz.addMethod(ab);
                clz.addMethod(lb);
            }
            public static void applyPowers(AbstractMonster m) {
                MonsterEditor e = GetEditor(m);
                if (e.canModify() && e.getBool("aloneBuff") && m.nextMove == alone_1) {
                    m.damage.get(alone_1).base = m.currentBlock + 10;
                    m.setMove(alone_1, AbstractMonster.Intent.ATTACK, m.damage.get(alone_1).base);
                    m.applyPowers();
                    m.createIntent();
                }
            }
        }
    }
    @SEMonsterEditor(m = CorruptHeart.class)
    public static class CorruptHeartSE {
        private static final byte debuff = -1;
        private static final byte bloods = 0;
        private static final byte massive = 1;
        private static final int multiCount = 15;
        public static void Edit(AbstractMonster _inst) {
            MonsterEditor e = GetModifierEditor(_inst);
            e.initFunc = m -> {
                setMonsterHp(m, 1000);
                m.damage.clear();
                m.damage.add(bloods, new DamageInfo(m, 2));
                m.damage.add(massive, new DamageInfo(m, 50));
                m.addPower(new BeatOfDeathPower(m, 3));
                m.addPower(new InvinciblePower(m, 50){
                    @Override
                    public void stackPower(int stackAmount) {
                        int oldMaxInt = getField(InvinciblePower.class, this, "maxAmt");
                        setField(InvinciblePower.class, this, "maxAmt", oldMaxInt + stackAmount);
                        super.stackPower(stackAmount);
                    }
                });
                m.addPower(new MetallicizePower(m, 5));
                m.addPower(new ThornsPower(m, 3));
                m.addPower(new RegenerateMonsterPower(m, 10));
                m.addPower(new MalleablePower(m, 5));
                m.addPower(new BarricadePower(m));
            };
            e.preBattle = m -> {
                CardCrawlGame.music.unsilenceBGM();
                AbstractDungeon.scene.fadeOutAmbiance();
                AbstractDungeon.getCurrRoom().playBgmInstantly("BOSS_ENDING");
                return true;
            };
            e.putBool("firstTurn", true);
            e.putInt("turnCount", 0);
            e.takeTurn = m -> {
                int turnCount = e.getInt("turnCount");
                turnCount++;
                e.setInt("turnCount", turnCount);
                m.addToBot(new ApplyPowerAction(m, m, new BufferPower(m, 1)));
                m.addToBot(new ApplyPowerAction(m, m, new InvinciblePower(m, 50)));
                m.addToBot(new MakeTempCardInDrawPileAction(new HeartOfSpire(), 1, false, true));
                switch (m.nextMove) {
                    case debuff:
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new VulnerablePower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new WeakPower(LMSK.Player(), 2, true)));
                        m.addToBot(new ApplyPowerAction(LMSK.Player(), m, new FrailPower(LMSK.Player(), 2, true)));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Slimed(), 1, true, true));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Burn(), 1, true, true));
                        m.addToBot(new MakeTempCardInDrawPileAction(new Wound(), 1, true, true));
                        if (!e.getBool("firstTurn")) {
                            m.addToBot(new VFXAction(new BorderFlashEffect(new Color(0.8F, 0.5F, 1F, 1F))));
                            m.addToBot(new VFXAction(new HeartBuffEffect(m.hb.cX, m.hb.cY)));
                            m.addToBot(new ApplyPowerAction(m, m, new StrengthPower(m, 2)));
                            m.addToBot(new ApplyPowerAction(m, m, new BeatOfDeathPower(m, 1)));
                            m.addToBot(new ApplyPowerAction(m, m, new MetallicizePower(m, 3)));
                            m.addToBot(new ApplyPowerAction(m, m, new ThornsPower(m, 2)));
                            m.addToBot(new ApplyPowerAction(m, m, new RegenerateMonsterPower(m, 3)));
                            AbstractPower p = m.getPower(StrengthPower.POWER_ID);
                            if (p != null && p.amount < 0) {
                                m.addToBot(new RemoveSpecificPowerAction(m, m, p));
                            }
                            m.addToBot(new ApplyPowerAction(m, m, new ArtifactPower(m, 2)));
                        }
                        e.setBool("firstTurn", false);
                        break;
                    case bloods:
                        m.addToBot(new VFXAction(new BloodShotEffect(m.hb.cX, m.hb.cY, LMSK.Player().hb.cX, LMSK.Player().hb.cY, multiCount), 0.3F));
                        for (int i = 0; i < multiCount; i++) {
                            m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(bloods), BLUNT_HEAVY, true));
                        }
                        m.addToBot(new MakeTempCardInDiscardAction(new Dazed(), 1));
                        break;
                    case massive:
                        m.addToBot(new VFXAction(new ViceCrushEffect(LMSK.Player().hb.cX, LMSK.Player().hb.cY), 0.5F));
                        m.addToBot(new DamageAction(LMSK.Player(), m.damage.get(massive), BLUNT_HEAVY));
                        break;
                }
                m.addToBot(new RollMoveAction(m));
                return true;
            };
            e.getMove = (m, roll) -> {
                if (e.getBool("firstTurn")) {
                    m.setMove(debuff, AbstractMonster.Intent.STRONG_DEBUFF);
                } else {
                    int turnCount = e.getInt("turnCount");
                    if (turnCount % 3 == 0) {
                        m.setMove(debuff, AbstractMonster.Intent.STRONG_DEBUFF);
                    } else if (lastMove(m, bloods)) {
                        m.setMove(massive, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(massive).base);
                    } else if (lastMove(m, massive)) {
                        m.setMove(bloods, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(bloods).base, multiCount, true);
                    } else {
                        if (roll < 50) {
                            m.setMove(bloods, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(bloods).base, multiCount, true);
                        } else {
                            m.setMove(massive, AbstractMonster.Intent.ATTACK_DEBUFF, m.damage.get(massive).base);
                        }
                    }
                }
                return true;
            };
        }
    }
}