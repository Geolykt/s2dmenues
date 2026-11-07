package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class CubicBezierPathCommand extends PathCommand {

    @NonNull
    private final Vector2dc endControlPoint;
    @NonNull
    private final Vector2dc endPoint;
    @NonNull
    private final Vector2dc startControlPoint;

    public CubicBezierPathCommand(boolean absolute, @NonNull Vector2dc startControlPoint, @NonNull Vector2dc endControlPoint, @NonNull Vector2dc endPoint) {
        super(absolute);
        this.startControlPoint = startControlPoint;
        this.endControlPoint = endControlPoint;
        this.endPoint = endPoint;
    }
}
