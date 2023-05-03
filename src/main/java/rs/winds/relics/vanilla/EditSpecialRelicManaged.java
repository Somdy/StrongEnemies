package rs.winds.relics.vanilla;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CultistMask;
import javassist.*;
import rs.lazymankits.utils.LMSK;

import java.util.HashMap;
import java.util.Map;

public class EditSpecialRelicManaged {
    
    @SpirePatch(clz = CultistMask.class, method = "getUpdatedDescription")
    public static class CultistMaskSE {
        @SpirePostfixPatch
        public static String Postfix(String _result, AbstractRelic _inst) {
            _result = "你可以借助邪神的力量，kill all一次。";
            return _result;
        }
        private static final Map<CultistMask, Boolean> clickStarted = new HashMap<>();
        private static final Map<CultistMask, Boolean> clicked = new HashMap<>();
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctBehavior.getDeclaringClass();
            CtMethod update = CtNewMethod.make(CtClass.voidType, "update", null, null, 
                    "{super.update();if(" + CultistMaskSE.class.getName() + ".UpdateLogic($0)){"
                            + CultistMaskSE.class.getName() + ".OnRightClick($0);};}", ctClass);
            ctClass.addMethod(update);
            CtMethod setcounter = CtNewMethod.make(CtClass.voidType, "setCounter", new CtClass[]{CtClass.intType}, null, 
                    "{if($1==-2){$0.usedUp();$0.counter=-2;}}", ctClass);
            ctClass.addMethod(setcounter);
            CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);
            constructor.insertAfter("{flavorText=\"头套是邪教的象征，带上头套可能会遇到好事（坏事）\";}");
//            CtMethod abs = ctClass.getDeclaredMethod("atBattleStart");
//            abs.insertAfter("{$0.usedUp=false;$0.grayscale=false;}");
        }
        public static boolean UpdateLogic(CultistMask _inst) {
            if (_inst.isObtained && _inst.hb != null && _inst.hb.hovered && InputHelper.justClickedRight) {
                clickStarted.put(_inst, true);
            }
            if (clickStarted.containsKey(_inst) && clickStarted.get(_inst) && InputHelper.justReleasedClickRight) {
                if (_inst.hb != null && _inst.hb.hovered)
                    clicked.put(_inst, true);
                clickStarted.remove(_inst);
            }
            if (clicked.containsKey(_inst) && clicked.get(_inst)) {
                clicked.remove(_inst);
                return true;
            }
            return false;
        }
        public static void OnRightClick(CultistMask _inst) {
            if (!_inst.usedUp && _inst.counter == -1) {
                for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> true)) {
                    LMSK.AddToBot(new InstantKillAction(m));
                }
                _inst.setCounter(-2);
            }
        }
    }
}