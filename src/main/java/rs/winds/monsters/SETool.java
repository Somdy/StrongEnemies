package rs.winds.monsters;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.random.Random;
import org.jetbrains.annotations.NotNull;
import rs.winds.cards.AbstractCardPatch;
import rs.winds.cards.CardEditor;

public interface SETool {
    
    static boolean SuchCalledPhysicalDamage(DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL;
    }
    
    static boolean SoCalledPhysicalDamage(DamageInfo info) {
        return info.owner != null && info.type == DamageInfo.DamageType.NORMAL;
    }
    
    static boolean SoCalledSpellDamage(DamageInfo info) {
        return !SoCalledPhysicalDamage(info);
    }
    
    static Random MonsterAIRng() {
        return AbstractDungeon.aiRng;
    }
    
    static CardEditor GetEditor(AbstractCard c) {
        return AbstractCardPatch.TrackerField.cTracker.get(c);
    }
    
    @NotNull
    static CardEditor GetModifierEditor(AbstractCard c) {
        CardEditor tracker = AbstractCardPatch.TrackerField.cTracker.get(c);
        if (!tracker.isAssigned())
            tracker.assign(c);
        return tracker.modify(true);
    }
    
    static void updateCardDesc(AbstractCard card, String desc) {
        updateCardText(card, null, desc);
    }
    
    static void updateCardText(@NotNull AbstractCard card, String name, String desc) {
        if (name != null) {
            card.name = name;
            getMethod(AbstractCard.class, "initializeTitle").invoke(card);
        }
        if (desc != null) {
            card.rawDescription = desc;
            card.initializeDescription();
        }
    }
    
    static void upgradeCardName(AbstractCard card) {
        getMethod(AbstractCard.class, "upgradeName").invoke(card);
    }
    
    static void upgradeCardMagic(AbstractCard card, int amount) {
        SETool.getMethod(AbstractCard.class, "upgradeMagicNumber", int.class).invoke(card, amount);
    }
    
    static void upgradeCardDamage(AbstractCard card, int amount) {
        SETool.getMethod(AbstractCard.class, "upgradeDamage", int.class).invoke(card, amount);
    }
    
    static void upgradeCardBlock(AbstractCard card, int amount) {
        SETool.getMethod(AbstractCard.class, "upgradeBlock", int.class).invoke(card, amount);
    }
    
    static void upgradeCardBaseCost(AbstractCard card, int cost) {
        SETool.getMethod(AbstractCard.class, "upgradeBaseCost", int.class).invoke(card, cost);
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
    
    static void getMove(AbstractMonster m, int roll) {
        getMethod(AbstractMonster.class, "getMove", int.class).invoke(m, roll);
    }
}