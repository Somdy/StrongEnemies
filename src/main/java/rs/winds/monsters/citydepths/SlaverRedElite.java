package rs.winds.monsters.citydepths;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;
import com.megacrit.cardcrawl.monsters.exordium.SlaverRed;
import com.megacrit.cardcrawl.powers.EntanglePower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.listeners.TurnEventListener;
import rs.lazymankits.listeners.tools.TurnEvent;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.DeathrattlePower;
import rs.winds.powers.VulnerablePlusPower;

public class SlaverRedElite extends AbstractMonster {
    public static final String ID = King.MakeID(SlaverRed.ID);
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte attack = 0;
    private static final byte debuff = 1;
    private boolean firstTurn;
    public final boolean deathrattle;
    
    public SlaverRedElite(float x, float y, boolean deathrattle) {
        super(NAME, ID, 70, 0F, 0F, 170F, 230F, null, x, y);
        type = EnemyType.ELITE;
        loadAnimation("images/monsters/theBottom/redSlaver/skeleton.atlas", "images/monsters/theBottom/redSlaver/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        damage.add(new DamageInfo(this, 15));
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
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new EntanglePower(LMSK.Player()){
                    {amount = 2;}
                    @Override
                    public void atEndOfTurn(boolean isPlayer) {
                        if (isPlayer) {
                            addToBot(new ReducePowerAction(owner, owner, this, 1));
                        }
                    }
                }));
                TurnEventListener.AddNewEndRoundEvent(new TurnEvent(() -> addToBot(new QuickAction(() -> {
                    SlaverBlueElite elite = new SlaverBlueElite(x, y);
                    elite.usePreBattleAction();
                    addToTop(new QuickAction(() -> {
                        AbstractDungeon.getMonsters().monsters.remove(tmp);
                        AbstractDungeon.getCurrRoom().cannotLose = false;
                        King.DepthsElitesAllFinished = true;
                    }));
                    addToTop(new QuickAction(elite::createIntent));
                    addToTop(new SpawnMonsterAction(elite, false));
                }))).setDelay(0, true).setRemoveConditions(event -> event.times <= 0));
            }));
        }
        firstTurn = true;
    }
    
    @Override
    public void usePreBattleAction() {
        if (deathrattle) {
            King.DepthsElitesAllFinished = false;
            AbstractDungeon.getCurrRoom().cannotLose = true;
        }
        addToBot(new GainBlockAction(this, 30));
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case debuff:
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new EntanglePower(LMSK.Player())));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new VulnerablePlusPower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 1)));
                break;
            case attack:
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int i) {
        if (firstTurn) {
            firstTurn = false;
            setMove(debuff, Intent.STRONG_DEBUFF);
        } else if (lastMove(debuff) || (lastMove(attack) && lastMoveBefore(debuff))) {
            setMove(attack, Intent.ATTACK, damage.get(attack).base);
        } else {
            setMove(debuff, Intent.STRONG_DEBUFF);
        }
    }
}
