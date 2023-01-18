package rs.winds.monsters;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.random.Random;
import org.jetbrains.annotations.NotNull;

public interface SETool {
    
    static Random MonsterAIRng() {
        return AbstractDungeon.aiRng;
    }
    
    static MonsterEditor GetEditor(AbstractMonster m) {
        return AbstractMonsterPatch.TrackerField.mTracker.get(m);
    }
    
    @NotNull
    static MonsterEditor GetModifierEditor(AbstractMonster m) {
        MonsterEditor tracker = AbstractMonsterPatch.TrackerField.mTracker.get(m);
        if (!tracker.isAssigned())
            tracker.assign(m);
        return tracker.modify(true);
    }
    
    static void setMonsterHp(AbstractMonster m, int min, int max) {
        ReflectionHacks.RMethod setHp = ReflectionHacks.privateMethod(AbstractMonster.class, 
                "setHp", int.class, int.class);
        setHp.invoke(m, min, max);
    }
    
    static void setMonsterHp(AbstractMonster m, int hp) {
        setMonsterHp(m, hp, hp);
    }
    
    static void setField(Class<?> clz, Object m, String field, Object fieldValue) {
        ReflectionHacks.setPrivate(m, clz, field, fieldValue);
    }
    
    static <T> T getField(Class<?> clz, Object m, String field) {
        return ReflectionHacks.getPrivate(m, clz, field);
    }
    
    static <T> T getStaticField(Class<?> clz, String field) {
        return ReflectionHacks.getPrivateStatic(clz, field);
    }
    
    static boolean getBool(Class<?> clz, Object m, String field) {
        return getField(clz, m, field);
    }
    
    static ReflectionHacks.RMethod getMethod(Class<?> clz, String method, Class<?>... paramTypes) {
        return ReflectionHacks.privateMethod(clz, method, paramTypes);
    }
    
    static boolean lastMove(@NotNull AbstractMonster m, byte move) {
        if (m.moveHistory.isEmpty()) {
            return false;
        } else {
            return m.moveHistory.get(m.moveHistory.size() - 1) == move;
        }
    }
    
    static boolean lastMoveBefore(@NotNull AbstractMonster m, byte move) {
        if (m.moveHistory.isEmpty() || m.moveHistory.size() < 2) {
            return false;
        } else {
            return m.moveHistory.get(m.moveHistory.size() - 2) == move;
        }
    }
    
    static boolean lastTwoMoves(@NotNull AbstractMonster m, byte move) {
        if (m.moveHistory.isEmpty() || m.moveHistory.size() < 2) {
            return false;
        } else {
            return m.moveHistory.get(m.moveHistory.size() - 1) == move && m.moveHistory.get(m.moveHistory.size() - 2) == move;
        }
    }
}