package rs.winds.patches.othermods.actlikeitp;


import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import rs.winds.dungeons.CityDepths;
import rs.winds.dungeons.RootDepths;

public class GetForkedKeepKeysPatch {
    @SpirePatch(cls = "actlikeit.events.GetForked", method = "nextDungeon", optional = true)
    public static class LeaveKeysForPlayers {
        public static void Postfix(String id) {
            if (CityDepths.ID.equals(id) || RootDepths.ID.equals(id)) {
                Settings.hasEmeraldKey = true;
                Settings.hasSapphireKey = true;
                Settings.hasRubyKey = true;
            }
        }
    }
}