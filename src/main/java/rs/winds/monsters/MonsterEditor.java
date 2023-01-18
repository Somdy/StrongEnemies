package rs.winds.monsters;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import javassist.bytecode.LocalVariableAttribute;
import org.jetbrains.annotations.NotNull;
import rs.winds.core.King;

import java.util.HashMap;
import java.util.Map;

public class MonsterEditor {
    private AbstractMonster m;
    private boolean modify;
    private Map<String, Boolean> boolMap;
    private Map<String, Integer> intMap;
    private Map<String, Object> objMap;
    protected SEInitFunc initFunc;
    protected SEInitFunc initThis;
    protected UsePreBattleFunc preBattle;
    protected TakeTurnFunc takeTurn;
    protected GetMoveFunc getMove;
    protected PostUpdateFunc postUpdate;
    
    public MonsterEditor() {
        m = null;
        modify = false;
        boolMap = new HashMap<>();
        intMap = new HashMap<>();
        objMap = new HashMap<>();
        preBattle = mo -> false;
        takeTurn = mo -> false;
        getMove = (mo, r) -> false;
        postUpdate = mo -> {};
    }
    
    public void init() {
        if (initFunc != null) 
            initFunc.seInit(m);
    }
    
    public void initThis() {
        if (initThis != null) 
            initThis.seInit(m);
    }
    
    public boolean takeTurn() {
        return takeTurn.takeTurn(m);
    }
    
    public boolean getMove(int roll) {
        return getMove.getMove(m, roll);
    }
    
    public boolean usePreBattle() {
        return preBattle.usePreBattle(m);
    }
    
    public void postUpdate() {
        postUpdate.postUpdate(m);
    }
    
    public boolean isAssigned() {
        return m != null;
    }
    
    public boolean canModify() {
        return AbstractDungeon.ascensionLevel >= 20 && modify;
    }
    
    public void assign(AbstractMonster m) {
        this.m = m;
    }
    
    public MonsterEditor modify(boolean modify) {
        this.modify = modify;
        return this;
    }
    
    public void putBool(String key, boolean value) {
        if (boolMap.containsKey(key)) {
            throw new IllegalArgumentException("KEY [" + key + "] HAS BEEN ASSIGNED WITH VALUE BEFORE");
        }
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
        if (intMap.containsKey(key)) {
            throw new IllegalArgumentException("KEY [" + key + "] HAS BEEN ASSIGNED WITH VALUE BEFORE");
        }
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
        if (objMap.containsKey(key)) {
            throw new IllegalArgumentException("KEY [" + key + "] HAS BEEN ASSIGNED WITH VALUE BEFORE");
        }
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
    
    protected static void InjectMethods(ClassPool pool, Class<? extends AbstractMonster> mClz, String editClz, boolean hasExtraFunctions) {
        if (mClz == null) {
            throw new NullPointerException("MONSTER CLASS IS NULL");
        }
        try {
            String getEditor = SETool.class.getName() + ".GetEditor(this)";
            CtClass mClass = pool.getCtClass(mClz.getName());
            mClass.defrost();
            CtMethod takeTurnMethod = mClass.getDeclaredMethod("takeTurn");
            CtMethod getMoveMethod = mClass.getDeclaredMethod("getMove");
            injectMethod(takeTurnMethod, pool, "takeTurn()");
            injectMethod(getMoveMethod, pool, "getMove($1)");
            try {
                CtMethod usePreBattleMethod = mClass.getDeclaredMethod("usePreBattleAction");
                injectMethod(usePreBattleMethod, pool, "usePreBattle()");
            } catch (Exception ignore) {
                CtMethod newUsePreBattle = CtNewMethod.make(CtClass.voidType, "usePreBattleAction", new CtClass[0], null,
                        "{if(" + getEditor +".canModify()){" + getEditor + ".usePreBattle();}}", mClass);
                mClass.addMethod(newUsePreBattle);
            }
            if (hasExtraFunctions) {
                try {
                    CtMethod updateMethod = mClass.getDeclaredMethod("update");
                    updateMethod.insertAfter("{" + getEditor + ".postUpdate();}");
                } catch (Exception ignore) {
                    CtMethod newUpdateMethod = CtNewMethod.make(CtClass.voidType, "update", new CtClass[0], null,
                            "{super.update();" + getEditor + ".postUpdate();}", mClass);
                    mClass.addMethod(newUpdateMethod);
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
        void seInit(AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface UsePreBattleFunc {
        boolean usePreBattle(AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface TakeTurnFunc {
        boolean takeTurn(AbstractMonster m);
    }
    
    @FunctionalInterface
    public interface GetMoveFunc {
        boolean getMove(AbstractMonster m, int roll);
    }
    
    public interface PostUpdateFunc {
        void postUpdate(AbstractMonster m);
    }
}