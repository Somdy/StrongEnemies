package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;
import javassist.*;
import javassist.bytecode.*;
import javassist.convert.Transformer;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rs.lazymankits.utils.LMDamageInfoHelper;
import rs.winds.core.King;

import java.util.Map;

public class IntangiblePowerPatch {
    @SpirePatches({
            @SpirePatch(clz = IntangiblePower.class, method = "atDamageFinalReceive"),
            @SpirePatch(clz = IntangiblePlayerPower.class, method = "atDamageFinalReceive")
    })
    public static class IntangibleDamageReceivePatch {
        @SpirePrefixPatch
        public static SpireReturn<Float> Prefix(AbstractPower _inst, float d, DamageInfo.DamageType type) {
            if (d > King.INTANGIBLE_FINAL_DAMAGE)
                d = King.INTANGIBLE_FINAL_DAMAGE;
            return SpireReturn.Return(d);
        }
    }
    
    @SpirePatches({
            @SpirePatch(clz = IntangiblePower.class, method = SpirePatch.CONSTRUCTOR),
            @SpirePatch(clz = IntangiblePlayerPower.class, method = SpirePatch.CONSTRUCTOR)
    })
    public static class StackPowerPatch {
        @SpirePostfixPatch
        public static void ConstructorPostfix(AbstractPower _inst, AbstractCreature c, int turns) {
            if (_inst.amount > 3)
                _inst.amount = 3;
        }
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws Exception {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod stackPower = CtNewMethod.make(CtClass.voidType, "stackPower", new CtClass[]{CtClass.intType}, null, 
                    "{if($0.amount+$1>=3)return;else{super.stackPower($$);}}", ctClass);
            ctClass.addMethod(stackPower);
        }
    }
    
    @SpirePatch(clz = LocalizedStrings.class, method = SpirePatch.CONSTRUCTOR)
    public static class IntangibleDescriptionPatch {
        @SpirePostfixPatch
        public static void Postfix(LocalizedStrings _inst, Map<String, PowerStrings> ___powers) {
            PowerStrings strings = ___powers.get(IntangiblePower.POWER_ID);
            if (strings != null) {
                strings.DESCRIPTIONS[0] = strings.DESCRIPTIONS[0].replace("1", String.valueOf(King.INTANGIBLE_FINAL_DAMAGE));
                strings.DESCRIPTIONS[0] += "最多可以叠 #b3 层";
            }
            strings = ___powers.get(IntangiblePlayerPower.POWER_ID);
            if (strings != null) {
                strings.DESCRIPTIONS[0] = strings.DESCRIPTIONS[0].replace("1", String.valueOf(King.INTANGIBLE_FINAL_DAMAGE));
                strings.DESCRIPTIONS[0] += "最多可以叠 #b3 层";
            }
        }
    }
    
    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class PlayerDamagePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"damageAmount"})
        public static void Insert(AbstractPlayer __instance, DamageInfo info, @ByRef int[] damageAmount) {
            if (LMDamageInfoHelper.HasTag(info, King.IGNORE_INTANGIBLE)) {
                damageAmount[0] = info.output;
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "decrementBlock");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
        
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws Exception {
            ctBehavior.instrument(new CodeConverter(){{
                transformers = new Transformer(transformers) {
                    @Override
                    public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool) {
                        int constcode = iterator.byteAt(index);
                        if (constcode == ICONST_1) {
                            int icmpcode = iterator.byteAt(index + 1);
                            int invokecode = iterator.byteAt(index + 8);
                            if (icmpcode == IF_ICMPLE && invokecode == INVOKEVIRTUAL) {
                                Bytecode bc = new Bytecode(constPool);
                                bc.addIconst(King.INTANGIBLE_FINAL_DAMAGE);
                                iterator.write(bc.get(), index);
                                bc = new Bytecode(constPool);
                                bc.addIconst(King.INTANGIBLE_FINAL_DAMAGE);
                                iterator.write(bc.get(), index + 14);
                            }
                        }
                        return index;
                    }
                };
            }});
        }
    }
    
    @SpirePatch(clz = AbstractMonster.class, method = "damage")
    public static class MonsterDamagePatch {
        
        public static boolean IgnoreIntangible(DamageInfo info) {
            return LMDamageInfoHelper.HasTag(info, King.IGNORE_INTANGIBLE);
        }
        
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws Exception {
            ctBehavior.instrument(new CodeConverter(){{
                transformers = new Transformer(transformers) {
                    @Override
                    public int transform(CtClass ctClass, int index, CodeIterator iterator, ConstPool constPool) throws CannotCompileException, BadBytecode {
                        int getfieldcode = iterator.byteAt(index);
                        boolean fieldlocated = false;
                        if (getfieldcode == GETFIELD) {
                            int ifcode = iterator.byteAt(index + 3);
                            int invokecode = iterator.byteAt(index + 10);
                            if (ifcode == IFLE && invokecode == INVOKEVIRTUAL) {
                                fieldlocated = true;
                                Bytecode bc = new Bytecode(constPool);
                                bc.addIconst(King.INTANGIBLE_FINAL_DAMAGE);
                                iterator.insert(index + 3, bc.get());
                                bc = new Bytecode(constPool);
                                bc.add(Opcode.IF_ICMPLE);
                                iterator.write(bc.get(), index + 4);
                            }
                        }
                        if (fieldlocated) {
                            int constcode = iterator.byteAt(index + 18);
                            if (constcode == ICONST_1) {
                                Bytecode bc = new Bytecode(constPool);
                                bc.addIconst(King.INTANGIBLE_FINAL_DAMAGE);
                                iterator.write(bc.get(), index + 18);
                            }
                        }
                        return index;
                    }
                };
            }});
            ctBehavior.instrument(new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("hasPower".equals(m.getMethodName())) {
                        m.replace("{$_=$proceed($$) && !" + MonsterDamagePatch.class.getName() + ".IgnoreIntangible(info);}");
                    }
                }
            });
        }
    }
}