package rs.winds.core;

import actlikeit.dungeons.CustomDungeon;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.*;
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
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.LMDebug;
import rs.lazymankits.abstracts.DamageInfoTag;
import rs.lazymankits.utils.LMSK;
import rs.winds.abstracts.AbstractSEPower;
import rs.winds.cards.silent.AlwaysPrepared;
import rs.winds.cards.watcher.WatcherFour;
import rs.winds.cards.watcher.WatcherOne;
import rs.winds.cards.watcher.WatcherThree;
import rs.winds.cards.watcher.WatcherTwo;
import rs.winds.dungeons.CityDepths;
import rs.winds.dungeons.RootDepths;
import rs.winds.events.ColosseumSE;
import rs.winds.monsters.beyond.DarklingSE;
import rs.winds.monsters.beyond.TestMonster;
import rs.winds.monsters.beyond.TestMonsterEx;
import rs.winds.monsters.city.ByrdSE;
import rs.winds.monsters.city.WrithingMassSE;
import rs.winds.monsters.citydepths.*;
import rs.winds.monsters.ending.EvilGod;
import rs.winds.monsters.exordium.RedLouseSE;
import rs.winds.monsters.exordium.TransientSE;
import rs.winds.monsters.rootdepths.SnakePlantGreenRD;
import rs.winds.monsters.rootdepths.SnakePlantPurpleRD;
import rs.winds.monsters.rootdepths.TheHolyTree;
import rs.winds.patches.SEEnums;
import rs.winds.powers.SECuriosityPower;
import rs.winds.powers.guniques.GodVisionPower;
import rs.winds.relics.SERBarricade;
import rs.winds.relics.SERInvitation;
import rs.winds.rewards.ApoReward;
import rs.winds.rewards.NightmareReward;

import java.util.HashMap;
import java.util.Map;

@SpireInitializer
public class King implements EditStringsSubscriber, PostInitializeSubscriber, StartGameSubscriber, CustomSavable<Map<String, String>>, 
        EditRelicsSubscriber, AddAudioSubscriber, PostExhaustSubscriber, EditCardsSubscriber, PostDungeonUpdateSubscriber, 
        OnPowersModifiedSubscriber, OnPlayerTurnStartPostDrawSubscriber {
    public static final String MOD_ID = "StrongEnemies";
    public static final String MOD_NAME = "Strong Enemies";
    public static final String[] AUTHORS = {"Somdy", "The World"};
    
    private static final String MOD_PREFIX = "strong";
    public static final int INTANGIBLE_FINAL_DAMAGE = 3;
    public static boolean ApoDropped = false;
    public static boolean ShieldAndSpearExisting = false;
    public static boolean DepthsElitesAllFinished = true;
    
    public static final DamageInfoTag IGNORE_INTANGIBLE = new DamageInfoTag("SE_IGNORE_INTANGIBLE_TAG");
    
    public static int PlayerBarricadeBlockLastTurn = 0;
    
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
    
    public static CardStrings CardStrings(@NotNull String id) {
        return CardCrawlGame.languagePack.getCardStrings(id);
    }
    
    public static String CardImage(String imgName) {
        String path = "SEAssets/images/cards/" + imgName + ".png";
        if (Gdx.files.internal(path).exists())
            return path;
        return "SEAssets/images/cards/heartofspire.png";
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
        BaseMod.loadCustomStringsFile(MonsterStrings.class, "SEAssets/locals/" + lang + "/monsters.json");
    }
    
    @Override
    public void receivePostInitialize() {
        BaseMod.registerCustomReward(SEEnums.ApoRewardType, load -> new ApoReward(), 
                save -> new RewardSave(save.type.toString(), "SE_ApoReward"));
        BaseMod.registerCustomReward(SEEnums.NightmareRewardType, load -> new NightmareReward(), 
                save -> new RewardSave(save.type.toString(), "SE_NightmareReward"));
        
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
        BaseMod.addEvent(new AddEventParams.Builder(ColosseumSE.ID, ColosseumSE.class)
                .eventType(EventUtils.EventType.NORMAL).spawnCondition(() -> AbstractDungeon.ascensionLevel >= 20)
                .bonusCondition(() -> AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.y > AbstractDungeon.map.size() / 2)
                .dungeonID(CityDepths.ID).endsWithRewardsUI(true).create());
        BaseMod.addEvent(new AddEventParams.Builder(ColosseumSE.ID, ColosseumSE.class)
                .eventType(EventUtils.EventType.NORMAL).spawnCondition(() -> AbstractDungeon.ascensionLevel >= 20)
                .bonusCondition(() -> AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.y > AbstractDungeon.map.size() / 2)
                .dungeonID(RootDepths.ID).endsWithRewardsUI(true).create());
        addMonsters();
    
        CustomDungeon.addAct(TheEnding.ID, new CityDepths());
        CustomDungeon.addAct(TheEnding.ID, new RootDepths());
    }
    
    @Override
    public void receiveStartGame() {
        PlayerBarricadeBlockLastTurn = 0;
        if (!CardCrawlGame.loadingSave) {
            if (ApoDropped) ApoDropped = false;
            if (ShieldAndSpearExisting) ShieldAndSpearExisting = false;
        }
    }
    
    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new SERBarricade(), RelicType.SHARED);
        BaseMod.addRelic(new SERInvitation(), RelicType.SHARED);
    }
    
    @Override
    public void receiveEditCards() {
        BaseMod.addCard(new WatcherOne());
        BaseMod.addCard(new WatcherTwo());
        BaseMod.addCard(new WatcherThree());
        BaseMod.addCard(new WatcherFour());
        BaseMod.addCard(new AlwaysPrepared());
    }
    
    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio(King.MakeID("RECRUIT_SOUND_0"), "SEAssets/audio/新兵老兵语音/Barrack_Ready.ogg");
        BaseMod.addAudio(King.MakeID("RECRUIT_SOUND_1"), "SEAssets/audio/新兵老兵语音/Barrack_Taunt1.ogg");
        BaseMod.addAudio(King.MakeID("RECRUIT_SOUND_2"), "SEAssets/audio/新兵老兵语音/Barrack_Taunt2.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_0"), "SEAssets/audio/国王语音/KingDenas-01d.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_1"), "SEAssets/audio/国王语音/KingDenas-02d.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_2"), "SEAssets/audio/国王语音/KingDenas-03g.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_3"), "SEAssets/audio/国王语音/KingDenas-04e.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_4"), "SEAssets/audio/国王语音/KingDenas_sfx_attack.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_5"), "SEAssets/audio/国王语音/KingDenas_sfx_order1.ogg");
        BaseMod.addAudio(King.MakeID("BOSS_KING_SOUND_6"), "SEAssets/audio/国王语音/KingDenas_sfx_order3.ogg");
    }
    
    @Override
    public void receivePostExhaust(AbstractCard card) {
        if (AbstractDungeon.getMonsters() != null) {
            for (AbstractMonster m : LMSK.GetAllExptMstr(c -> !c.powers.isEmpty())) {
                for (AbstractPower p : m.powers) {
                    if (p instanceof AbstractSEPower)
                        ((AbstractSEPower) p).onPlayerExhaustCard(card);
                }
            }
        }
    }
    
    @Override
    public void receivePostDungeonUpdate() {
        if (AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            for (AbstractCreature c : LMSK.GetAllExptCreatures(c -> !c.isDeadOrEscaped())) {
                SECuriosityPower.SECuriosityMark mark = SECuriosityPower.CuriosityField.MarkField.get(c);
                if (mark != null) {
                    boolean marked = mark.marked;
                    boolean hasPower = c.powers.stream().anyMatch(p -> p instanceof SECuriosityPower);
                    boolean removable = mark.removable();
                    if (marked && !hasPower && !removable) {
                        int amount = mark.amount;
                        SECuriosityPower power = new SECuriosityPower(c, amount);
                        c.powers.add(power);
                    }
                }
            }
        }
    }
    
    @Override
    public void receivePowersModified() {
        for (AbstractMonster m : LMSK.GetAllExptMstr(m -> m instanceof EvilGod)) {
            if (m instanceof EvilGod) {
                ((EvilGod) m).onPowersModified();
                AbstractPower p = m.getPower(GodVisionPower.ID);
                if (p instanceof GodVisionPower) 
                    ((GodVisionPower) p).updateOnPowersModified();
            }
        }
    }
    
    @Override
    public void receiveOnPlayerTurnStartPostDraw() {
        PlayerBarricadeBlockLastTurn = LMSK.Player().currentBlock;
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
                new SphericGuardian(-280F, MathUtils.random(-15F, 10F)), new OrbWalker(50F, MathUtils.random(0F, 25F))));
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
        BaseMod.addMonster(Encounter.TEST_MONSTER, () -> Populate(new TestMonsterEx(0F, 0F)));
        BaseMod.addMonster(Encounter.THREE_DARKLINGS_ENC, () -> Populate(new Darkling(-440F, 10F), new DarklingSE(-140F, 30F),
                new Darkling(180F, -5F)));
    
        // specials
        BaseMod.addMonster(Encounter.TEST_MONSTER_EX, () -> Populate(new Cultist(-420F, 0F), new Cultist(-180F, 0F), 
                new TestMonsterEx(0F, 0F)));
        BaseMod.addMonster(Encounter.TWO_TEST_MONSTER_EX, () -> Populate(new TestMonsterEx(-230F, 0F), new TestMonsterEx(60F, 0F)));
        BaseMod.addMonster(EvilGod.ID, () -> Populate(new EvilGod(EvilGod.MAIN_OFFSET_X, 0)));
        
        // the ending
        BaseMod.addMonster(Encounter.TWO_TEST_MONSTER, () -> Populate(new TestMonsterEx(-500F, 0F), new TestMonsterEx(0F, 0F)));
        BaseMod.addMonster(Encounter.LOUSE_BYRD_DARKLING, () -> Populate(new RedLouseSE(-580F, MathUtils.random(-20F, 20F)),
                new ByrdSE(-240F, MathUtils.random(25F, 70F)), new DarklingSE(120F, -5F)));
        
        // city depths
        BaseMod.addMonster(Encounter.CITY_DEPTHS_ELITE, () -> Populate(new Recruit(-440F, MathUtils.random(20F, 50F), true), 
                new SlaverElite(-150F, MathUtils.random(-15F, 0F), true), new SlaverRedElite(170F, 10F, true)));
        BaseMod.addBoss(CityDepths.ID, BossKing.ID, "SEAssets/images/ui/map/boss/kingIcon.png", "SEAssets/images/ui/map/boss/kingIcon.png");
        BaseMod.addMonster(BossKing.ID, () -> Populate(new Paladin(BossKing.PALADIN_OFFSET_X, BossKing.PALADIN_OFFSET_Y), 
                new BossKing(BossKing.BOSS_KING_OFFSET_X, BossKing.BOSS_KING_OFFSET_Y),
                new Recruit(BossKing.RECRUIT_OFFSET_X, BossKing.RECRUIT_OFFSET_Y, false)));
        
        // root depths
        BaseMod.addMonster(Encounter.ROOT_DEPTHS_ELITE, () -> Populate(new SnakePlantGreenRD(-350F, 0F), 
                new SnakePlantPurpleRD(120F, 0F)));
        BaseMod.addBoss(RootDepths.ID, TheHolyTree.ID, "SEAssets/images/ui/map/boss/treeIcon.png", "SEAssets/images/ui/map/boss/treeIcon.png");
        BaseMod.addMonster(TheHolyTree.ID, () -> Populate(new TheHolyTree(0F, 0F)));
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
        public static final String TWO_TEST_MONSTER = "SE Two Test Monster";
        public static final String LOUSE_BYRD_DARKLING = "SE Louse Byrd Darkling";
        public static final String CITY_DEPTHS_ELITE = "The Depths Elites";
        public static final String ROOT_DEPTHS_ELITE = "The Root Elites";
        public static final String TEST_MONSTER_EX = TEST_MONSTER + " EX";
        public static final String TWO_TEST_MONSTER_EX = TWO_TEST_MONSTER + " EX";
    }
}