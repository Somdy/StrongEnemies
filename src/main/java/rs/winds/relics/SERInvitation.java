package rs.winds.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.ui.campfire.InvitationOption;

import java.util.ArrayList;

public class SERInvitation extends CustomRelic {
    public static final String ID = King.MakeID("SERInvitation");
    
    public SERInvitation() {
        super(ID, ImageMaster.loadImage("SEAssets/images/relics/invitation.png"), 
                ImageMaster.loadImage("SEAssets/images/relics/outline/invitation.png"), RelicTier.SPECIAL, LandingSound.FLAT);
    }
    
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
    
    @Override
    public void addCampfireOption(ArrayList<AbstractCampfireOption> options) {
        options.add(new InvitationOption(true));
    }
}