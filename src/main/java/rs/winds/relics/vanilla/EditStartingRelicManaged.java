package rs.winds.relics.vanilla;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.relics.*;
import javassist.*;
import javassist.bytecode.*;
import javassist.convert.Transformer;
import rs.lazymankits.utils.LMSK;

public class EditStartingRelicManaged {
    @SpirePatch(clz = BurningBlood.class, method = "getUpdatedDescription")
    public static class BurningBloodSE {
        protected static final int BLOOD_HEAL_AMT = 10;
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = _result.replace("6", Integer.toString(BLOOD_HEAL_AMT));
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtClass relic = ctClass.getClassPool().get(AbstractRelic.class.getName());
            CtMethod oV = ctClass.getDeclaredMethod("onVictory");
            oV.instrument(new BloodCodeConverter().transform(6, relic));
        }
    }
    @SpirePatch(clz = BlackBlood.class, method = "getUpdatedDescription")
    public static class BlackBloodSE {
        protected static final int BLOOD_HEAL_AMT = 20;
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = _result.replace("12", Integer.toString(BLOOD_HEAL_AMT));
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtClass relic = ctClass.getClassPool().get(AbstractRelic.class.getName());
            CtMethod oV = ctClass.getDeclaredMethod("onVictory");
            oV.instrument(new BloodCodeConverter().transform(12, relic));
        }
    }
    
    private static class BloodCodeConverter extends CodeConverter {
        private CodeConverter transform(int originValue, CtClass relicClass) {
            transformers = new Transformer(transformers){
                @Override
                public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool) throws BadBytecode {
                    int pushcode = iterator.byteAt(index);
                    if (pushcode == BIPUSH) {
                        int bytecode = iterator.byteAt(index + 1);
                        if (bytecode == originValue) {
                            iterator.writeByte(NOP, index);
                            iterator.writeByte(NOP, index + 1);
                            Bytecode bc = new Bytecode(constPool);
                            bc.addAload(0);
                            bc.addInvokestatic(EditStartingRelicManaged.class.getName(), "GetBloodHealAmt", 
                                    Descriptor.ofMethod(CtClass.intType, new CtClass[]{relicClass}));
                            iterator.insert(index, bc.get());
                        }
                    }
                    return index;
                }
            };
            return this;
        }
    }
    
    public static int GetBloodHealAmt(AbstractRelic relic) {
        if (BurningBlood.ID.equals(relic.relicId))
            return BurningBloodSE.BLOOD_HEAL_AMT;
        if (BlackBlood.ID.equals(relic.relicId))
            return BlackBloodSE.BLOOD_HEAL_AMT;
        return 0;
    }
    
    public static class CrackedCoreSE {
        @SpirePatch(clz = CrackedCore.class, method = "atPreBattle")
        public static class PreBattlePatch {
            @SpirePostfixPatch
            public static void Postfix(AbstractRelic _inst) {
                LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), LMSK.Player(), new FocusPower(LMSK.Player(), 1)));
            }
        }
        @SpirePatch(clz = CrackedCore.class, method = "getUpdatedDescription")
        public static class DescriptionPatch {
            @SpirePostfixPatch
            public static String Postfix(String _result, AbstractRelic _inst) {
                _result = "战斗开始时， #y生成 一个 #y闪电 充能球，获得 #b1 #y集中 。";
                return _result;
            }
        }
    }
    
    @SpirePatch(clz = FrozenCore.class, method = "getUpdatedDescription")
    public static class FrozenCoreSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "#r替换 破损核心，战斗开始时，生成一个 #y闪电 充能球，一个 #y冰霜 充能球，获得 #b2 #y集中 。";
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod opet = ctClass.getDeclaredMethod("onPlayerEndTurn");
            ctClass.removeMethod(opet);
            CtMethod apb = CtNewMethod.make(CtClass.voidType, "atPreBattle", new CtClass[0], null, 
                    "{" + FrozenCoreSE.class.getName() + ".FrozenCorePreBattle($0);}", ctClass);
            ctClass.addMethod(apb);
        }
        
        public static void FrozenCorePreBattle(AbstractRelic r) {
            LMSK.Player().channelOrb(new Lightning());
            LMSK.Player().channelOrb(new Frost());
            LMSK.AddToBot(new ApplyPowerAction(LMSK.Player(), LMSK.Player(), new FocusPower(LMSK.Player(), 2)));
        }
    }
    
    @SpirePatch(clz = PureWater.class, method = "getUpdatedDescription")
    public static class PureWaterSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "每回合开始时，将一张奇迹放入你的手牌。";
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod abspd = ctClass.getDeclaredMethod("atBattleStartPreDraw");
            ctClass.removeMethod(abspd);
            CtMethod ats = CtNewMethod.make(CtClass.voidType, "atTurnStart", new CtClass[0], null,
                    "{" + PureWaterSE.class.getName() + ".PureWaterTurnStart($0);}", ctClass);
            ctClass.addMethod(ats);
        }
        
        public static void PureWaterTurnStart(AbstractRelic r) {
            LMSK.AddToBot(new RelicAboveCreatureAction(LMSK.Player(), r));
            LMSK.AddToBot(new MakeTempCardInHandAction(new Miracle(), 1));
        }
    }
    
    @SpirePatch(clz = HolyWater.class, method = "getUpdatedDescription")
    public static class HolyWaterSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "每回合开始时，将两张奇迹放入你的手牌。";
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod abspd = ctClass.getDeclaredMethod("atBattleStartPreDraw");
            ctClass.removeMethod(abspd);
            CtMethod ats = CtNewMethod.make(CtClass.voidType, "atTurnStart", new CtClass[0], null,
                    "{" + PureWaterSE.class.getName() + ".PureWaterTurnStart($0);}", ctClass);
            ctClass.addMethod(ats);
        }
        
        public static void PureWaterTurnStart(AbstractRelic r) {
            LMSK.AddToBot(new RelicAboveCreatureAction(LMSK.Player(), r));
            LMSK.AddToBot(new MakeTempCardInHandAction(new Miracle(), 2));
        }
    }
    
    @SpirePatch(clz = SnakeRing.class, method = "getUpdatedDescription")
    public static class SnakeRingSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "每回合开始时，额外抽一张牌，丢弃一张牌。";
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod abs = ctClass.getDeclaredMethod("atBattleStart");
            ctClass.removeMethod(abs);
            CtMethod oe = CtNewMethod.make(CtClass.voidType, "onEquip", new CtClass[0], null, 
                    "{" + AbstractDungeon.class.getName() + ".player.masterHandSize++;}", ctClass);
            CtMethod oue = CtNewMethod.make(CtClass.voidType, "onUnequip", new CtClass[0], null,
                    "{" + AbstractDungeon.class.getName() + ".player.masterHandSize--;}", ctClass);
            CtMethod atspd = CtNewMethod.make(CtClass.voidType, "atTurnStartPostDraw", new CtClass[0], null,
                    "{" + SnakeRingSE.class.getName() + ".RingTurnStart($0);}", ctClass);
            ctClass.addMethod(oe);
            ctClass.addMethod(oue);
            ctClass.addMethod(atspd);
        }
        
        public static void RingTurnStart(AbstractRelic r) {
            r.flash();
            LMSK.AddToBot(new DiscardAction(LMSK.Player(), LMSK.Player(), 1, false));
        }
    }
    
    @SpirePatch(clz = RingOfTheSerpent.class, method = "getUpdatedDescription")
    public static class RingOfTheSerpentSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "每回合开始时，额外抽两张牌，丢弃一张牌。";
            return _result;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod ats = ctClass.getDeclaredMethod("atTurnStart");
            ctClass.removeMethod(ats);
            CtMethod oe = ctClass.getDeclaredMethod("onEquip");
            oe.instrument(new RingCodeConverter().iconst(2));
            CtMethod oue = ctClass.getDeclaredMethod("onUnequip");
            oue.instrument(new RingCodeConverter().iconst(2));
            CtMethod atspd = CtNewMethod.make(CtClass.voidType, "atTurnStartPostDraw", new CtClass[0], null,
                    "{" + RingOfTheSerpentSE.class.getName() + ".RingTurnStart($0);}", ctClass);
            ctClass.addMethod(atspd);
        }
        
        public static void RingTurnStart(AbstractRelic r) {
            r.flash();
            LMSK.AddToBot(new DiscardAction(LMSK.Player(), LMSK.Player(), 1, false));
        }
        
        private static class RingCodeConverter extends CodeConverter {
            private RingCodeConverter iconst(int v) {
                transformers = new Transformer(transformers) {
                    @Override
                    public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool) 
                            throws CannotCompileException, BadBytecode {
                        int constbyte = iterator.byteAt(index);
                        if (constbyte == ICONST_1) {
                            iterator.writeByte(NOP, index);
                            Bytecode bc = new Bytecode(constPool);
                            bc.addIconst(v);
                            iterator.write(bc.get(), index);
                        }
                        return index;
                    }
                };
                return this;
            }
        }
    }
}