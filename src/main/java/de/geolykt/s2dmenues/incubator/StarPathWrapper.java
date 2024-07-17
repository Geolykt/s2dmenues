package de.geolykt.s2dmenues.incubator;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.math.Vector2;

import snoddasmannen.galimulator.StarPath;

public class StarPathWrapper implements StarMovementPath {
    @NotNull
    private final StarPath path;
    private float pathAngle;
    private final float pathAngleVelocity;
    private final float sclX;
    private final float sclY;

    public StarPathWrapper(@NotNull StarPath path, snoddasmannen.galimulator.Star star, float sclX, float sclY) {
        this.path = path;
        this.pathAngle = star.pathAngle;
        this.pathAngleVelocity = star.getPathAngleVelocity();
        this.sclX = sclX;
        this.sclY = sclY;
    }

    @SuppressWarnings("null")
    @Override
    @NotNull
    public Vector2 update(@NotNull Vector2 location) {
        this.path.a();
        this.pathAngle += this.pathAngleVelocity;
        return location.set(this.path.b(this.pathAngle)).scl(this.sclX, this.sclY);
    }
}
