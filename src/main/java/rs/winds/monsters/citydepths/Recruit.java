package rs.winds.monsters.citydepths;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.red.Clash;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.listeners.TurnEventListener;
import rs.lazymankits.listeners.tools.TurnEvent;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.DeathrattlePower;

public class Recruit extends AbstractMonster implements LMGameGeneralUtils, BossKing.BossKingAlly {
    public static final String ID = King.MakeID("Recruit");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final String IMG = "SEAssets/images/monsters/recruit/recruit.png";
    private static final byte attack_defense = 0;
    private static final byte multi_attack = 1;
    private static final int multiCount = 3;
    public final boolean deathrattle;
    
    public Recruit(float x, float y, boolean deathrattle) {
        super(NAME, ID, 85, 0, 0, 113, 156, IMG, x, y);
        type = EnemyType.ELITE;
        damage.add(attack_defense, new DamageInfo(this, 15));
        damage.add(multi_attack, new DamageInfo(this, 10));
        this.deathrattle = deathrattle;
        if (deathrattle) {
            addPower(new DeathrattlePower(this, DIALOG[0], m -> {
                ApologySlime tmp = new ApologySlime(){
                    {
                        halfDead = true;
                        drawX = 9999F;
                        drawY = 9999F;
                    }
        
                    @Override
                    public void takeTurn() {
                        addToBot(new RollMoveAction(this));
                    }
        
                    @Override
                    protected void getMove(int num) {
                        setMove((byte) 0, Intent.UNKNOWN);
                    }
        
                    @Override
                    public void render(SpriteBatch sb) {}
                };
                tmp.rollMove();
                tmp.createIntent();
                AbstractDungeon.getMonsters().add(tmp);
                addToBot(new MakeTempCardInDrawPileAction(new Clash(), 1, true, true));
                TurnEventListener.AddNewEndRoundEvent(new TurnEvent(() -> addToBot(new QuickAction(() -> {
                    Recruit recruit = new Recruit(x, y, false);
                    recruit.usePreBattleAction();
                    addToTop(new QuickAction(() -> {
                        AbstractDungeon.getMonsters().monsters.remove(tmp);
                        AbstractDungeon.getCurrRoom().cannotLose = false;
                        King.DepthsElitesAllFinished = true;
                    }));
                    addToTop(new QuickAction(recruit::createIntent));
                    addToTop(new SpawnMonsterAction(recruit, false));
                }))).setDelay(0, true).setRemoveConditions(e -> e.times <= 0));
            }));
        }
    }
    
    @Override
    public void usePreBattleAction() {
        if (deathrattle) {
            King.DepthsElitesAllFinished = false;
            AbstractDungeon.getCurrRoom().cannotLose = true;
        }
    }
    
    @Override
    public void takeTurn() {
        playSfx();
        switch (nextMove) {
            case attack_defense:
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack_defense), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new GainBlockAction(this, 15));
                break;
            case multi_attack:
                for (int i = 0; i < multiCount; i++) {
                    addToBot(new DamageAction(LMSK.Player(), damage.get(multi_attack), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                }
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (monsterAiRng().randomBoolean(0.5F) && !lastMove(attack_defense)) {
            setMove(attack_defense, Intent.ATTACK_DEFEND, damage.get(attack_defense).base);
        } else if (!lastMove(multi_attack)) {
            setMove(multi_attack, Intent.ATTACK, damage.get(multi_attack).base, multiCount, true);
        } else {
            setMove(attack_defense, Intent.ATTACK_DEFEND, damage.get(attack_defense).base);
        }
    }
    
    private void playSfx() {
        int roll = MathUtils.random(3);
        addToBot(new SFXAction(King.MakeID("RECRUIT_SOUND_" + roll)));
    }
}