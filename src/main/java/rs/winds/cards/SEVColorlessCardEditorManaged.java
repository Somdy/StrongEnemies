package rs.winds.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.*;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static rs.winds.core.ClosedBeta.*;
import static rs.winds.monsters.SETool.*;
import static rs.winds.cards.SECardEditor.*;

public class SEVColorlessCardEditorManaged {
    private static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    private static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    @SECardEditor(card = Slimed.class, functional = BETA_3)
    public static class SlimedPatch {
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardBaseCost(c, 3);
                }
                return false;
            };
        }
    }
    
    @SECardEditor(card = Burn.class, functional = BETA_3)
    public static class BurnPatch {
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    upgradeCardMagic(c, 4);
                    updateCardDesc(c, " 不能被打出 。 NL 在你的回合结束时，你受到6点伤害。");
                }
                return true;
            };
        }
    }
    
    @SECardEditor(card = Dazed.class, functional = BETA_3, extraMethods = {ONDRAWN})
    public static class DazedPatch {
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.onDrawn = c -> {
                if (c.upgraded) {
                    addToTop(new MakeTempCardInHandAction(new Dazed(), 3));
                    addToTop(new ExhaustSpecificCardAction(c, AbstractDungeon.player.hand));
                    return true;
                }
                return false;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    updateCardDesc(c, "虚无 ，抽到时手牌加入3张眩晕");
                }
                return false;
            };
        }
    }
    
    @SECardEditor(card = Wound.class, functional = BETA_3, extraMethods = {ONDRAWN})
    public static class WoundPatch {
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.onDrawn = c -> {
                if (c.upgraded) {
                    addToTop(new MakeTempCardInDrawPileAction(new Wound(), 1, true, true));
                    return true;
                }
                return false;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    updateCardDesc(c, "不能被打出 。每当抽到这张牌时，往抽牌堆加入1张伤口");
                }
                return false;
            };
        }
    }
    
    @SECardEditor(card = VoidCard.class, functional = BETA_3, extraMethods = {ONDRAWN})
    public static class VoidCardPatch {
        public static void Edit(AbstractCard _inst) {
            CardEditor e = GetModifierEditor(_inst);
            e.onDrawn = c -> {
                if (c.upgraded) {
                    addToBot(new LoseEnergyAction(3));
                    return true;
                }
                return false;
            };
            e.upgrade = c -> {
                if (!c.upgraded) {
                    upgradeCardName(c);
                    updateCardDesc(c, "不能被打出 。 NL 抽到这张牌时失去3点能量。 NL 虚无 。");
                }
                return false;
            };
        }
    }
}
