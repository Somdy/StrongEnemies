package rs.winds.abstracts;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.stances.AbstractStance;
import rs.lazymankits.abstracts.LMCustomCard;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.winds.core.King;

public abstract class AbstractSECard extends LMCustomCard implements LMGameGeneralUtils {
    
    protected CardStrings strings;
    protected String NAME;
    protected String DESCRIPTION;
    protected String UPGRADE_DESCRIPTION;
    
    public AbstractSECard(String id, String img, int cost, CardType type, CardColor color, CardRarity rarity, CardTarget target) {
        super(id, "undefined", King.CardImage(img), cost, "undefined", type, color, rarity, target);
        strings = King.CardStrings(id);
        NAME = strings.NAME;
        DESCRIPTION = strings.DESCRIPTION;
        UPGRADE_DESCRIPTION = strings.UPGRADE_DESCRIPTION;
        initLocals();
    }
    
    private void initLocals() {
        name = NAME;
        rawDescription = DESCRIPTION;
        initializeTitle();
        initializeDescription();
    }
    
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        play(p, m);
    }
    
    protected abstract void play(AbstractCreature s, AbstractCreature t);
    
    protected void upgradeTexts(String newTitle) {
        if (newTitle != null) name = newTitle;
        upgradeName();
        if (UPGRADE_DESCRIPTION != null) {
            rawDescription = UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
    
    protected void upgradeTexts() {
        upgradeTexts(null);
    }
    
    public void onChangeStance(AbstractStance oldStance, AbstractStance newStance) {
        
    }
}