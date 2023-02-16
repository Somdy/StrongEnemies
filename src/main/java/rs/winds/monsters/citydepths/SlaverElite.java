package rs.winds.monsters.citydepths;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.VampireDamageAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Taskmaster;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.listeners.TurnEventListener;
import rs.lazymankits.listeners.tools.TurnEvent;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.DeathrattlePower;
import rs.winds.powers.SlaverElitePower;

public class SlaverElite extends AbstractMonster implements BossKing.BossKingAlly {
    public static final String ID = King.MakeID(Taskmaster.ID);
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    public final boolean deathrattle;
    
    public SlaverElite(float x, float y, boolean deathrattle) {
        super(NAME, ID, 120, 0F, 0F, 200F, 280F, null, x, y);
        type = EnemyType.ELITE;
        loadAnimation("images/monsters/theCity/slaverMaster/skeleton.atlas", "images/monsters/theCity/slaverMaster/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        damage.add(new DamageInfo(this, 15));
        addPower(new SlaverElitePower(this, 4));
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
                addToBot(new MakeTempCardInDrawPileAction(new Wound(), 2, true, true));
                TurnEventListener.AddNewEndRoundEvent(new TurnEvent(() -> addToBot(new QuickAction(() -> {
                    Veteran veteran = new Veteran(x, y);
                    veteran.usePreBattleAction();
                    addToTop(new QuickAction(() -> {
                        AbstractDungeon.getMonsters().monsters.remove(tmp);
                        AbstractDungeon.getCurrRoom().cannotLose = false;
                        King.DepthsElitesAllFinished = true;
                    }));
                    addToTop(new QuickAction(veteran::createIntent));
                    addToTop(new SpawnMonsterAction(veteran, false));
                }))).setDelay(0, true).setRemoveConditions(event -> event.times <= 0));
            }));
        }
    }
    
    @Override
    public void usePreBattleAction() {
        if (deathrattle) {
            King.DepthsElitesAllFinished = false;
            AbstractDungeon.getCurrRoom().cannotLose = true;
        }
        addToBot(new GainBlockAction(this, 20));
    }
    
    @Override
    public void takeTurn() {
        playSfx();
        addToBot(new VampireDamageAction(LMSK.Player(), damage.get(0), AbstractGameAction.AttackEffect.SLASH_HEAVY));
        addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 1)));
        addToBot(new MakeTempCardInDiscardAction(new Wound(), 2));
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        setMove((byte) 0, Intent.ATTACK_DEBUFF, damage.get(0).base);
    }
    
    private void playSfx() {
        int roll = MathUtils.random(1);
        if (roll == 0) {
            addToBot(new SFXAction("VO_SLAVERLEADER_1A"));
        } else {
            addToBot(new SFXAction("VO_SLAVERLEADER_1B"));
        }
    }
}