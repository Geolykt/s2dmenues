package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class SmoothQuadraticPathCommand extends PathCommand {

    @NonNull
    private final Vector2dc endPoint;

    public SmoothQuadraticPathCommand(boolean absolute, @NonNull Vector2dc endPoint) {
        super(absolute);
        this.endPoint = endPoint;
    }
}
