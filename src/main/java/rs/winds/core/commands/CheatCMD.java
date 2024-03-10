package rs.winds.core.commands;

import basemod.DevConsole;
import basemod.devcommands.ConsoleCommand;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.unique.IncreaseMaxHpAction;
import com.megacrit.cardcrawl.cards.purple.FlurryOfBlows;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import rs.lazymankits.utils.LMSK;

public class CheatCMD extends ConsoleCommand {
    public static boolean CHEATING = false;
    
    @Override
    protected void execute(String[] tokens, int i) {
        if (tokens.length < 1) {
            cmdEnergyHelp();
            return;
        }
//        if (tokens[1].equalsIgnoreCase("privatest") && tokens.length > 2) {
//            LMSK.AddToBot(new MakeTempCardInDrawPileAction(new FlurryOfBlows(), 3, true, true));
//            DevConsole.infiniteEnergy = !DevConsole.infiniteEnergy;
//            if (DevConsole.infiniteEnergy) {
//                AbstractDungeon.player.gainEnergy(9999);
//            }
//            CHEATING = true;
//        } else {
//            cmdEnergyHelp();
//        }
    }
    
    @Override
    protected void errorMsg() {
        cmdEnergyHelp();
    }
    
    private static void cmdEnergyHelp() {
        DevConsole.couldNotParse();
        DevConsole.log("no available codes");
    }
}
