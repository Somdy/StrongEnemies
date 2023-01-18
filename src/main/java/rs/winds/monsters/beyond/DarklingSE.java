package rs.winds.monsters.beyond;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.Logger;
import rs.lazymankits.utils.LMSK;
import rs.winds.core.King;
import rs.winds.vfx.SEAwakenedEyeParticle;

public class DarklingSE extends AbstractMonster {
    public static final String ID = King.MakeID(Darkling.ID);
    private static final MonsterStrings strings = King.MonsterStrings(Darkling.ID);
    private float particleTimer = 0F;
    private final Bone eye;
    
    public DarklingSE(float x, float y) {
        super("[二名] 白疾风小黑", ID, 275, 0F, -20F, 260F, 200F, null, x, y + 20F);
        loadAnimation("SEAssets/images/monsters/darkling/skeleton.atlas", "SEAssets/images/monsters/darkling/skeleton.json", 1F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        e.setTimeScale(MathUtils.random(0.75F, 1.0F));
        eye = skeleton.findBone("eye_main_L");
    }
    
    @Override
    public void takeTurn() {
        addToBot(new RollMoveAction(this));
    }
    
    @Override
    protected void getMove(int i) {
        setMove((byte) 0, Intent.UNKNOWN);
    }
    
    @Override
    public void update() {
        super.update();
        if (!isDying) {
            particleTimer -= Gdx.graphics.getDeltaTime();
            if (particleTimer < 0F) {
                particleTimer = 0.1F;
                AbstractDungeon.effectList.add(new SEAwakenedEyeParticle(skeleton.getX() + eye.getWorldX(), skeleton.getY() + eye.getWorldY(),
                        LMSK.Color(192, 0, 0)));
            }
        }
    }
    
    @SpirePatch2(clz = Darkling.class, method = "damage")
    public static class DarklingDamagePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"allDead"})
        public static void Insert(@ByRef boolean[] allDead) {
            for (AbstractMonster m : LMSK.GetAllExptMonsters(m -> DarklingSE.ID.equals(m.id))) {
                if (!m.halfDead) {
                    allDead[0] = false;
                    break;
                }
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(Logger.class, "info");
                int line = LineFinder.findAllInOrder(ctBehavior, matcher)[1];
                return new int[]{line};
            }
        }
    }
}