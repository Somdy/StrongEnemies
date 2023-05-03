package rs.winds.powers;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BufferPower;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import javassist.CtBehavior;
import rs.lazymankits.abstracts.DamageInfoTag;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMDamageInfoHelper;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SEVMonsterEditorManaged;

public class TreePoisonPower extends AbstractSEPower {
    public static final String ID = King.MakeID("TreePoisonPower");
    private static final DamageInfoTag IGNORE_INTANGIBLE = new DamageInfoTag("SE_TREE_POISON_INTANGIBLE_TAG");
    private static final DamageInfoTag IGNORE_BUFFER = new DamageInfoTag("SE_TREE_POISON_BUFFER_TAG");
    
    public TreePoisonPower(AbstractCreature owner, AbstractCreature source, int amount) {
        super(ID, "poison", PowerType.DEBUFF, owner);
        setValues(source, amount);
        preloadString(s -> setAmtValue(0, this.amount));
        updateDescription();
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (amount > 0) {
            addToBot(new DamageAction(owner, LMDamageInfoHelper.Create(source, amount, DamageInfo.DamageType.THORNS, 
                    King.IGNORE_INTANGIBLE.cpy(), IGNORE_BUFFER.cpy()), AbstractGameAction.AttackEffect.POISON));
            addToBot(new ReducePowerAction(owner, owner, this, 1));
        } else {
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new TreePoisonPower(owner, source, amount);
    }
    
    @SpirePatch2(clz = BufferPower.class, method = "onAttackedToChangeDamage")
    public static class IgnoreBufferPatch {
        @SpirePrefixPatch
        public static SpireReturn<Integer> Prefix(BufferPower __instance, DamageInfo info, int damageAmount) {
            if (LMDamageInfoHelper.HasTag(info, IGNORE_BUFFER)) {
                return SpireReturn.Return(damageAmount);
            }
            return SpireReturn.Continue();
        }
    }
}