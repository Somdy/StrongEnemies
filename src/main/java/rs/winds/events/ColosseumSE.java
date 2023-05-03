package rs.winds.events;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.dungeons.TheEnding;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CultistMask;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.dungeons.CityDepths;
import rs.winds.dungeons.RootDepths;

public class ColosseumSE extends AbstractImageEvent {
    public static final String ID = King.MakeID(Colosseum.ID);
    private static final EventStrings strings = CardCrawlGame.languagePack.getEventString(Colosseum.ID);
    public static final String[] DESCRIPTIONS = strings.DESCRIPTIONS;
    public static final String[] OPTIONS = strings.OPTIONS;
    private static final byte intro = 0;
    private static final byte fight = 1;
    public static final byte leave = 2;
    public static final byte postcombat = 3;
    private byte phase;
    
    public ColosseumSE() {
        super(strings.NAME, DESCRIPTIONS[0], "images/events/colosseum.jpg");
        imageEventText.setDialogOption(OPTIONS[0]);
    }
    
    @Override
    protected void buttonEffect(int button) {
        switch (phase) {
            case intro:
                imageEventText.updateBodyText(DESCRIPTIONS[1] + DESCRIPTIONS[2] + 4200 + DESCRIPTIONS[3]);
                imageEventText.updateDialogOption(0, OPTIONS[1]);
                phase = fight;
                return;
            case fight:
                AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(getMonsters(0));
                AbstractDungeon.getCurrRoom().rewards.clear();
                AbstractDungeon.getCurrRoom().rewardAllowed = false;
                enterCombatFromImage();
                AbstractDungeon.lastCombatMetricKey = getMonsters(0);
                imageEventText.clearRemainingOptions();
                phase = postcombat;
                return;
            case postcombat:
                AbstractDungeon.getCurrRoom().rewardAllowed = true;
                if (button == 1) {
                    phase = leave;
                    AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(getMonsters(1));
                    AbstractDungeon.getCurrRoom().rewards.clear();
                    if (AbstractDungeon.actNum == 4) {
                        AbstractDungeon.getCurrRoom().addRelicToRewards(new CultistMask());
                    } else {
                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.RARE);
                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.UNCOMMON);
                    }
                    AbstractDungeon.getCurrRoom().addGoldToRewards(100);
                    AbstractDungeon.getCurrRoom().eliteTrigger = true;
                    enterCombatFromImage();
                    AbstractDungeon.lastCombatMetricKey = getMonsters(1);
                } else {
                    openMap();
                }
                return;
            case leave:
                openMap();
        }
    }
    
    private String getMonsters(int combat) {
        switch (AbstractDungeon.id) {
            case TheCity.ID:
                return combat == 0 ? MonsterHelper.THREE_BYRDS_ENC : King.Encounter.THREE_BYRDS_ENC;
            case TheBeyond.ID:
                return combat == 0 ? MonsterHelper.THREE_DARKLINGS_ENC : King.Encounter.THREE_DARKLINGS_ENC;
            case TheEnding.ID:
            case CityDepths.ID:
            case RootDepths.ID:
                return combat == 0 ? King.Encounter.TEST_MONSTER_EX : King.Encounter.TWO_TEST_MONSTER_EX;
            default:
                return combat == 0 ? MonsterHelper.THREE_LOUSE_ENC : King.Encounter.THREE_LOUSE_ENC;
        }
    }
    
    @Override
    public void reopen() {
        if (phase != leave) {
            AbstractDungeon.resetPlayer();
            LMSK.Player().drawX = Settings.WIDTH * 0.25F;
            LMSK.Player().preBattlePrep();
            enterImageFromCombat();
            imageEventText.updateBodyText(DESCRIPTIONS[4]);
            imageEventText.updateDialogOption(0, OPTIONS[2]);
            imageEventText.setDialogOption(OPTIONS[3]);
        }
    }
    
//    @SpirePatch2(clz = AbstractDungeon.class, method = "getEvent")
//    public static class ModifyChancePatch {
//        @SpireInsertPatch(locator = Locator.class, localvars = {"tmp"})
//        public static void Insert(ArrayList<String> tmp) {
//            String eventKey = getColosseumKey(tmp);
//            if (eventKey != null) {
//                int size = tmp.size();
//                int events = (int) tmp.stream().filter(ModifyChancePatch::isColosseum).count();
//                King.Log("Modifying " + events + " specified event [" + eventKey + "] chance in total " + size + " events");
//                float baseChance = MathUtils.random(0.33F, 0.335F) / 0.75F;
//                int need = MathUtils.ceil((baseChance * size - events) / (1 - baseChance));
//                for (int i = 0; i < need; i++) {
//                    tmp.add(eventKey);
//                }
//                King.Log(need + " specified events added");
//            }
//        }
//        private static String getColosseumKey(ArrayList<String> tmp) {
//            return tmp.stream().filter(ModifyChancePatch::isColosseum).findFirst().orElse(null);
//        }
//        private static boolean isColosseum(String key) {
//            return key.startsWith(ID);
//        }
//        private static class Locator extends SpireInsertLocator {
//            @Override
//            public int[] Locate(CtBehavior ctBehavior) throws Exception {
//                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(ArrayList.class, "get");
//                return LineFinder.findInOrder(ctBehavior, matcher);
//            }
//        }
//    }
    
    @SpirePatch2(clz = AbstractDungeon.class, method = "generateEvent")
    public static class GenerateEventPatch {
        @SpirePrefixPatch
        public static void Prefix(@ByRef float[] ___shrineChance) {
            if (___shrineChance[0] > 0.1F)
                ___shrineChance[0] = 0.1F;
        }
    }
}