package de.geolykt.s2dmenues.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.s2dmenues.bridge.MovingSpiralStarGenerator;

@Mixin(targets = "snoddasmannen/galimulator/ProceduralStarGenerator$25")
public class PSGMovingSpiralMixins implements MovingSpiralStarGenerator {
    @Shadow
    private float coreSize;
    @Shadow
    private float orbitalFudge;
    @Shadow
    private float speed;
    @Shadow
    private float undulation;

    @Override
    public float s2dmenues$getCoreSize() {
        return this.coreSize;
    }

    @Override
    public float s2dmenues$getOrbitalFudge() {
        return this.orbitalFudge;
    }

    @Override
    public float s2dmenues$getSpeed() {
        return this.speed;
    }

    @Override
    public float s2dmenues$getUndulation() {
        return this.undulation;
    }

    @Override
    public void s2dmenues$setCoreSize(float value) {
        this.coreSize = value;
    }

    @Override
    public void s2dmenues$setOrbitalFudge(float value) {
        this.orbitalFudge = value;
    }

    @Override
    public void s2dmenues$setSpeed(float value) {
        this.speed = value;
    }

    @Override
    public void s2dmenues$setUndulation(float value) {
        this.undulation = value;
    }
}
