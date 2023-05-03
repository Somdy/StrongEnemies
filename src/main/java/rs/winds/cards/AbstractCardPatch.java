package rs.winds.cards;

import basemod.AutoAdd;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import org.clapper.util.classutil.*;
import rs.winds.core.King;
import rs.winds.monsters.AbstractMonsterPatch;
import rs.winds.monsters.MonsterEditor;
import rs.winds.monsters.SEMonsterEditor;
import rs.winds.monsters.SEVMonsterEditorManaged;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AbstractCardPatch {
    private static URL myJarUrl;
    @SpirePatch2(clz = CardCrawlGame.class, method = "render")
    public static class VanillaMonsterPatch {
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws Exception {
            King.PatchLog("Begin editing card classes");
            for (ModInfo info : Loader.MODINFOS) {
                if (info != null && King.MOD_ID.equals(info.ID)) {
                    myJarUrl = info.jarURL;
                    break;
                }
            }
            ClassPool pool = ctBehavior.getDeclaringClass().getClassPool();
            ClassFinder finder = new ClassFinder();
            finder.add(new File(myJarUrl.toURI()));
            List<ClassInfo> classesFound = new ArrayList<>();
            ClassFilter alterFilter = new OrClassFilter(new AutoAdd.PackageFilter(SEVCardEditorManaged.class));
            ClassFilter classFilter = new AndClassFilter(alterFilter, new AbstractMonsterPatch.AnnotatedClassFilter(SECardEditor.class, pool));
            int classesToEdit = finder.findClasses(classesFound, classFilter);
            King.PatchLog("Found [" + classesToEdit + "] card editor classes");
            if (!classesFound.isEmpty()) {
                for (ClassInfo classInfo : classesFound) {
                    CtClass eClz = pool.get(classInfo.getClassName());
                    SECardEditor annotation = (SECardEditor) eClz.getAnnotation(SECardEditor.class);
                    Class<? extends AbstractCard> mClz = annotation.card();
                    if (mClz != null && annotation.functional()) {
                        classesToEdit--;
                        String[] extraMethods = annotation.extraMethods();
                        boolean hasExtraFunctions = extraMethods != null && extraMethods.length > 0;
                        CardEditor.InjectMethods(pool, mClz, classInfo.getClassName(), extraMethods, hasExtraFunctions);
//                        King.PatchLog("Class [" + mClz.getSimpleName() + "] edited by [" + classInfo.getClassName() + "]");
                    }
                    if (!annotation.functional()) {
                        King.PatchLog("[" + classInfo.getClassName() + "] not functional");
                    }
                }
            }
        }
    }
    
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class TrackerField {
        public static SpireField<CardEditor> cTracker = new SpireField<>(CardEditor::new);
    }
    
    @SpirePatch2(clz = AbstractCard.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, String.class, int.class, String.class, AbstractCard.CardType.class,
                    AbstractCard.CardColor.class, AbstractCard.CardRarity.class, AbstractCard.CardTarget.class, DamageInfo.DamageType.class})
    public static class ConstructorEdit {
        @SpirePostfixPatch
        public static void AddTracker(AbstractCard __instance) {
            TrackerField.cTracker.get(__instance).assign(__instance);
        }
    }
}
