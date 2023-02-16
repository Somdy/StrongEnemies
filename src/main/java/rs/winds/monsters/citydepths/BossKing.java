package rs.winds.monsters.citydepths;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireOverride;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.*;
import org.jetbrains.annotations.Nullable;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.SETool;
import rs.winds.powers.AuthorityPower;
import rs.winds.powers.HighSpellResistancePower;
import rs.winds.powers.MidSpellResistancePower;
import rs.winds.powers.SpotWeaknessPower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Not done yet
public class BossKing extends AbstractMonster {
    public static final String ID = King.MakeID("BossKing");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final String IMG = "SEAssets/images/monsters/king/king.png";
    public static final float BOSS_KING_OFFSET_X = -150F;
    public static final float BOSS_KING_OFFSET_Y = 0F;
    public static final float PALADIN_OFFSET_X = BOSS_KING_OFFSET_X - 260F;
    public static final float PALADIN_OFFSET_Y = BOSS_KING_OFFSET_Y + 360F;
    public static final float RECRUIT_OFFSET_X = BOSS_KING_OFFSET_X + 260F;
    public static final float RECRUIT_OFFSET_Y = BOSS_KING_OFFSET_Y + 350F;
    public static final float SLAVER_ELITE_OFFSET_X = BOSS_KING_OFFSET_X - 320F;
    public static final float SLAVER_ELITE_OFFSET_Y = BOSS_KING_OFFSET_Y - 10F;
    public static final float KINGDOM_CHAMP_OFFSET_X = BOSS_KING_OFFSET_X + 300F;
    public static final float KINGDOM_CHAMP_OFFSET_Y = BOSS_KING_OFFSET_Y;
    private static final int normal_attack = 0;
    private static final int intangible_attack = 1;
    private static final int barricade_attack = 2;
    private static final int both_attack = 3;
    private static final byte[] special_attacks = new byte[]{0, 1, 2, 3};
    private static final byte first_summon = 10;
    private static final byte buff_allies = 11;
    private static final byte summon_allies = 12;
    private static final int attack_base_damage = 15;
    private static final int attack_base_multi = 3;
    private byte specialAttackKey;
    private boolean specialAttacking;
    private boolean specialAttackedLast;
    private boolean continousAllyMove;
    private final Map<Byte, SpecialAttack> attackMap = new HashMap<>();
    private boolean firstTurn;
    private BossKingMap map;
    
    public BossKing(float x, float y) {
        super(NAME, ID, 550, 0, 0, 328, 310, IMG, x, y);
        type = EnemyType.BOSS;
        addPower(new AuthorityPower(this));
        addPower(new MidSpellResistancePower(this));
        addPower(new SpotWeaknessPower(this));
        addPower(new BufferPower(this, 5));
        addPower(new InvinciblePower(this, 300));
        attackMap.put(special_attacks[normal_attack], new SpecialAttack(this, special_attacks[normal_attack], 
                attack_base_damage, attack_base_multi));
        attackMap.put(special_attacks[intangible_attack], new SpecialAttack(this, special_attacks[intangible_attack], 
                attack_base_multi, attack_base_damage));
        attackMap.put(special_attacks[barricade_attack], new SpecialAttack(this, special_attacks[barricade_attack], 
                attack_base_damage, attack_base_multi));
        attackMap.put(special_attacks[both_attack], new SpecialAttack(this, special_attacks[both_attack], 
                2 * attack_base_multi, attack_base_damage));
        damage.add(attackMap.get(special_attacks[normal_attack]).info);
        damage.add(attackMap.get(special_attacks[intangible_attack]).info);
        damage.add(attackMap.get(special_attacks[barricade_attack]).info);
        damage.add(attackMap.get(special_attacks[both_attack]).info);
        firstTurn = true;
        specialAttacking = false;
        specialAttackedLast = false;
        specialAttackKey = -1;
        continousAllyMove = false;
    }
    
    @Override
    public void usePreBattleAction() {
        CardCrawlGame.music.unsilenceBGM();
        AbstractDungeon.scene.fadeOutAmbiance();
        CardCrawlGame.music.playTempBgmInstantly("SE_BossKing_BGM.mp3", true);
        map = new BossKingMap(this);
    }
    
    @Override
    public void takeTurn() {
        if (specialAttacking && specialAttackKey >= 0) {
            SpecialAttack sa = attackMap.get(specialAttackKey);
            addToBot(new TalkAction(this, DIALOG[3]));
            for (int i = 0; i < sa.multi; i++) {
                addToBot(new DamageAction(LMSK.Player(), damage.get(sa.move), AbstractGameAction.AttackEffect.SLASH_HEAVY));
            }
            specialAttackKey = -1;
            specialAttacking = false;
            specialAttackedLast = true;
            continousAllyMove = true;
        } else {
            if (specialAttackedLast) {
                specialAttackedLast = false;
                continousAllyMove = true;
            }
            playSfx();
            talk();
            switch (nextMove) {
                case first_summon:
                    SlaverElite elite = new SlaverElite(SLAVER_ELITE_OFFSET_X, SLAVER_ELITE_OFFSET_Y, false);
                    addToBot(new SpawnMonsterAction(elite, false));
                    KingdomChamp champ = new KingdomChamp(KINGDOM_CHAMP_OFFSET_X, KINGDOM_CHAMP_OFFSET_Y);
                    addToBot(new SpawnMonsterAction(champ, false));
                    map.elite = elite;
                    map.champ = champ;
                    if (map.allAlive()) {
                        setMove(buff_allies, Intent.BUFF);
                    } else {
                        setMove(summon_allies, Intent.UNKNOWN);
                    }
                    return;
                case summon_allies:
                    for (int i = 0; i < 2; i++) {
                        AbstractMonster m = map.getBackup();
                        if (m != null) {
                            addToBot(new SpawnMonsterAction(m, false));
                        }
                    }
                    break;
                case buff_allies:
                    addToBot(new ApplyPowerToEnemiesAction(this, StrengthPower.class, ApplyPowerParam.ANY_OWNER, 5));
                    for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> m != null && !m.isDeadOrEscaped())) {
                        addToBot(new GainBlockAction(m, this, 10));
                    }
                    addToBot(new ApplyPowerToEnemiesAction(this, PlatedArmorPower.class, ApplyPowerParam.ANY_OWNER, 10));
                    break;
            }
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (firstTurn) {
            firstTurn = false;
            setMove(first_summon, Intent.UNKNOWN);
        } else {
            if (specialAttackedLast || continousAllyMove) {
                continousAllyMove = false;
                if (map.allAlive()) {
                    setMove(buff_allies, Intent.BUFF);
                } else {
                    setMove(summon_allies, Intent.UNKNOWN);
                }
            } else {
                specialAttacking = true;
                spotWeakness(false);
            }
        }
    }
    
    @Override
    public void die() {
        if (!AbstractDungeon.getCurrRoom().cannotLose) {
            super.die();
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> !m.isDeadOrEscaped())) {
                addToBot(new EscapeAction(m));
            }
            onBossVictoryLogic();
            onFinalBossVictoryLogic();
            CardCrawlGame.stopClock = true;
        }
    }
    
    private boolean applyBackAttack() {
        return AbstractDungeon.player.hasPower("Surrounded")
                && (AbstractDungeon.player.flipHorizontal && AbstractDungeon.player.drawX < this.drawX
                || !AbstractDungeon.player.flipHorizontal && AbstractDungeon.player.drawX > this.drawX);
    }
    
    @Override
    public void applyPowers() {
        if (hasPower(SpotWeaknessPower.ID))
            spotWeakness(true);
        boolean applyBackAttack = applyBackAttack();
        if (applyBackAttack && !hasPower("BackAttack"))
            AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(this, null, new BackAttackPower(this)));
        EnemyMoveInfo move = ReflectionHacks.getPrivate(this, AbstractMonster.class, "move");
        if (move.baseDamage > -1)
            calculateDamage(move.baseDamage);
        SETool.setField(AbstractMonster.class, this, "intentImg", 
                SETool.getMethod(AbstractMonster.class, "getIntentImg").invoke(this));
        SETool.getMethod(AbstractMonster.class, "updateIntentTip").invoke(this);
    }
    
    public void spotWeakness(boolean powerModified) {
        if (!specialAttacking) return;
        boolean hasIntangible = LMSK.Player().hasPower(IntangiblePlayerPower.POWER_ID);
        boolean hasBarricade = LMSK.Player().hasPower(BarricadePower.POWER_ID);
        byte finalMove;
        if (hasIntangible && hasBarricade) {
            finalMove = special_attacks[both_attack];
        } else if (hasIntangible) {
            finalMove = special_attacks[intangible_attack];
        } else if (hasBarricade) {
             finalMove = special_attacks[barricade_attack];
        } else {
            finalMove = special_attacks[normal_attack];
        }
        specialAttackKey = finalMove;
        if (powerModified || nextMove != specialAttackKey) {
            attackMap.get(finalMove).applyPowers(hasIntangible, hasBarricade);
            damage.set(finalMove, attackMap.get(finalMove).info);
//            King.Log("Setting move: " + attackMap.get(finalMove).toString());
            setMove(finalMove, Intent.ATTACK, damage.get(finalMove).base, attackMap.get(finalMove).multi, true);
            createIntent();
        }
    }
    
    @SpireOverride
    protected void calculateDamage(int dmg) {
        if (specialAttacking) {
            dmg = damage.get(nextMove).base;
        }
        if (dmg < 0) dmg = 0;
        ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentDmg", dmg);
    }
    
    private void playSfx() {
        int roll = MathUtils.random(6);
        addToBot(new SFXAction(King.MakeID("BOSS_KING_SOUND_" + roll)));
    }
    
    private void talk() {
        int roll = MathUtils.random(2);
        addToBot(new TalkAction(this, DIALOG[roll]));
    }
    
    private static class BossKingMap {
        private final BossKing bossKing;
        private SlaverElite elite;
        private Paladin paladin;
        private Recruit recruit;
        private KingdomChamp champ;
        
        private BossKingMap(BossKing bossKing) {
            this.bossKing = bossKing;
            for (AbstractMonster m : LMSK.GetAllExptMstr(m -> m instanceof BossKingAlly)) {
                if (m instanceof SlaverElite && elite == null)
                    elite = (SlaverElite) m;
                if (m instanceof Paladin && paladin == null)
                    paladin = (Paladin) m;
                if (m instanceof Recruit && recruit == null)
                    recruit = (Recruit) m;
                if (m instanceof KingdomChamp && champ == null)
                    champ = (KingdomChamp) m;
            }
        }
        
        @Nullable
        private AbstractMonster getBackup() {
            if (paladin == null || paladin.isDeadOrEscaped()) {
                paladin = new Paladin(PALADIN_OFFSET_X, PALADIN_OFFSET_Y);
                return paladin;
            }
            List<AbstractMonster> allies = new ArrayList<>();
            if (elite == null || elite.isDeadOrEscaped()) {
                allies.add(new SlaverElite(SLAVER_ELITE_OFFSET_X, SLAVER_ELITE_OFFSET_Y, false));
            }
            if (recruit == null || recruit.isDeadOrEscaped()) {
                allies.add(new Recruit(RECRUIT_OFFSET_X, RECRUIT_OFFSET_Y, false));
            }
            if (champ == null || champ.isDeadOrEscaped()) {
                allies.add(new KingdomChamp(KINGDOM_CHAMP_OFFSET_X, KINGDOM_CHAMP_OFFSET_Y));
            }
            AbstractMonster retVal = LMSK.GetRandom(allies, SETool.MonsterAIRng()).orElse(null);
            if (retVal instanceof SlaverElite)
                elite = (SlaverElite) retVal;
            if (retVal instanceof Recruit)
                recruit = ((Recruit) retVal);
            if (retVal instanceof KingdomChamp)
                champ = ((KingdomChamp) retVal);
            return retVal;
        }
        
        private boolean allAlive() {
            return elite != null && !elite.isDeadOrEscaped() && paladin != null && !paladin.isDeadOrEscaped()
                    && recruit != null && !recruit.isDeadOrEscaped() && champ != null && !champ.isDeadOrEscaped();
        }
        
        private boolean contains(AbstractMonster m) {
            int aliveSize = LMSK.GetAllExptMonsters(mo -> mo != null && !mo.isDeadOrEscaped()).size();
            return aliveSize <= 5 || m == bossKing || m == elite || m == paladin || m == recruit || m == champ;
        }
    }
    
    protected interface BossKingAlly {}
    
    private static class SpecialAttack {
        private final BossKing bossKing;
        private final byte move;
        private final int baseDamage;
        private int damage;
        private final int baseMulti;
        private int multi;
        private DamageInfo info;
        
        private SpecialAttack(BossKing bossKing, byte move, int baseDamage, int baseMulti) {
            this.bossKing = bossKing;
            this.move = move;
            this.damage = this.baseDamage = baseDamage;
            this.multi = this.baseMulti = baseMulti;
            info = new DamageInfo(bossKing, baseDamage);
        }
        
        private void applyPowers(boolean intangible, boolean barricade) {
            float tmp = intangible ? baseMulti : baseDamage;
            if (Settings.isEndless && AbstractDungeon.player.hasBlight("DeadlyEnemies")) {
                float mod = AbstractDungeon.player.getBlight("DeadlyEnemies").effectFloat();
                tmp *= mod;
            }
            if (intangible) {
                for (AbstractPower p : bossKing.powers) {
                    if (p instanceof StrengthPower)
                        tmp = p.atDamageGive(tmp, DamageInfo.DamageType.NORMAL);
                }
                damage = King.INTANGIBLE_FINAL_DAMAGE;
            } else {
                if (Settings.isEndless && AbstractDungeon.player.hasBlight("DeadlyEnemies")) {
                    float mod = AbstractDungeon.player.getBlight("DeadlyEnemies").effectFloat();
                    tmp *= mod;
                }
                for (AbstractPower p : bossKing.powers)
                    tmp = p.atDamageGive(tmp, DamageInfo.DamageType.NORMAL);
                for (AbstractPower p : LMSK.Player().powers)
                    tmp = p.atDamageReceive(tmp, DamageInfo.DamageType.NORMAL);
                tmp = AbstractDungeon.player.stance.atDamageReceive(tmp, DamageInfo.DamageType.NORMAL);
                if (bossKing.applyBackAttack())
                    tmp = (int)(tmp * 1.5F);
                for (AbstractPower p : bossKing.powers)
                    tmp = p.atDamageFinalGive(tmp, DamageInfo.DamageType.NORMAL);
                for (AbstractPower p : LMSK.Player().powers)
                    tmp = p.atDamageFinalReceive(tmp, DamageInfo.DamageType.NORMAL);
            }
            int result = MathUtils.floor(tmp);
            if (intangible) {
                multi = result;
            } else {
                damage = barricade ? 2 * result : result;
            }
            info = new DamageInfo(bossKing, damage);
        }
    
        @Override
        public String toString() {
            return "SpecialAttack{" + "move=" + move + ", damage=" + damage + ", multi=" + multi + "}";
        }
    }
}