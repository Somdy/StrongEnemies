package rs.winds.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.powers.BarricadePower;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;

public class SERBarricade extends CustomRelic {
    public static final String ID = King.MakeID("SERBarricade");
    
    public SERBarricade() {
        super(ID, ImageMaster.loadImage("SEAssets/images/relics/barricade.png"), 
                ImageMaster.loadImage("SEAssets/images/relics/outline/barricade.png"), RelicTier.SPECIAL, LandingSound.FLAT);
    }
    
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
    
    @Override
    public void onEquip() {
        LMSK.Player().increaseMaxHp(30, true);
    }
    
    @Override
    public void atBattleStart() {
        addToBot(new RelicAboveCreatureAction(LMSK.Player(), this));
        addToBot(new ApplyPowerAction(LMSK.Player(), LMSK.Player(), new BarricadePower(LMSK.Player())));
    }
}