package de.geolykt.s2dmenues.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import de.geolykt.s2dmenues.bridge.VelocityMovingStarGenerator;

@Mixin(targets = "snoddasmannen/galimulator/ProceduralStarGenerator$23")
public class PSGMovingFrameMixins implements VelocityMovingStarGenerator {
    @Shadow
    private float speed;

    @Override
    @Unique
    public float s2dmenues$getVelocity() {
        return this.speed;
    }

    @Override
    @Unique
    public void s2dmenues$setVelocity(float velocity) {
        this.speed = velocity;
    }
}
