package rs.winds.cards;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.BaneAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.green.Acrobatics;
import com.megacrit.cardcrawl.cards.green.Bane;
import com.megacrit.cardcrawl.cards.green.Eviscerate;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AccuracyPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.utils.LMDamageInfoHelper;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

import static rs.winds.monsters.SETool.*;
import static rs.winds.cards.SECardEditor.*;
import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;

public class SEVCardEditorManaged {
    private static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    private static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    @SECardEditor(card = Acrobatics.class)
    public static class AcrobaticsSE {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Acrobatics"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> updateCardDesc(c, strings.DESCRIPTION);
            e.use = (c,p,m) -> {
                addToBot(new DrawCardAction(p, c.magicNumber));
                addToBot(new DamageAction(p, new DamageInfo(p, 4, c.damageTypeForTurn)));
                addToBot(new SimpleHandCardSelectBuilder(ca -> true)
                        .setCanPickZero(false).setAnyNumber(false)
                        .setAmount(1).setMsg(DiscardAction.TEXT[0]).setManipulator(new HandCardManipulator() {
                            @Override
                            public boolean manipulate(AbstractCard card, int i) {
                                e.putObj("discarded", card);
                                p.hand.moveToDiscardPile(card);
                                card.triggerOnManualDiscard();
                                GameActionManager.incrementDiscard(false);
                                return false;
                            }
                        }).setFollowUpAction(new QuickAction(() -> {
                            AbstractCard card = e.getObj("discarded");
                            if (p.discardPile.contains(card)) {
                                p.discardPile.removeCard(card);
                                addToTop(new NewQueueCardAction(card, true, true, true));
                            }
                        })));
                return true;
            };
        }
    }
    
    @SECardEditor(card = Shiv.class, extraMethods = {APPLYPOWERS, CALCDAMAGE, ONEXHAUST})
    public static class ShivSE {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Shiv"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.baseDamage = c.damage = 4;
                c.isEthereal = true;
            };
            e.use = (c,p,m) -> {
                addToBot(new DamageAction(m, LMDamageInfoHelper.Create(p, c.damage, c.damageTypeForTurn), SLASH_HORIZONTAL));
                return true;
            };
            e.applyPowers = c -> {
                float tmp = c.baseDamage;
                AbstractPower p = LMSK.Player().getPower(AccuracyPower.POWER_ID);
                if (p != null) tmp += p.amount;
                c.damage = MathUtils.floor(tmp);
                c.isDamageModified = c.baseDamage != c.damage;
                return true;
            };
            e.calcDamage = (c,m) -> {
                float tmp = c.baseDamage;
                AbstractPower p = LMSK.Player().getPower(AccuracyPower.POWER_ID);
                if (p != null) tmp += p.amount;
                c.damage = MathUtils.floor(tmp);
                c.isDamageModified = c.baseDamage != c.damage;
                return true;
            };
            e.onExhaust = c -> {
                addToBot(new GainBlockAction(LMSK.Player(), c.damage));
                return true;
            };
        }
        
        @SpirePatches2({
                @SpirePatch2(clz = AccuracyPower.class, method = "updateExistingShivs"),
                @SpirePatch2(clz = AccuracyPower.class, method = "onDrawOrDiscard")
        })
        public static class AccuracyPowerPatch {
            @SpirePrefixPatch
            public static SpireReturn Prefix() {
                return SpireReturn.Return();
            }
        }
    }
    
    @SECardEditor(card = Eviscerate.class, extraMethods = {PST_APPLYPOWERS, PST_CALCDAMAGE, ONDRAWN})
    public static class EviscerateSE {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Eviscerate"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.baseMagicNumber = c.magicNumber = 3;
            };
            e.use = (c,p,m) -> {
                addToBot(new LoseHPAction(p, p, 3));
                return false;
            };
            e.applyPowers = c -> {
                e.putInt("realBase", c.baseDamage);
                c.baseDamage += c.magicNumber * GameActionManager.totalDiscardedThisTurn;
                return false;
            };
            e.postPowers = c -> {
                c.baseDamage = e.getInt("realBase");
                c.isDamageModified = c.baseDamage != c.damage;
            };
            e.calcDamage = (c,m) -> {
                e.putInt("realBase", c.baseDamage);
                c.baseDamage += c.magicNumber * GameActionManager.totalDiscardedThisTurn;
                return false;
            };
            e.postCalc = (c,m) -> {
                c.baseDamage = e.getInt("realBase");
                c.isDamageModified = c.baseDamage != c.damage;
            };
            e.onDrawn = c -> {
                c.applyPowers();
                return false;
            };
        }
        @SpirePatch2(clz = Eviscerate.class, method = "didDiscard")
        public static class DidDiscardPatch {
            @SpirePostfixPatch
            public static void Postfix(AbstractCard __instance) {
                __instance.applyPowers();
            }
        }
    }
    
    @SECardEditor(card = Bane.class)
    public static class BaneSE {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Bane"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.upgrade = c -> {
                if (!c.upgraded) {
                    updateCardDesc(c, strings.DESCRIPTION);
                }
                return false;
            };
            e.use = (c,p,m) -> {
                if (c.upgraded) {
                    addToBot(new DamageAction(m, new DamageInfo(p, c.damage, c.damageTypeForTurn), SLASH_HORIZONTAL));
                    addToBot(new QuickAction(() -> {
                        AbstractPower po = m.getPower(PoisonPower.POWER_ID);
                        if (po != null) {
                            addToTop(new DamageAction(m, new DamageInfo(p, po.amount, c.damageTypeForTurn), SLASH_HORIZONTAL));
                        }
                    }));
                    return true;
                }
                return false;
            };
        }
    }
}