package rs.winds.monsters.citydepths;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.powers.MetallicizePower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.powers.MidArmorPower;
import rs.winds.powers.PaladinCardCounterPower;
import rs.winds.powers.TauntPower;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Paladin extends AbstractMonster implements BossKing.BossKingAlly {
    public static final String ID = King.MakeID("Paladin");
    private static final MonsterStrings strings = King.MonsterStrings(ID);
    public static final String NAME = strings.NAME;
    public static final String[] DIALOG = strings.DIALOG;
    private static final String IMG = "SEAssets/images/monsters/paladin/paladin.png";
    private static final byte attack = 0;
    private static final byte taunt = 1;
    private static final byte buff = 2;
    
    public Paladin(float x, float y) {
        super(NAME, ID, 150, 0, 0, 164, 203, IMG, x, y);
        damage.add(new DamageInfo(this, 20));
        addPower(new ArtifactPower(this, 2));
        addPower(new BarricadePower(this));
        addPower(new MidArmorPower(this));
        addPower(new PaladinCardCounterPower(this, 13, 10));
    }
    
    @Override
    public void usePreBattleAction() {
        addToBot(new GainBlockAction(this, this, 50));
    }
    
    @Override
    public void takeTurn() {
        switch (nextMove) {
            case taunt:
                addToBot(new GainBlockAction(this, this, 100));
                addToBot(new ApplyPowerAction(this, this, new TauntPower(this, 1)));
                break;
            case attack:
                addToBot(new DamageAction(LMSK.Player(), damage.get(attack), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
            case buff:
                addToBot(new ApplyPowerAction(this, this, new MetallicizePower(this, 10)));
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
        if (lastMove(taunt)) {
            setMove(attack, Intent.ATTACK, damage.get(attack).base);
        } else if (lastMove(attack)) {
            setMove(buff, Intent.BUFF);
        } else {
            setMove(taunt, Intent.BUFF);
        }
    }
}