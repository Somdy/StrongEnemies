package rs.winds.powers.guniques;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.core.King;
import rs.winds.monsters.ending.EvilGod;

public class TrueGodPower extends AbstractSEPower {
    public static final String ID = King.MakeID("TrueGodPower");
    
    public TrueGodPower(EvilGod owner) {
        super(ID, "powerstealer", PowerType.BUFF, owner);
        updateDescription();
    }
    
    private void onInstantlyKilled() {
        if (owner instanceof EvilGod)
            ((EvilGod) owner).onInstantlyKilled();
    }
    
    @Override
    public AbstractPower makeCopy() {
        return null;
    }
    
    @SpirePatch(clz = InstantKillAction.class, method = "update")
    public static class InstantKillActionPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractGameAction _inst) {
            if (_inst.target != null) {
                AbstractPower p = _inst.target.getPower(ID);
                if (p instanceof TrueGodPower) {
                    ((TrueGodPower) p).onInstantlyKilled();
                }
            }
        }
    }
}
