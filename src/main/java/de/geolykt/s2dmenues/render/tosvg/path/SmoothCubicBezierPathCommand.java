package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class SmoothCubicBezierPathCommand extends PathCommand {

    @NonNull
    private final Vector2dc endControlPoint;
    // The start control point is the current point or the previous endControlPoint of the previous cubic bezier command, if applicable
    @NonNull
    private final Vector2dc endPoint;

    public SmoothCubicBezierPathCommand(boolean absolute, @NonNull Vector2dc endControlPoint, @NonNull Vector2dc endPoint) {
        super(absolute);
        this.endControlPoint = endControlPoint;
        this.endPoint = endPoint;
    }

}
