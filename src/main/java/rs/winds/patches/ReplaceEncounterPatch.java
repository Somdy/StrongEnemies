package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import com.megacrit.cardcrawl.monsters.city.Chosen;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

public class ReplaceEncounterPatch {
    @SpirePatch(clz = MonsterHelper.class, method = "getEncounter")
    public static class GetEncounterPatch {
        @SpirePrefixPatch
        public static SpireReturn<MonsterGroup> PrefixGet(String key) {
            if (MonsterHelper.AWAKENED_ENC.equals(key) && LMSK.AscnLv() >= 20) {
                return SpireReturn.Return(King.Populate(new Chosen(-590F, 10F){
                    @Override
                    public void usePreBattleAction() {
                        addToBot(new ApplyPowerAction(this, this, new PlatedArmorPower(this, 10)));
                        super.usePreBattleAction();
                    }
                }, new Cultist(-298F, -10F, false){
                    @Override
                    public void usePreBattleAction() {
                        addToBot(new ApplyPowerAction(this, this, new PlatedArmorPower(this, 10)));
                        super.usePreBattleAction();
                    }
                }, new AwakenedOne(100F, 15F){
                    @Override
                    public void usePreBattleAction() {
                        addToBot(new ApplyPowerAction(this, this, new PlatedArmorPower(this, 10)));
                        super.usePreBattleAction();
                    }
                }));
            }
            return SpireReturn.Continue();
        }
    }
}