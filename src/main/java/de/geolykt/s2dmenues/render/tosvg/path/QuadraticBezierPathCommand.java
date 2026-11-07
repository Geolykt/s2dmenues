package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class QuadraticBezierPathCommand extends PathCommand {

    @NonNull
    private final Vector2dc endPoint;
    @NonNull
    private final Vector2dc controlPoint;

    public QuadraticBezierPathCommand(boolean absolute, @NonNull Vector2dc controlPoint, @NonNull Vector2dc endPoint) {
        super(absolute);
        this.controlPoint = controlPoint;
        this.endPoint = endPoint;
    }
}
