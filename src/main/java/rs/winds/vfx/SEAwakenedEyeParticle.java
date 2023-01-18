package rs.winds.vfx;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.vfx.AwakenedEyeParticle;

public class SEAwakenedEyeParticle extends AwakenedEyeParticle {
    public SEAwakenedEyeParticle(float x, float y, Color color) {
        super(x, y);
        this.color = color.cpy();
    }
}