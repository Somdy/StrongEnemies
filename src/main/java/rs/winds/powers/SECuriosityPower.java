package rs.winds.powers;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.GainStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;

import java.util.Map;

public class SECuriosityPower extends AbstractSEPower {
    public static final String ID = King.MakeID("CuriosityPower");
    
    public SECuriosityPower(AbstractCreature owner, int amount) {
        super(ID, "curiosity", PowerType.DEBUFF, owner);
        setValues(amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, AbstractCard.CardType.POWER)) {
            addToBot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, -amount)));
            addToBot(new ApplyPowerAction(owner, owner, new GainStrengthPower(owner, amount)));
            addToBot(new ApplyPowerAction(owner, owner, new FocusPower(owner, -amount)));
            addToBot(new ApplyPowerAction(owner, owner, new GainFocusPower(owner, amount)));
        }
    }
    
    @Override
    public void stackPower(int stackAmount) {
        if (stackAmount < 0) return;
        super.stackPower(stackAmount);
        SECuriosityMark mark = CuriosityField.MarkField.get(owner);
        if (mark != null) {
            mark.amount = this.amount;
        } else {
            mark = new SECuriosityMark(owner, amount, true);
            CuriosityField.MarkField.set(owner, mark);
        }
    }
    
    @Override
    public void reducePower(int reduceAmount) {
        if (reduceAmount >= amount) return;
        super.reducePower(reduceAmount);
    }
    
    @Override
    public void onInitialApplication() {
        SECuriosityMark mark = new SECuriosityMark(owner, amount, true);
        CuriosityField.MarkField.set(owner, mark);
    }
    
    @Override
    public void onVictory() {
        SECuriosityMark mark = CuriosityField.MarkField.get(owner);
        if (mark != null) {
            CuriosityField.MarkField.set(owner, null);
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new SECuriosityPower(owner, amount);
    }
    
    private boolean removable() {
        return owner.isDeadOrEscaped() || AbstractDungeon.getMonsters().areMonstersBasicallyDead();
    }
    
    public static class SECuriosityMark {
        public AbstractCreature owner;
        public int amount;
        public boolean marked;
    
        public SECuriosityMark(AbstractCreature owner, int amount, boolean marked) {
            this.owner = owner;
            this.amount = amount;
            this.marked = marked;
        }
        
        public boolean removable() {
            return owner.isDeadOrEscaped() || AbstractDungeon.getMonsters().areMonstersBasicallyDead();
        }
    }
    
    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CLASS)
    public static class CuriosityField {
        public static SpireField<SECuriosityMark> MarkField = new SpireField<>(() -> null);
    }
    
    @SpirePatch2(clz = RemoveSpecificPowerAction.class, method = "update")
    public static class RemoveSpecificPowerActionPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractGameAction __instance, String ___powerToRemove, AbstractPower ___powerInstance) {
            if (ID.equals(___powerToRemove) || ___powerInstance instanceof SECuriosityPower) {
                __instance.isDone = true;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
}