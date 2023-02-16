package rs.winds.abstracts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import rs.lazymankits.abstracts.LMCustomPower;
import rs.winds.core.King;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSEPower extends LMCustomPower {
    @FunctionalInterface
    public interface PreloadPowerStringFunction {
        void pre(String[] rawString);
    }
    
    public static final Pattern POWER_NAME = Pattern.compile("\\[(owner)]", Pattern.MULTILINE);
    public static final Pattern POWER_AMT = Pattern.compile("\\[(amt_(\\d))]", Pattern.MULTILINE);
    public static final Pattern POWER_TARGET = Pattern.compile("\\[(crt_(\\d))]", Pattern.MULTILINE);
    
    private PreloadPowerStringFunction func = null;
    protected PowerStrings powerStrings;
    protected String NAME;
    protected String[] DESCRIPTIONS;
    private String owner_name;
    private final String[] crt_names;
    private final String[] amts;
    
    public AbstractSEPower(String ID, String img, PowerType type, AbstractCreature owner) {
        this.ID = ID;
        this.type = type;
        this.owner = owner;
        crt_names = new String[3];
        amts = new String[3];
        Arrays.fill(crt_names, "missing_name");
        Arrays.fill(amts, "missing_value");
        initLocals();
        loadImg(img);
    }
    
    protected void initLocals() {
        powerStrings = King.PowerStrings(ID);
        NAME = powerStrings.NAME;
        DESCRIPTIONS = powerStrings.DESCRIPTIONS;
        this.name = NAME;
    }
    
    protected void preloadString(PreloadPowerStringFunction func) {
        this.func = func;
    }
    
    protected String getString() {
        String[] rawString = new String[] {DESCRIPTIONS[0]};
        if (func != null) func.pre(rawString);
        return rawString[0];
    }
    
    @Override
    protected void loadImg(String name) {
        if (Gdx.files.internal("SEAssets/images/powers/128/" + name + ".png").exists()) {
            region128 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("SEAssets/images/powers/128/" + name + ".png"),
                    0, 0, 128, 128);
            region48 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("SEAssets/images/powers/48/" + name + ".png"),
                    0, 0, 48, 48);
        }
        if (region128 == null || region48 == null)
            loadRegion(name);
    }
    
    protected void setValues(int amount) {
        super.setValues(owner, null, amount);
        setOwnerName();
    }
    
    protected void setValues(int amount, int extraAmt) {
        super.setValues(owner, null, amount, extraAmt);
        setOwnerName();
    }
    
    protected void setValues(AbstractCreature source, int amount) {
        super.setValues(owner, source, amount);
        setOwnerName();
    }
    
    protected void setValues(AbstractCreature source, int amount, int extraAmt) {
        super.setValues(owner, source, amount, extraAmt);
        setOwnerName();
    }
    
    protected void setOwnerName() {
        owner_name = owner.isPlayer ? cpr().getLocalizedCharacterName() : owner.name;
    }
    
    protected void setAmtValue(int slot, int value) {
        if (slot > amts.length - 1)
            slot = amts.length - 1;
        amts[slot] = String.valueOf(value);
    }
    
    protected void setAmtValue(int slot, float value) {
        if (slot > amts.length - 1)
            slot = amts.length - 1;
        amts[slot] = String.valueOf(value);
    }
    
    protected void setCrtName(int slot, String value) {
        if (slot > crt_names.length - 1)
            slot = crt_names.length - 1;
        crt_names[slot] = value;
    }
    
    @Override
    public void updateDescription() {
        String rawString = getString();
        description = checkWithPatterns(rawString);
    }
    
    protected String checkWithPatterns(String origin) {
        origin = checkOwnerName(origin);
        origin = checkAmtValue(origin);
        origin = checkCrtValue(origin);
        return origin;
    }
    
    private String checkAmtValue(String origin) {
        final Matcher matcher = POWER_AMT.matcher(origin);
        while (matcher.find() && matcher.groupCount() >= 2) {
            int slot = Integer.parseInt(matcher.group(2));
            if (slot <= amts.length - 1) {
                origin = origin.replace(matcher.group(0), amts[slot]);
            }
        }
        return origin;
    }
    
    private String checkCrtValue(String origin) {
        final Matcher matcher = POWER_TARGET.matcher(origin);
        while (matcher.find() && matcher.groupCount() >= 2) {
            int slot = Integer.parseInt(matcher.group(2));
            if (slot <= crt_names.length - 1) {
                origin = origin.replace(matcher.group(0), crt_names[slot]);
            }
        }
        return origin;
    }
    
    private String checkOwnerName(String origin) {
        final Matcher matcher = POWER_NAME.matcher(origin);
        if (matcher.find())
            origin = origin.replace(matcher.group(0), owner_name);
        return origin;
    }
    
    @Override
    protected TextureAtlas getPowerAtlas() {
        return null;
    }
    
    public void onMonsterDeath(AbstractMonster target) {}
    public void onPlayerExhaustCard(AbstractCard card) {}
}