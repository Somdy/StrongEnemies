package rs.winds.actions.unique;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.combat.PowerDebuffEffect;
import rs.lazymankits.abstracts.LMCustomGameAction;
import rs.winds.powers.SECuriosityPower;

import java.util.Collections;

public class ApplySECuriosityAction extends LMCustomGameAction {
    
    public ApplySECuriosityAction(AbstractCreature target, AbstractCreature source, int amount) {
        setValues(target, source, amount);
    }
    
    @Override
    public void update() {
        if (target != null && !target.isDeadOrEscaped()) {
            SECuriosityPower power = new SECuriosityPower(target, amount);
            target.useFastShakeAnimation(0.5F);
            if (!target.hasPower(SECuriosityPower.ID)) {
                target.powers.add(power);
                Collections.sort(target.powers);
                power.onInitialApplication();
                power.flash();
                effectToList(new PowerDebuffEffect(target.hb.cX - target.animX, target.hb.cY + target.hb.height / 2F, power.name));
            } else {
                AbstractPower oldPower = target.getPower(SECuriosityPower.ID);
                if (oldPower != null) {
                    oldPower.stackPower(amount);
                    oldPower.flash();
                    oldPower.updateDescription();
                    effectToList(new PowerDebuffEffect(target.hb.cX - target.animX, target.hb.cY + target.hb.height / 2F,
                            "+" + amount + " " + power.name));
                }
            }
            AbstractDungeon.onModifyPower();
        }
        isDone = true;
    }
}