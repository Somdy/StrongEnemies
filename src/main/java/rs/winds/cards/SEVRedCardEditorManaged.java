package rs.winds.cards;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Stack;
import com.megacrit.cardcrawl.cards.red.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BlurPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.combat.CleaveEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import rs.lazymankits.actions.common.ApplyPowerToEnemiesAction;
import rs.lazymankits.enums.ApplyPowerParam;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static rs.winds.monsters.SETool.*;
import static rs.winds.cards.SECardEditor.*;
import static rs.winds.core.ClosedBeta.*;

public class SEVRedCardEditorManaged {
    
    private static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    private static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    @SECardEditor(card = PerfectedStrike.class, functional = BETA_2)
    public static class PerfectedStrikePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("PerfectedStrike"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.damage = c.baseDamage = 60;
                c.magicNumber = c.baseMagicNumber = 10;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardDamage(c, 10);
                }
                return true;
            };
        }
        @SpirePatch(clz = PerfectedStrike.class, method = "applyPowers")
        public static class RawPatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                CtClass ctB = ctBehavior.getDeclaringClass();
                CtMethod[] methods = ctB.getDeclaredMethods();
                for (CtMethod ctM : methods) {
                    ctM.instrument(new ExprEditor(){
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            if ("magicNumber".equals(f.getFieldName())) {
                                f.replace("{$_=-$0.magicNumber;}");
                            }
                        }
                    });
                }
            }
        }
    }
    
    @SECardEditor(card = Armaments.class, functional = BETA_2)
    public static class ArmamentsPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Armaments"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.block = c.baseBlock = 15;
                c.magicNumber = c.baseMagicNumber = 1;
                c.exhaust = true;
                c.rarity = AbstractCard.CardRarity.UNCOMMON;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    updateCardDesc(c, strings.UPGRADE_DESCRIPTION);
                }
                return true;
            };
            e.use = (c,p,m) -> {
                addToBot(new GainBlockAction(p, c.block));
                if (c.upgraded) {
                    addToBot(new ApplyPowerAction(p, p, new BlurPower(p, c.magicNumber)));
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = IronWave.class, functional = BETA_2)
    public static class IronWavePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("IronWave"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        isDone = true;
                        if (p.powers.stream().anyMatch(po -> po.type == AbstractPower.PowerType.DEBUFF)) {
                            List<AbstractPower> debuffs = p.powers.stream()
                                    .filter(po -> po.type == AbstractPower.PowerType.DEBUFF)
                                    .collect(Collectors.toList());
                            for (int i = 0; i < 2; i++) {
                                Optional<AbstractPower> opt = LMSK.GetRandom(debuffs, LMSK.CardRandomRng());
                                opt.ifPresent(po -> addToTop(new RemoveSpecificPowerAction(p, p, po)));
                            }
                        }
                    }
                });
                return false;
            };
        }
    }
    
    @SECardEditor(card = LimitBreak.class, functional = BETA_2)
    public static class LimitBreakPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("IronWave"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                c.cost = c.costForTurn = 1;
            };
        }
    }
    
    @SECardEditor(card = Cleave.class, functional = BETA_2)
    public static class CleavePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Cleave"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                e.putBool("twiced", false);
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                addToBot(new SFXAction("ATTACK_HEAVY"));
                addToBot(new VFXAction(p, new CleaveEffect(), 0.1F));
                addToBot(new DamageAllEnemiesAction(p, c.multiDamage, c.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
                if (LMSK.GetAllExptMstr(mo -> !mo.isDeadOrEscaped()).size() == 1 && !e.getBool("twiced")) {
                    AbstractCard card = c.makeStatEquivalentCopy();
                    card.purgeOnUse = true;
                    CardEditor ce = GetEditor(card);
                    ce.putBool("twiced", true);
                    addToBot(new NewQueueCardAction(card, true, true, true));
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = Clothesline.class, functional = BETA_2)
    public static class ClotheslinePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Cleave"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                c.damage = c.baseDamage = 16;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardDamage(c, 4);
                    upgradeCardMagic(c, 1);
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = Entrench.class, functional = BETA_2)
    public static class EntrenchPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Entrench"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 3;
            };
            e.use = (c,p,m) -> {
                addToBot(new LoseHPAction(p, p, c.magicNumber));
                return false;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardBaseCost(c, 1);
                    updateCardDesc(c, strings.UPGRADE_DESCRIPTION);
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = Barricade.class, functional = BETA_2)
    public static class BarricadePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Barricade"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.isEthereal = true;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    updateCardDesc(c, strings.UPGRADE_DESCRIPTION);
                    c.isEthereal = false;
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = Disarm.class, functional = BETA_2)
    public static class DisarmPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Disarm"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 2;
                c.target = AbstractCard.CardTarget.ALL_ENEMY;
            };
            e.use = (c,p,m) -> {
                for (int i = 0; i < c.magicNumber; i++) {
                    addToBot(new ApplyPowerToEnemiesAction(p, StrengthPower.class, ApplyPowerParam.ANY_OWNER, -1));
                }
                return true;
            };
        }
    }
}
