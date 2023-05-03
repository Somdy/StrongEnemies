package rs.winds.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.GameOverStat;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import javassist.CtBehavior;
import rs.winds.dungeons.CityDepths;
import rs.winds.dungeons.RootDepths;

import java.util.ArrayList;

public class VictoryScreenStatsPatch {
    private static final int KING_SCORE = 250;
    private static final int TREE_SCORE = 300;
    @SpirePatch(clz = VictoryScreen.class, method = "createGameOverStats")
    public static class AddBossKingStats {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(VictoryScreen _inst, ArrayList<GameOverStat> ___stats) {
            if (CardCrawlGame.dungeon instanceof CityDepths)
                ___stats.add(new GameOverStat("击败国王", "击败国王", Integer.toString(KING_SCORE)));
            if (CardCrawlGame.dungeon instanceof RootDepths)
                ___stats.add(new GameOverStat("风暴击倒大树", "风暴击倒大树", Integer.toString(TREE_SCORE)));
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
            if (GameOverScreen.isVictory) {
                if (CardCrawlGame.dungeon instanceof CityDepths)
                    p += KING_SCORE;
                if (CardCrawlGame.dungeon instanceof RootDepths)
                    p += TREE_SCORE;
            }
            return p;
        }
    }
}