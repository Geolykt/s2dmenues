package de.geolykt.s2dmenues.render.tosvg.path;

import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

public final class MovePathCommand extends PathCommand {

    @NonNull
    private final Vector2dc position;

    public MovePathCommand(boolean absolute, @NonNull Vector2dc position) {
        super(absolute);

        this.position = position;
    }
}
