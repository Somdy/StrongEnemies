package rs.winds.cards;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LocalVariableAttribute;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.SETool;

import java.util.HashMap;
import java.util.Map;

public class CardEditor {
    private AbstractCard card;
    private boolean modify;
    private Map<String, Boolean> boolMap;
    private Map<String, Integer> intMap;
    private Map<String, Object> objMap;
    protected SEInitFunc initFunc;
    protected CardUseFunc use;
    protected UpgradeFunc upgrade;
    protected ApplyPowersFunc applyPowers;
    protected PostApplyPowersFunc postPowers;
    protected CalcCardDamageFunc calcDamage;
    protected PostCalcCardDamageFunc postCalc;
    protected OnExhaust onExhaust;
    protected OnDrawn onDrawn;
    
    public CardEditor() {
        card = null;
        modify = false;
        boolMap = new HashMap<>();
        intMap = new HashMap<>();
        objMap = new HashMap<>();
        use = (c, p, m) -> false;
        upgrade = c -> false;
        // extra methods
        applyPowers = c -> false;
        calcDamage = (c, m) -> false;
        onExhaust = c -> false;
        onDrawn = c -> false;
    }
    
    public void init() {
        if (initFunc != null)
            initFunc.seInit(card);
    }
    
    public boolean use(AbstractPlayer p, AbstractMonster m) {
        return use.use(card, p, m);
    }
    
    public boolean upgrade() {
        return upgrade.upgrade(card);
    }
    
    public boolean applyPowers() {
        return applyPowers.applyPowers(card);
    }
    
    public void postApplyPowers() {
        if (postPowers != null)
            postPowers.applyPowers(card);
    }
    
    public boolean calcDamage(AbstractMonster m) {
        return calcDamage.calculate(card, m);
    }
    
    public void postCalcDamage(AbstractMonster m) {
        if (postCalc != null)
            postCalc.calculate(card, m);
    }
    
    public boolean onExhaust() {
        return onExhaust.onExhaust(card);
    }
    
    public boolean onDrawn() {
        return onDrawn.onDrawn(card);
    }
    
    public boolean isAssigned() {
        return card != null;
    }
    
    public boolean canModify() {
        return AbstractDungeon.ascensionLevel >= 20 && modify;
    }
    
    public void assign(AbstractCard card) {
        this.card = card;
    }
    
    public CardEditor modify(boolean modify) {
        this.modify = modify;
        return this;
    }
    
    public void putBool(String key, boolean value) {
        boolMap.put(key, value);
    }
    
    public void setBool(String key, boolean value) {
        if (boolMap.containsKey(key))
            boolMap.put(key, value);
    }
    
    public boolean getBool(String key) {
        if (boolMap.containsKey(key))
            return boolMap.get(key);
        throw new NullPointerException("KEY [" + key + "] IS NOT ASSIGNED WITH A BOOLEAN");
    }
    
    public void putInt(String key, int value) {
        intMap.put(key, value);
    }
    
    public void setInt(String key, int value) {
        if (intMap.containsKey(key))
            intMap.put(key, value);
    }
    
    public int getInt(String key) {
        if (intMap.containsKey(key))
            return intMap.get(key);
        throw new NullPointerException("KEY [" + key + "] IS NOT ASSIGNED WITH AN INTEGER");
    }
    
    public void putObj(String key, Object value) {
        objMap.put(key, value);
    }
    
    public void setObj(String key, Object value) {
        if (objMap.containsKey(key))
            objMap.put(key, value);
    }
    
    public <T> T getObj(String key) {
        if (objMap.containsKey(key))
            return (T) objMap.get(key);
        throw new NullPointerException("KEY [" + key + "] IS NOT ASSIGNED WITH AN OBJECT");
    }
    
    protected static void InjectMethods(ClassPool pool, Class<? extends AbstractCard> mClz, String editClz, String[] extraMethods, boolean hasExtraFunctions) {
        if (mClz == null) {
            throw new NullPointerException("CARD CLASS IS NULL");
        }
        try {
            String getEditor = SETool.class.getName() + ".GetEditor(this)";
            CtClass mClass = pool.getCtClass(mClz.getName());
            mClass.defrost();
            CtMethod useMethod = mClass.getDeclaredMethod("use");
            CtMethod upgradeMethod = mClass.getDeclaredMethod("upgrade");
            injectMethod(useMethod, pool, "use($$)");
            injectMethod(upgradeMethod, pool, "upgrade()");
            if (hasExtraFunctions) {
                CtClass monsterClz = pool.get(AbstractMonster.class.getName());
                for (String m : extraMethods) {
                    switch (m) {
                        case SECardEditor.APPLYPOWERS:
                            injectExtraMethod(mClass, "applyPowers", CtClass.voidType, new CtClass[0], pool, "applyPowers()", null);
                            break;
                        case SECardEditor.PST_APPLYPOWERS:
                            injectExtraMethod(mClass, "applyPowers", CtClass.voidType, new CtClass[0], pool, "applyPowers()", "postApplyPowers()");
                            break;
                        case SECardEditor.CALCDAMAGE:
                            injectExtraMethod(mClass, "calculateCardDamage", CtClass.voidType, new CtClass[]{monsterClz}, pool, "calcDamage($1)", null);
                            break;
                        case SECardEditor.PST_CALCDAMAGE:
                            injectExtraMethod(mClass, "calculateCardDamage", CtClass.voidType, new CtClass[]{monsterClz}, pool, "calcDamage($1)", "postCalcDamage($1)");
                            break;
                        case SECardEditor.ONEXHAUST:
                            injectExtraMethod(mClass, "triggerOnExhaust", CtClass.voidType, new CtClass[0], pool, "onExhaust()", null);
                            break;
                        case SECardEditor.ONDRAWN:
                            injectExtraMethod(mClass, "triggerWhenDrawn", CtClass.voidType, new CtClass[0], pool, "onDrawn()", null);
                            break;
                    }
                }
            }
            CtConstructor[] constructors = mClass.getDeclaredConstructors();
            for (CtConstructor constructor : constructors) {
                if (constructor.callsSuper()) {
                    constructor.insertAfter("{" + editClz + ".Edit($0);if(" + getEditor + ".canModify()){"
                            + getEditor + ".init();}}");
                } else {
                    King.PatchLog("Injecting init to [" + mClz.getSimpleName() + "]'s this-called constructor");
                    constructor.insertAfter("{if(" + getEditor + ".canModify()){" + getEditor + ".initThis();}}");
                }
            }
        } catch (Exception e) {
            King.Log("Failed to inject [" + mClz.getSimpleName() + "]");
            e.printStackTrace();
        }
    }
    
    private static void injectExtraMethod(CtClass ctClass, @NotNull String ctMethod, CtClass returnType, CtClass[] params,
                                          @NotNull ClassPool pool, String injectPreMethod, String injectPostMethod) throws Exception {
        String getEditor = SETool.class.getName() + ".GetEditor(this)";
        try {
            CtMethod targetMethod = ctClass.getDeclaredMethod(ctMethod);
            if (injectPreMethod != null && !injectPreMethod.isEmpty())
                injectMethod(targetMethod, pool, injectPreMethod);
            if (injectPostMethod != null && !injectPostMethod.isEmpty()) 
                targetMethod.insertAfter("{if(" + getEditor + ".canModify()){" + getEditor + "." + injectPostMethod);
        } catch (Exception e) {
            CtMethod targetMethod = CtNewMethod.make(returnType, ctMethod, params, null, "{super." + ctMethod + "($$);}", ctClass);
            if (injectPreMethod != null && !injectPreMethod.isEmpty())
                injectMethod(targetMethod, pool, injectPreMethod);
            if (injectPostMethod != null && !injectPostMethod.isEmpty()) 
                targetMethod.insertAfter("{if(" + getEditor + ".canModify()){" + getEditor + "." + injectPostMethod + ";}}");
            ctClass.addMethod(targetMethod);
        }
    }
    
    private static void injectMethod(@NotNull CtMethod ctMethod, @NotNull ClassPool pool, String injectMethod) throws Exception {
        CtClass boolType = pool.get(boolean.class.getName());
        LocalVariableAttribute table = (LocalVariableAttribute) ctMethod.getMethodInfo().getCodeAttribute()
                .getAttribute(LocalVariableAttribute.tag);
        String boolParam = getParamName(table, new int[]{0});
        String getEditor = SETool.class.getName() + ".GetEditor(this)";
        ctMethod.addLocalVariable(boolParam, boolType);
        ctMethod.insertBefore("{if(" + getEditor + ".canModify()){" + boolParam + "=" + getEditor
                + "." + injectMethod + ";if(" + boolParam + ") return;}}");
    }
    
    @NotNull
    private static String getParamName(LocalVariableAttribute table, @NotNull int[] paramIndex) {
        int index = (table != null ? table.length() : 0) + paramIndex[0];
        paramIndex[0]++;
        return "_param_" + index + "_SEReturn";
    }
    
    @FunctionalInterface
    public interface SEInitFunc {
        void seInit(AbstractCard card);
    }
    
    @FunctionalInterface
    public interface CardUseFunc {
        boolean use(AbstractCard card, AbstractPlayer p, AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface UpgradeFunc {
        boolean upgrade(AbstractCard card);
    }
    
    @FunctionalInterface
    public interface ApplyPowersFunc {
        boolean applyPowers(AbstractCard card);
    }
    
    @FunctionalInterface
    public interface PostApplyPowersFunc {
        void applyPowers(AbstractCard card);
    }
    
    @FunctionalInterface
    public interface CalcCardDamageFunc {
        boolean calculate(AbstractCard card, AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface PostCalcCardDamageFunc {
        void calculate(AbstractCard card, AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface OnExhaust {
        boolean onExhaust(AbstractCard card);
    }
    
    @FunctionalInterface
    public interface OnDrawn {
        boolean onDrawn(AbstractCard card);
    }
}
