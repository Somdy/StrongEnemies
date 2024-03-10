package rs.winds.relics;

import basemod.CustomEventRoom;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.events.ColosseumSE;
import rs.winds.ui.campfire.InvitationOption;

import java.util.ArrayList;

public class SERInvitation extends CustomRelic {
    public static final String ID = King.MakeID("SERInvitation");
    public boolean colosseum;
    
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
        colosseum = false;
        if (!grayscale && !usedUp && counter > -2) {
            options.add(new InvitationOption(true, this));
        }
    }
    
    public void recharge() {
        flash();
        grayscale = false;
        usedUp = false;
        counter = -1;
    }
    
    @Override
    public void usedUp() {
        grayscale = true;
        usedUp = true;
        counter = -99;
    }
}