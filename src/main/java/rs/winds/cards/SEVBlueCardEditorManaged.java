package rs.winds.cards;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.vfx.combat.BlizzardEffect;
import com.megacrit.cardcrawl.vfx.combat.MindblastEffect;
import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.convert.Transformer;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import rs.lazymankits.actions.utility.QuickAction;
import rs.winds.core.King;
import rs.winds.powers.WeakPlusPower;

import static rs.winds.monsters.SETool.*;
import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.winds.core.ClosedBeta.*;

public class SEVBlueCardEditorManaged {
    
    private static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    private static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    @SECardEditor(card = SweepingBeam.class, functional = BETA_2)
    public static class SweepingBeamPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Disarm"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                c.magicNumber = c.baseMagicNumber = 2;
            };
            e.upgrade = c -> {
                if (!c.upgraded)
                    upgradeCardMagic(c, 1);
                return false;
            };
        }
    }
    
    @SECardEditor(card = GoForTheEyes.class, functional = BETA_2)
    public static class GoForTheEyesPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("GoForTheEyes"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.damage = c.baseDamage = 4;
                c.magicNumber = c.baseMagicNumber = 1;
            };
            e.use = (c,p,m) -> {
                addToBot(new DamageAction(m, new DamageInfo(p, c.damage, c.damageTypeForTurn), SLASH_DIAGONAL));
                addToBot(new ApplyPowerAction(m, p, new WeakPlusPower(m, c.magicNumber, false)));
                return true;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardDamage(c, 1);
                    upgradeCardMagic(c, 1);
                }
                return true;
            };
        }
        @SpirePatch(clz = GoForTheEyes.class, method = "triggerOnGlowCheck")
        public static class GlowPatch {
            @SpirePrefixPatch
            public static SpireReturn Prefix(AbstractCard _inst) {
                CardEditor e = GetEditor(_inst);
                if (e.canModify())
                    return SpireReturn.Return();
                return SpireReturn.Continue();
            }
        }
    }
    
    @SECardEditor(card = SteamBarrier.class, functional = BETA_2)
    public static class SteamBarrierPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Steam"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                addToBot(new GainBlockAction(p, p, c.block));
                addToBot(new ModifyBlockAction(c.uuid, 1));
                return true;
            };
        }
    }
    
    @SECardEditor(card = ThunderStrike.class, functional = BETA_2)
    public static class ThunderStrikePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Steam"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                c.cost = c.costForTurn = 1;
            };
        }
    }
    
    @SECardEditor(card = Blizzard.class, functional = BETA_2)
    public static class BlizzardPatch {
        public static final CardStrings strings = King.CardStrings(King.MakeID("Blizzard"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                c.cost = c.costForTurn = 2;
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                for (int i = 0; i < 2; i++) {
                    addToBot(new ChannelAction(new Frost()));
                }
                addToBot(new QuickAction(() -> {
                    int count = 0;
                    for (AbstractOrb orb : AbstractDungeon.actionManager.orbsChanneledThisCombat) {
                        if (orb instanceof Frost)
                            count++;
                    }
                    c.baseDamage = count * c.magicNumber;
                    c.calculateCardDamage(null);
                    addToTop(new DamageAllEnemiesAction(p, c.multiDamage, c.damageTypeForTurn, BLUNT_HEAVY, false));
                    addToTop(new VFXAction(new BlizzardEffect(count, AbstractDungeon.getMonsters().shouldFlipVfx()), 0.25F));
                }));
                return true;
            };
        }
        @SpirePatch(clz = Blizzard.class, method = SpirePatch.CONSTRUCTOR)
        public static class RawPatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                CtClass ctB = ctBehavior.getDeclaringClass();
                CtMethod[] methods = ctB.getDeclaredMethods();
                for (CtMethod ctM : methods) {
                    ctM.instrument(new ExprEditor(){
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            if ("DESCRIPTION".equals(f.getFieldName())) {
                                f.replace("{$_=" + BlizzardPatch.class.getName() + ".strings.DESCRIPTION;}");
                            }
                        }
                    });
                }
            }
        }
    }
    
    @SECardEditor(card = Hyperbeam.class, functional = BETA_2)
    public static class HyperbeamPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Hyperbeam"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                addToBot(new SFXAction("ATTACK_HEAVY"));
                addToBot(new VFXAction(p, new MindblastEffect(p.dialogX, p.dialogY, p.flipHorizontal), 0.1F));
                addToBot(new DamageAllEnemiesAction(p, c.multiDamage, c.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
                addToBot(new LoseHPAction(p, p, c.magicNumber));
                return true;
            };
        }
    }
    
    @SECardEditor(card = Stack.class, functional = BETA_2)
    public static class StackPatch {
        public static final CardStrings strings = King.CardStrings(King.MakeID("Stack"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.exhaust = true;
            };
        }
        @SpirePatch(clz = Stack.class, method = "applyPowers")
        public static class RawPatch {
            @SpireRawPatch
            public static void Raw(CtBehavior ctBehavior) throws Exception {
                CtClass ctB = ctBehavior.getDeclaringClass();
                CtMethod[] methods = ctB.getDeclaredMethods();
                for (CtMethod ctM : methods) {
                    ctM.instrument(new ExprEditor(){
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            if ("DESCRIPTION".equals(f.getFieldName())) {
                                f.replace("{$_=" + StackPatch.class.getName() + ".strings.DESCRIPTION;}");
                            } else if ("UPGRADE_DESCRIPTION".equals(f.getFieldName())) {
                                f.replace("{$_=" + StackPatch.class.getName() + ".strings.UPGRADE_DESCRIPTION;}");
                            }
                        }
                    });
                }
                ctBehavior.instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess f) throws CannotCompileException {
                        if ("discardPile".equals(f.getFieldName())) {
                            f.replace("{$_=" + AbstractDungeon.class.getName() + ".player.masterDeck;}");
                        }
                    }
                });
                ctBehavior.instrument(new CodeConverter() {{
                    this.transformers = new Transformer(this.transformers) {
                        @Override
                        public int transform(CtClass ctClass, int i, CodeIterator iterator, ConstPool constPool) throws CannotCompileException, BadBytecode {
                            int code = iterator.byteAt(i);
                            if (code == IFEQ) {
                                int icode = iterator.byteAt(i + 8);
                                if (icode == ICONST_3) {
                                    Bytecode bc = new Bytecode(constPool);
                                    bc.add(IFNE);
                                    iterator.writeByte(NOP, i);
                                    iterator.writeByte(NOP, i + 9);
                                    iterator.write(bc.get(), i);
                                    bc = new Bytecode(constPool);
                                    bc.add(ISUB);
                                    iterator.write(bc.get(), i + 9);
                                }
                            }
                            return i;
                        }
                    };
                }});
            }
//            @SpireInstrumentPatch
            
        }
    }
}
