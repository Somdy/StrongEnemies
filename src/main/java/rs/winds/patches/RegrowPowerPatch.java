package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RegrowPower;
import rs.winds.monsters.MonsterEditor;
import rs.winds.monsters.SETool;

@SpirePatch(clz = RegrowPower.class, method = "updateDescription")
public class RegrowPowerPatch {
    @SpirePostfixPatch
    public static void Postfix(AbstractPower _inst) {
        if (_inst.owner instanceof AbstractMonster) {
            MonsterEditor editor = SETool.GetEditor((AbstractMonster) _inst.owner);
            if (editor.canModify()) {
                _inst.description = _inst.description.replace("50", "100");
            }
        }
    }
}