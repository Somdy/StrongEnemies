package rs.winds.monsters.citydepths;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AngerPower;
import com.megacrit.cardcrawl.powers.AngryPower;
import com.megacrit.cardcrawl.powers.MetallicizePower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.monsters.SETool;
import rs.winds.powers.LightArmorPower;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Veteran extends AbstractMonster {
    public static final String ID = King.MakeID("Veteran");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final String IMG = "SEAssets/images/monsters/veteran/veteran.png";
    private static final byte smash = 0;
    private static final byte multi = 1;
    private static final byte debuff = 2;
    private static final int multiCount = 2;
    private int turnCount;
    private boolean firstTurn;
    
    public Veteran(float x, float y) {
        super(NAME, ID, 180, 0, 0, 116, 187, IMG, x, y);
        type = EnemyType.ELITE;
        damage.add(smash, new DamageInfo(this, 30));
        damage.add(multi, new DamageInfo(this, 10));
        addPower(new MetallicizePower(this, 10));
        addPower(new AngerPower(this, 2));
        addPower(new AngryPower(this, 2));
        addPower(new LightArmorPower(this));
        turnCount = 1;
        firstTurn = true;
    }
    
    @Override
    public void takeTurn() {
        playSfx();
        turnCount++;
        switch (nextMove) {
            case smash:
                addToBot(new DamageAction(LMSK.Player(), damage.get(smash), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
            case multi:
                for (int i = 0; i < multiCount; i++) {
                    addToBot(new DamageAction(LMSK.Player(), damage.get(multi), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                }
                break;
            case debuff:
                addToBot(new QuickAction(() -> {
                    List<AbstractPower> debuffs = powers.stream().filter(p -> p.type == AbstractPower.PowerType.DEBUFF)
                            .collect(Collectors.toList());
                    for (int i = 0; i < 2 && !debuffs.isEmpty(); i++) {
                        Optional<AbstractPower> p = LMSK.GetRandom(debuffs, LMSK.MonsterRng());
                        p.ifPresent(po -> {
                            debuffs.remove(po);
                            addToTop(new RemoveSpecificPowerAction(this, this, po));
                        });
                    }
                }));
                break;
        }
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int roll) {
        if (firstTurn || lastMove(debuff)) {
            firstTurn = false;
            if (SETool.MonsterAIRng().randomBoolean(0.5F)) {
                setMove(smash, Intent.ATTACK, damage.get(smash).base);
            } else {
                setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
            }
        } else if (turnCount % 3 == 0) {
            turnCount = 0;
            setMove(debuff, Intent.BUFF);
        } else if (lastMove(smash)) {
            setMove(multi, Intent.ATTACK, damage.get(multi).base, multiCount, true);
        } else {
            setMove(smash, Intent.ATTACK, damage.get(smash).base);
        }
    }
    
    private void playSfx() {
        int roll = MathUtils.random(3);
        addToBot(new SFXAction(King.MakeID("RECRUIT_SOUND_" + roll)));
    }
}