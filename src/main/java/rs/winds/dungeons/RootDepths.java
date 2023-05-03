package rs.winds.dungeons;

import actlikeit.dungeons.CustomDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapGenerator;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.scenes.AbstractScene;
import com.megacrit.cardcrawl.scenes.TheCityScene;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.citydepths.BossKing;
import rs.winds.monsters.rootdepths.TheHolyTree;

import java.util.ArrayList;

public class RootDepths extends CustomDungeon {
    public static final String ID = "RootDepths";
    
    public RootDepths() {
        super("深根底层", ID);
        isFinalAct(true);
    }
    
    public RootDepths(CustomDungeon cd, AbstractPlayer p, ArrayList<String> emptyList) {
        super(cd, p, emptyList);
    }
    
    public RootDepths(CustomDungeon cd, AbstractPlayer p, SaveFile sf) {
        super(cd, p, sf);
    }
    
    @Override
    protected void makeMap() {
        map = new ArrayList<>();
        ArrayList<MapRoomNode> row1 = new ArrayList<>();
        MapRoomNode restNode = new MapRoomNode(3, 0);
        restNode.room = new RestRoom();
        MapRoomNode shopNode = new MapRoomNode(3, 1);
        shopNode.room = new ShopRoom();
        MapRoomNode enemyNode = new MapRoomNode(3, 2);
        enemyNode.room = new MonsterRoomElite();
        MapRoomNode bossNode = new MapRoomNode(3, 3);
        bossNode.room = new MonsterRoomBoss();
        MapRoomNode victoryNode = new MapRoomNode(3, 4);
        victoryNode.room = new TrueVictoryRoom();
        connectNode(restNode, shopNode);
        connectNode(shopNode, enemyNode);
        enemyNode.addEdge(new MapEdge(enemyNode.x, enemyNode.y, enemyNode.offsetX, enemyNode.offsetY, bossNode.x, bossNode.y, bossNode.offsetX, bossNode.offsetY, false));
        row1.add(new MapRoomNode(0, 0));
        row1.add(new MapRoomNode(1, 0));
        row1.add(new MapRoomNode(2, 0));
        row1.add(restNode);
        row1.add(new MapRoomNode(4, 0));
        row1.add(new MapRoomNode(5, 0));
        row1.add(new MapRoomNode(6, 0));
        ArrayList<MapRoomNode> row2 = new ArrayList<>();
        row2.add(new MapRoomNode(0, 1));
        row2.add(new MapRoomNode(1, 1));
        row2.add(new MapRoomNode(2, 1));
        row2.add(shopNode);
        row2.add(new MapRoomNode(4, 1));
        row2.add(new MapRoomNode(5, 1));
        row2.add(new MapRoomNode(6, 1));
        ArrayList<MapRoomNode> row3 = new ArrayList<>();
        row3.add(new MapRoomNode(0, 2));
        row3.add(new MapRoomNode(1, 2));
        row3.add(new MapRoomNode(2, 2));
        row3.add(enemyNode);
        row3.add(new MapRoomNode(4, 2));
        row3.add(new MapRoomNode(5, 2));
        row3.add(new MapRoomNode(6, 2));
        ArrayList<MapRoomNode> row4 = new ArrayList<>();
        row4.add(new MapRoomNode(0, 3));
        row4.add(new MapRoomNode(1, 3));
        row4.add(new MapRoomNode(2, 3));
        row4.add(bossNode);
        row4.add(new MapRoomNode(4, 3));
        row4.add(new MapRoomNode(5, 3));
        row4.add(new MapRoomNode(6, 3));
        ArrayList<MapRoomNode> row5 = new ArrayList<>();
        row5.add(new MapRoomNode(0, 4));
        row5.add(new MapRoomNode(1, 4));
        row5.add(new MapRoomNode(2, 4));
        row5.add(victoryNode);
        row5.add(new MapRoomNode(4, 4));
        row5.add(new MapRoomNode(5, 4));
        row5.add(new MapRoomNode(6, 4));
        row5.add(victoryNode);
        map.addAll(LMSK.ListFromObjs(row1, row2, row3, row4, row5));
        logger.info("Generated the following dungeon map:");
        logger.info(MapGenerator.toString(map, true));
        logger.info("Game Seed: " + Settings.seed);
        firstRoomChosen = false;
        fadeIn();
    }
    
    private void connectNode(MapRoomNode src, MapRoomNode dst) {
        src.addEdge(new MapEdge(src.x, src.y, src.offsetX, src.offsetY, dst.x, dst.y, dst.offsetX, dst.offsetY, false));
    }
    
    @Override
    public AbstractScene DungeonScene() {
        return new TheCityScene();
    }
    
    @Override
    public boolean canSpawn() {
        return AbstractDungeon.ascensionLevel >= 20;
    }
    
    @Override
    public void Ending() {
        CardCrawlGame.music.fadeOutBGM();
        MapRoomNode node = new MapRoomNode(3, 4);
        node.room = new TrueVictoryRoom();
        AbstractDungeon.nextRoom = node;
        AbstractDungeon.closeCurrentScreen();
        AbstractDungeon.nextRoomTransitionStart();
    }
    
    @Override
    protected void initializeBoss() {
        bossList.add(TheHolyTree.ID);
        bossList.add(TheHolyTree.ID);
        bossList.add(TheHolyTree.ID);
    }
    
    @Override
    protected void generateMonsters() {
        monsterList.clear();
        monsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        monsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        monsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        eliteMonsterList.clear();
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
    }
    
    @Override
    protected void generateElites(int count) {
        eliteMonsterList.clear();
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
        eliteMonsterList.add(King.Encounter.ROOT_DEPTHS_ELITE);
    }
    
    @Override
    protected void generateStrongEnemies(int count) {}
    
    @Override
    protected void generateWeakEnemies(int count) {}
}