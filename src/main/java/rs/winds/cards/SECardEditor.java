package rs.winds.cards;

import com.megacrit.cardcrawl.cards.AbstractCard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SECardEditor {
    Class<? extends AbstractCard> card();
    boolean functional() default true;
    String[] extraMethods() default {};
    
    String APPLYPOWERS = "applyPowers";
    String PST_APPLYPOWERS = "postApplyPowers";
    String CALCDAMAGE = "calculateCardDamage";
    String PST_CALCDAMAGE = "postCalcDamage";
    String ONEXHAUST = "triggerOnExhaust";
    String ONDRAWN = "triggerWhenDrawn";
}