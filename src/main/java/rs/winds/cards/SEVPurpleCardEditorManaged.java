package rs.winds.cards;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.tempCards.Insight;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.powers.watcher.BlockReturnPower;
import com.megacrit.cardcrawl.stances.CalmStance;
import com.megacrit.cardcrawl.stances.WrathStance;
import rs.lazymankits.utils.LMSK;
import rs.winds.actions.unique.ThirdEyeSpecialAction;
import rs.winds.core.King;

import static rs.winds.cards.SECardEditor.PST_APPLYPOWERS;
import static rs.winds.cards.SECardEditor.PST_CALCDAMAGE;
import static rs.winds.monsters.SETool.*;
import static rs.winds.core.ClosedBeta.*;

public class SEVPurpleCardEditorManaged {
    
    private static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    private static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    @SECardEditor(card = EmptyFist.class, functional = BETA_1)
    public static class EmptyFistPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("EmptyFist"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 1;
            };
            e.use = (c,p,m) -> {
                addToBot(new DrawCardAction(p, c.magicNumber));
                return false;
            };
        }
    }
    
    @SECardEditor(card = EmptyBody.class, functional = BETA_1)
    public static class EmptyBodyPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("EmptyBody"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 1;
            };
            e.use = (c,p,m) -> {
                addToBot(new DrawCardAction(p, c.magicNumber));
                return false;
            };
        }
    }
    
    @SECardEditor(card = ThirdEye.class, functional = BETA_1)
    @SpirePatch2(clz = ThirdEye.class, method = SpirePatch.CONSTRUCTOR)
    public static class ThirdEyePatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard __instance) {
            __instance.rarity = AbstractCard.CardRarity.RARE;
        }
        private static final CardStrings strings = King.CardStrings(King.MakeID("ThirdEye"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 1;
            };
            e.use = (c,p,m) -> {
                addToBot(new ThirdEyeSpecialAction(5, c.originalName));
                addToBot(new DrawCardAction(p, c.magicNumber));
                return true;
            };
            e.upgrade = c -> {
                upgradeCardName(c);
                upgradeCardMagic(c, 1);
                return true;
            };
        }
    }
    
    @SECardEditor(card = Evaluate.class, functional = BETA_1)
    public static class EvaluatePatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("Evaluate"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.magicNumber = c.baseMagicNumber = 2;
            };
            e.use = (c,p,m) -> {
                addToBot(new GainBlockAction(p, p, c.block));
                Insight insight = new Insight();
                addToBot(new MakeTempCardInDrawPileAction(insight, 1, true, true, false));
                if (CalmStance.STANCE_ID.equals(LMSK.Player().stance.ID)) {
                    addToBot(new GainEnergyAction(c.magicNumber));
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = FlyingSleeves.class, functional = BETA_1, extraMethods = {PST_APPLYPOWERS, PST_CALCDAMAGE})
    @SpirePatch2(clz = FlyingSleeves.class, method = SpirePatch.CONSTRUCTOR)
    public static class FlyingSleevesPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard __instance) {
            __instance.rarity = AbstractCard.CardRarity.UNCOMMON;
        }
        private static final CardStrings strings = King.CardStrings(King.MakeID("FlyingSleeves"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.applyPowers = c -> {
                e.putInt("realBase", c.baseDamage);
                if (WrathStance.STANCE_ID.equals(LMSK.Player().stance.ID))
                    c.baseDamage *= 2;
                return false;
            };
            e.postPowers = c -> {
                c.baseDamage = e.getInt("realBase");
                c.isDamageModified = c.baseDamage != c.damage;
            };
            e.calcDamage = (c,m) -> {
                e.putInt("realBase", c.baseDamage);
                if (WrathStance.STANCE_ID.equals(LMSK.Player().stance.ID))
                    c.baseDamage *= 2;
                return false;
            };
            e.postCalc = (c,m) -> {
                c.baseDamage = e.getInt("realBase");
                c.isDamageModified = c.baseDamage != c.damage;
            };
        }
    }
    
    @SECardEditor(card = TalkToTheHand.class, functional = BETA_1)
    public static class TalkToTheHandPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("TalkToTheHand"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
            };
            e.use = (c,p,m) -> {
                addToBot(new ApplyPowerAction(m, p, new WeakPower(m, 1, false)));
                addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, 1, false)));
                addToBot(new ApplyPowerAction(m, p, new BlockReturnPower(m, c.magicNumber), c.magicNumber));
                return true;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    updateCardDesc(c, strings.UPGRADE_DESCRIPTION);
                    c.exhaust = false;
                }
                return false;
            };
        }
    }
    
    @SECardEditor(card = FlurryOfBlows.class, functional = BETA_1)
    public static class FlurryOfBlowsPatch {
        private static final CardStrings strings = King.CardStrings(King.MakeID("FlurryOfBlows"));
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.initFunc = c -> {
                updateCardDesc(c, strings.DESCRIPTION);
                c.damage = c.baseDamage = 6;
                c.block = c.baseBlock = 6;
                c.cost = c.costForTurn = 1;
            };
            e.use = (c,p,m) -> {
                addToBot(new DamageAction(m, new DamageInfo(p, c.damage, c.damageTypeForTurn), 
                        AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new GainBlockAction(p, p, c.block));
                return true;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardDamage(c, 3);
                    upgradeCardBlock(c, 3);
                }
                return true;
            };
        }
    }
}