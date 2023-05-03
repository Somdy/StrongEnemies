package rs.winds.cards.colorless;

import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;
import rs.winds.abstracts.AbstractSECard;
import rs.winds.core.King;

public class SpecialTreeAttack extends AbstractSECard {
    public static final String ID = King.MakeID("SpecialTreeAttack");
    
    public SpecialTreeAttack() {
        super(ID, "sta", 1, CardType.ATTACK, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.ENEMY);
        exhaust = true;
    }
    
    @Override
    protected void play(AbstractCreature s, AbstractCreature t) {
        addToBot(new VFXAction(new WeightyImpactEffect(t.hb.cX, t.hb.cY)));
        addToBot(new RemoveAllPowersAction(t, false));
        addToBot(new DamageAction(t, new DamageInfo(s, 500, DamageInfo.DamageType.THORNS)));
    }
    
    @Override
    public void upgrade() {}
}