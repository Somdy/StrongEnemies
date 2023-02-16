package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.GameOverStat;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import javassist.CtBehavior;
import rs.winds.dungeons.CityDepths;

import java.util.ArrayList;

public class VictoryScreenStatsPatch {
    @SpirePatch(clz = VictoryScreen.class, method = "createGameOverStats")
    public static class AddBossKingStats {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(VictoryScreen _inst, ArrayList<GameOverStat> ___stats) {
            if (CardCrawlGame.dungeon instanceof CityDepths)
                ___stats.add(new GameOverStat("击败国王", "击败国王", Integer.toString(250)));
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.NewExprMatcher matcher = new Matcher.NewExprMatcher(GameOverStat.class);
                int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
                return new int[]{lines[lines.length - 2]};
            }
        }
    }
    @SpirePatch(clz = GameOverScreen.class, method = "checkScoreBonus")
    public static class AddBossKingScores {
        @SpirePostfixPatch
        public static int Postfix(int p, boolean v) {
            if (GameOverScreen.isVictory && CardCrawlGame.dungeon instanceof CityDepths)
                p += 250;
            return p;
        }
    }
}