package rs.winds.monsters;

import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SEMonsterEditor {
    Class<? extends AbstractMonster> m();
    boolean functional() default true;
    boolean hasExtraFunctions() default false;
}