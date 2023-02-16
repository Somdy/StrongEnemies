package rs.winds.monsters.citydepths;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Champ;
import com.megacrit.cardcrawl.powers.*;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.SETool;
import rs.winds.powers.LightArmorPower;

public class KingdomChamp extends AbstractMonster implements BossKing.BossKingAlly {
    public static final String ID = King.MakeID(Champ.ID);
    private static final MonsterStrings strings = King.MonsterStrings(Champ.ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final byte attack_debuff = 0;
    private static final byte pure_attack = 1;
    private static final byte buff = 2;
    private boolean firstTurn;
    
    public KingdomChamp(float x, float y) {
        super("王国勇士", ID, 200, 0F, 0F, 200F, 205F, null, x, y);
        loadAnimation("images/monsters/theCity/champ/skeleton.atlas", "images/monsters/theCity/champ/skeleton.json", 1.5F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        e.setTimeScale(0.8F);
        addPower(new ArtifactPower(this, 1));
        addPower(new LightArmorPower(this));
        addPower(new ThornsPower(this, 3));
        damage.add(attack_debuff, new DamageInfo(this, 10));
        damage.add(pure_attack, new DamageInfo(this, 15));
        firstTurn = true;
    }
    
    @Override
    public void takeTurn() {
        playSfx();
        talk();
        switch (nextMove) {
            case attack_debuff:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack_debuff), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new WeakPower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new FrailPower(LMSK.Player(), 2, true)));
                addToBot(new ApplyPowerAction(LMSK.Player(), this, new VulnerablePower(LMSK.Player(), 2, true)));
                break;
            case pure_attack:
                addToBot(new ChangeStateAction(this, "ATTACK"));
                addToBot(new DamageAction(LMSK.Player(), damage.get(pure_attack), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                break;
            case buff:
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 5)));
                addToBot(new ApplyPowerAction(this, this, new MetallicizePower(this, 5)));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (firstTurn) {
            firstTurn = false;
            setMove(attack_debuff, Intent.ATTACK_DEBUFF, damage.get(attack_debuff).base);
        } else if (roll < 30) {
            if (!lastMove(pure_attack)) {
                setMove(pure_attack, Intent.ATTACK, damage.get(pure_attack).base);
            } else {
                getMove(SETool.MonsterAIRng().random(roll, 99));
            }
        } else if (roll < 60) {
            if (!lastMove(buff)) {
                setMove(buff, Intent.BUFF);
            } else if (SETool.MonsterAIRng().randomBoolean(30 / 7F)) {
                setMove(pure_attack, Intent.ATTACK, damage.get(pure_attack).base);
            } else {
                setMove(attack_debuff, Intent.ATTACK_DEBUFF, damage.get(attack_debuff).base);
            }
        } else {
            if (!lastMove(attack_debuff)) {
                setMove(attack_debuff, Intent.ATTACK_DEBUFF, damage.get(attack_debuff).base);
            } else {
                getMove(SETool.MonsterAIRng().random(0, roll));
            }
        }
    }
    
    @Override
    public void changeState(String stateName) {
        switch (stateName) {
            case "ATTACK":
                state.setAnimation(0, "Attack", false);
                state.addAnimation(0, "Idle", true, 0F);
                break;
        }
    }
    
    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
            state.setAnimation(0, "Hit", false);
            state.addAnimation(0, "Idle", true, 0F);
        }
    }
    
    private void playSfx() {
        String[] voices = new String[]{"MONSTER_CHAMP_SLAP", "VO_CHAMP_2A", "MONSTER_CHAMP_CHARGE"};
        int index = MathUtils.random(voices.length - 1);
        addToBot(new SFXAction(voices[index]));
    }
    
    private void talk() {
        int roll = MathUtils.random(5);
        addToBot(new TalkAction(this, DIALOG[roll]));
    }
}