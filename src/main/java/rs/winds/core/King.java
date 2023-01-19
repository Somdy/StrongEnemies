package rs.winds.core;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.MonsterInfo;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.monsters.beyond.OrbWalker;
import com.megacrit.cardcrawl.monsters.beyond.Spiker;
import com.megacrit.cardcrawl.monsters.city.*;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.LMDebug;
import rs.winds.events.ColosseumSE;
import rs.winds.monsters.beyond.DarklingSE;
import rs.winds.monsters.beyond.TestMonster;
import rs.winds.monsters.city.ByrdSE;
import rs.winds.monsters.city.WrithingMassSE;
import rs.winds.monsters.exordium.RedLouseSE;
import rs.winds.monsters.exordium.TransientSE;
import rs.winds.patches.SEEnums;
import rs.winds.relics.SERBarricade;
import rs.winds.rewards.ApoReward;

import java.util.HashMap;
import java.util.Map;

@SpireInitializer
public class King implements EditStringsSubscriber, PostInitializeSubscriber, StartGameSubscriber, CustomSavable<Map<String, String>>, 
        EditRelicsSubscriber {
    public static final String MOD_ID = "StrongEnemies";
    public static final String MOD_NAME = "Strong Enemies";
    public static final String[] AUTHORS = {"Somdy", "The World"};
    
    private static final String MOD_PREFIX = "strong";
    
    public static boolean ApoDropped = false;
    public static boolean ShieldAndSpearExisting = false;
    
    public static void initialize() {
        King instance = new King();
        BaseMod.subscribe(instance);
        BaseMod.addSaveField(MOD_ID, instance);
    }
    
    public static void Log(Object what) {
        LMDebug.deLog(King.class, "===[STRONG ENEMIES]> " + what);
    }
    
    public static void PatchLog(Object what) {
        System.out.println("[SEPATCH]> " + what);
    }
    
    @NotNull
    public static String getSupLang() {
        switch(Settings.language) {
            case ZHS:
                return "zhs";
            case ZHT:
                return "zht";
            default:
                return "eng";
        }
    }
    
    @NotNull
    public static String GetPrefix() {
        return MOD_PREFIX + ":";
    }
    
    @NotNull
    public static String MakeID(String id) {
        return GetPrefix() + id;
    }
    
    @NotNull
    public static String RmPrefix(@NotNull String idWithPrefix) {
        if (idWithPrefix.startsWith(GetPrefix()))
            return idWithPrefix.replaceFirst(GetPrefix(), "");
        return idWithPrefix;
    }
    
    public static UIStrings UIStrings(@NotNull String id) {
        if (!id.startsWith(GetPrefix()))
            id = MakeID(id);
        return CardCrawlGame.languagePack.getUIString(id);
    }
    
    public static PowerStrings PowerStrings(@NotNull String id) {
        if (!id.startsWith(GetPrefix()))
            id = MakeID(id);
        return CardCrawlGame.languagePack.getPowerStrings(id);
    }
    
    public static MonsterStrings MonsterStrings(@NotNull String id) {
        return CardCrawlGame.languagePack.getMonsterStrings(id);
    }
    
    @Override
    public Map<String, String> onSave() {
        Map<String, String> map = new HashMap<>();
        map.put("ApoDropped", String.valueOf(ApoDropped));
        map.put("ShieldAndSpearExisting", String.valueOf(ShieldAndSpearExisting));
        return map;
    }
    
    @Override
    public void onLoad(Map<String, String> map) {
        if (map != null) {
            map.forEach((k, v) -> {
                if ("ApoDropped".equals(k))
                    ApoDropped = Boolean.parseBoolean(v);
                if ("ShieldAndSpearExisting".equals(k))
                    ShieldAndSpearExisting = Boolean.parseBoolean(v);
            });
        }
    }
    
    @Override
    public void receiveEditStrings() {
        String lang = getSupLang();
        BaseMod.loadCustomStringsFile(PowerStrings.class, "SEAssets/locals/" + lang + "/powers.json");
        BaseMod.loadCustomStringsFile(CardStrings.class, "SEAssets/locals/" + lang + "/cards.json");
        BaseMod.loadCustomStringsFile(RelicStrings.class, "SEAssets/locals/" + lang + "/relics.json");
    }
    
    @Override
    public void receivePostInitialize() {
        BaseMod.registerCustomReward(SEEnums.ApoRewardType, load -> new ApoReward(), 
                save -> new RewardSave(save.type.toString(), "SE_ApoReward"));
        
        BaseMod.addEvent(new AddEventParams.Builder(ColosseumSE.ID, ColosseumSE.class)
                .eventType(EventUtils.EventType.OVERRIDE).overrideEvent(Colosseum.ID)
                .bonusCondition(() -> AbstractDungeon.ascensionLevel >= 20).dungeonID(TheCity.ID).endsWithRewardsUI(true).create());
        BaseMod.addEvent(new AddEventParams.Builder(ColosseumSE.ID, ColosseumSE.class)
                .eventType(EventUtils.EventType.NORMAL).spawnCondition(() -> AbstractDungeon.ascensionLevel >= 20)
                .bonusCondition(() -> AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.y > AbstractDungeon.map.size() / 2)
                .dungeonID(Exordium.ID).endsWithRewardsUI(true).create());
        BaseMod.addEvent(new AddEventParams.Builder(ColosseumSE.ID, ColosseumSE.class)
                .eventType(EventUtils.EventType.NORMAL).spawnCondition(() -> AbstractDungeon.ascensionLevel >= 20)
                .bonusCondition(() -> AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.y > AbstractDungeon.map.size() / 2)
                .dungeonID(TheBeyond.ID).endsWithRewardsUI(true).create());
        addMonsters();
    }
    
    private static void addMonsters() {
        // the bottom
        BaseMod.addStrongMonsterEncounter(Exordium.ID, new MonsterInfo(Encounter.LOOTER_AND_FUNGI, 2.071F));
        BaseMod.addMonster(Encounter.LOOTER_AND_FUNGI, () -> Populate(new Looter(-400F, 30F), new FungiBeast(-40F, 20F)));
        BaseMod.addEliteEncounter(Exordium.ID, new MonsterInfo(Encounter.TRANSIENT_EXORDIUM, 1F));
        BaseMod.addMonster(Encounter.TRANSIENT_EXORDIUM, () -> Populate(new TransientSE()));
        BaseMod.addMonster(Encounter.THREE_LOUSE_ENC, () -> Populate(new LouseDefensive(-350F, 25F), new RedLouseSE(-125F, 10F),
                new LouseNormal(80F, 30F)));
    
        // the city
        BaseMod.addEliteEncounter(TheCity.ID, new MonsterInfo(MonsterHelper.GREMLIN_LEADER_ENC, 1F));
        ReflectionHacks.RStaticMethod spawnGremlin = ReflectionHacks.privateStaticMethod(MonsterHelper.class,
                "spawnGremlin", float.class, float.class);
        BaseMod.addMonster(MonsterHelper.GREMLIN_LEADER_ENC, () -> Populate(spawnGremlin
                .invoke(new Object[]{GremlinLeader.POSX[2], GremlinLeader.POSY[0]}), spawnGremlin
                .invoke(new Object[]{GremlinLeader.POSX[0], GremlinLeader.POSY[1]}), spawnGremlin
                .invoke(new Object[]{GremlinLeader.POSX[1], GremlinLeader.POSY[2]}), new GremlinLeader()));
        BaseMod.addStrongMonsterEncounter(TheCity.ID, new MonsterInfo(Encounter.SPIKER_GUARDIAN_ORB, 9.667F));
        BaseMod.addMonster(Encounter.SPIKER_GUARDIAN_ORB, () -> Populate(new Spiker(-480F, 6F), new BronzeOrb(-220F, -6F, 0){
            @Override
            public void takeTurn() {
                if (this.nextMove == 2) {
                    AbstractMonster m = AbstractDungeon.getRandomMonster();
                    if (m == null || m.isDeadOrEscaped()) m = this;
                    addToBot(new GainBlockAction(m, this, 12));
                    addToBot(new RollMoveAction(this));
                } else {
                    super.takeTurn();
                }
            }
        }, new SphericGuardian(110F, 10F)));
        BaseMod.addStrongMonsterEncounter(TheCity.ID, new MonsterInfo(Encounter.SUPER_JAW_WORM, 9.667F));
        BaseMod.addMonster(Encounter.SUPER_JAW_WORM, () -> Populate(new JawWorm(0F, 25F){
            @Override
            public void usePreBattleAction() {
                super.usePreBattleAction();
                addToBot(new GainBlockAction(this, this, 50));
            }
            {
                this.powers.add(new BarricadePower(this));
                this.powers.add(new StrengthPower(this, 5));
                this.powers.add(new RegenerateMonsterPower(this, 5));
                this.powers.add(new PlatedArmorPower(this, 5));
            }
        }));
        BaseMod.addEliteEncounter(TheCity.ID, new MonsterInfo(Encounter.WRITHING_MASS_CITY, 1F));
        BaseMod.addMonster(Encounter.WRITHING_MASS_CITY, () -> Populate(new WrithingMassSE()));
        BaseMod.addMonster(Encounter.THREE_BYRDS_ENC, () -> Populate(new Byrd(-360F, MathUtils.random(25F, 70F)), 
                new ByrdSE(-80F, MathUtils.random(25F, 70F)), new Byrd(200F, MathUtils.random(25F, 70F))));
    
        // the beyond
        BaseMod.addStrongMonsterEncounter(TheBeyond.ID, new MonsterInfo(Encounter.SNECKO_AND_CHOSEN, 1.6F));
        BaseMod.addMonster(Encounter.SNECKO_AND_CHOSEN, () -> Populate(new Snecko(-400F, 20F), new Chosen(-50F, 10F)));
        BaseMod.addStrongMonsterEncounter(TheBeyond.ID, new MonsterInfo(Encounter.SPIRE_CREATIONS, 1.6F));
        BaseMod.addMonster(Encounter.SPIRE_CREATIONS, () -> Populate(new Sentry(-580F, MathUtils.random(-10F, 15F)),
                new SphericGuardian(-280F, MathUtils.random(-15F, 10F)), new OrbWalker(-150F, MathUtils.random(0F, 25F))));
        BaseMod.addStrongMonsterEncounter(TheBeyond.ID, new MonsterInfo(Encounter.SUPER_GUARDIAN, 1.6F));
        BaseMod.addMonster(Encounter.SUPER_GUARDIAN, () -> Populate(new SphericGuardian(){
            @Override
            public void usePreBattleAction() {
                addToBot(new ApplyPowerAction(this, this, new ArtifactPower(this, 5)));
                addToBot(new GainBlockAction(this, this, 100));
                addToBot(new ApplyPowerAction(this, this, new MetallicizePower(this, 10)));
                addToBot(new ApplyPowerAction(this, this, new BarricadePower(this)));
                addToBot(new ApplyPowerAction(this, this, new RitualPower(this, 2, false){
                    @Override
                    public void atEndOfRound() {
                        flash();
                        addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, this.amount)));
                    }
                }));
            }
        }));
        BaseMod.addStrongMonsterEncounter(TheBeyond.ID, new MonsterInfo(Encounter.TEST_MONSTER, 4.27F));
        BaseMod.addMonster(Encounter.TEST_MONSTER, () -> Populate(new TestMonster(0F, 0F)));
        BaseMod.addMonster(Encounter.THREE_DARKLINGS_ENC, () -> Populate(new Darkling(-440F, 10F), new DarklingSE(-140F, 30F), 
                new Darkling(180F, -5F)));
    }
    
    @Override
    public void receiveStartGame() {
        if (!CardCrawlGame.loadingSave) {
            if (ApoDropped) ApoDropped = false;
            if (ShieldAndSpearExisting) ShieldAndSpearExisting = false;
        }
    }
    
    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new SERBarricade(), RelicType.SHARED);
    }
    
    @NotNull
    public static MonsterGroup Populate(AbstractMonster... monsters) {
        return new MonsterGroup(monsters);
    }
    
    public static class Encounter {
        public static final String LOOTER_AND_FUNGI = "SE Looter And Fungi";
        public static final String SPIKER_GUARDIAN_ORB = "SE Spiker Guardian Orb";
        public static final String SUPER_JAW_WORM = "SE Super Jaw Worm";
        public static final String SNECKO_AND_CHOSEN = "SE Snecko And Chosen";
        public static final String SPIRE_CREATIONS = "SE Spire Creations";
        public static final String SUPER_GUARDIAN = "SE Super Guardian";
        public static final String TRANSIENT_EXORDIUM = "SE Transient";
        public static final String WRITHING_MASS_CITY = "SE Writhing Mass";
        public static final String TEST_MONSTER = "SE Test Monster";
        public static final String THREE_LOUSE_ENC = "SE 3 Louses";
        public static final String THREE_BYRDS_ENC = "SE 3 Byrds";
        public static final String THREE_DARKLINGS_ENC = "SE 3 Darklings";
    }
}