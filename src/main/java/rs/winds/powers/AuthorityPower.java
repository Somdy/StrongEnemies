package rs.winds.powers;

import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.SETool;
import rs.winds.monsters.citydepths.BossKing;

public class AuthorityPower extends AbstractSEPower {
    public static final String ID = King.MakeID("AuthorityPower");
    
    public AuthorityPower(BossKing owner) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (SETool.SoCalledPhysicalDamage(info) && SETool.MonsterAIRng().randomBoolean(0.5F)) {
            flash();
            addToTop(new DamageAction(LMSK.Player(), new DamageInfo(owner, 40, DamageInfo.DamageType.THORNS)));
        }
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
}