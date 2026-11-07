package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class EllipticalArcPathCommand extends PathCommand {

    public static enum ArcSize {
        LARGE,
        SMALL;
    }

    public static enum ArcSweep {
        CLOCKWISE,
        COUNTERCLOCKWISE;
    }

    @NonNull
    private final Vector2dc radius;
    @NonNull
    private final Vector2dc endPoint;
    private final double rotationRad;
    @NonNull
    private final ArcSize size;
    @NonNull
    private final ArcSweep sweep;

    public EllipticalArcPathCommand(boolean absolute, @NonNull Vector2dc radius, double rotationRad, @NonNull ArcSize size, @NonNull ArcSweep sweep, @NonNull Vector2dc endPoint) {
        super(absolute);
        this.radius = radius;
        this.rotationRad = rotationRad;
        this.size = size;
        this.sweep = sweep;
        this.endPoint = endPoint;
    }
}
