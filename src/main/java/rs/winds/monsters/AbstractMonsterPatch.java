package rs.winds.monsters;

import basemod.AutoAdd;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import org.clapper.util.classutil.*;
import rs.winds.core.King;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class AbstractMonsterPatch {
    private static URL myJarUrl;
    @SpirePatch2(clz = CardCrawlGame.class, method = "render")
    public static class VanillaMonsterPatch {
        @SpireRawPatch
        public static void Raw(CtBehavior ctBehavior) throws Exception {
            King.PatchLog("Begin editing monster classes");
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
            ClassFilter alterFilter = new OrClassFilter(new AutoAdd.PackageFilter(SEVMonsterEditorManaged.class));
            ClassFilter classFilter = new AndClassFilter(alterFilter, new AnnotatedClassFilter(SEMonsterEditor.class, pool));
            int classesToEdit = finder.findClasses(classesFound, classFilter);
            King.PatchLog("Found [" + classesToEdit + "] editor classes");
            if (!classesFound.isEmpty()) {
                for (ClassInfo classInfo : classesFound) {
                    CtClass eClz = pool.get(classInfo.getClassName());
                    SEMonsterEditor annotation = (SEMonsterEditor) eClz.getAnnotation(SEMonsterEditor.class);
                    Class<? extends AbstractMonster> mClz = annotation.m();
                    if (mClz != null && annotation.functional()) {
                        classesToEdit--;
                        boolean hasExtraFunctions = annotation.hasExtraFunctions();
                        MonsterEditor.InjectMethods(pool, mClz, classInfo.getClassName(), hasExtraFunctions);
//                        King.PatchLog("Class [" + mClz.getSimpleName() + "] edited by [" + classInfo.getClassName() + "]");
                    }
                    if (!annotation.functional()) {
                        King.PatchLog("[" + classInfo.getClassName() + "] not functional");
                    }
                }
            }
//            MonsterEditor.InjectMethods(pool, AcidSlime_S.class, SEVanillaMonsterPatch.AcidSlimeS.class);
        }
    }
    
    protected static class AnnotatedClassFilter extends ClassModifiersClassFilter {
        private final Class<? extends Annotation> annotationClass;
        private final ClassPool pool;
        
        public AnnotatedClassFilter(Class<? extends Annotation> annotationClass, ClassPool pool) {
            super(Modifier.PUBLIC | Modifier.STATIC);
            this.annotationClass = annotationClass;
            this.pool = pool;
        }
    
        @Override
        public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
            if (!super.accept(classInfo, classFinder)) return false;
            boolean hasAnnotation = false;
            try {
                CtClass ctClass = pool.getCtClass(classInfo.getClassName());
                hasAnnotation = ctClass.getAnnotation(annotationClass) != null;
            } catch (NotFoundException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return hasAnnotation;
        }
    }
    
    @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CLASS)
    public static class TrackerField {
        public static SpireField<MonsterEditor> mTracker = new SpireField<>(MonsterEditor::new);
    }
    
    @SpirePatch2(clz = AbstractMonster.class, method = SpirePatch.CONSTRUCTOR, 
            paramtypez = {String.class, String.class, int.class, float.class, float.class, float.class, float.class, 
                    String.class, float.class, float.class, boolean.class})
    public static class ConstructorEdit {
        @SpirePostfixPatch
        public static void AddTracker(AbstractMonster __instance) {
            TrackerField.mTracker.get(__instance).assign(__instance);
        }
    }
}