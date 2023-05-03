package rs.winds.rewards;

import basemod.abstracts.CustomReward;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Apotheosis;
import com.megacrit.cardcrawl.cards.green.Nightmare;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import rs.lazymankits.utils.LMSK;

import java.util.ArrayList;

import static rs.winds.patches.SEEnums.ApoRewardType;
import static rs.winds.patches.SEEnums.NightmareRewardType;

public class NightmareReward extends CustomReward {
    private final ArrayList<AbstractCard> apo = new ArrayList<>();
    
    public NightmareReward() {
        super(ImageMaster.REWARD_CARD_NORMAL, RewardItem.TEXT[2], NightmareRewardType);
        Nightmare card = new Nightmare();
        for (AbstractRelic r : LMSK.Player().relics) {
            r.onPreviewObtainCard(card);
        }
        apo.add(card);
    }
    
    @Override
    public boolean claimReward() {
        AbstractDungeon.cardRewardScreen.open(apo, this, RewardItem.TEXT[4]);
        AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        return false;
    }
}