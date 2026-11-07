package de.geolykt.s2dmenues.render.tosvg;

import java.lang.foreign.Arena;
import java.util.List;

import org.jetbrains.annotations.Unmodifiable;
import org.joml.Matrix4dc;
import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.math.Rectangle;

import de.geolykt.s2dmenues.render.plutovg.PlutoVGCanvas;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGColor;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGMatrix;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGPath;
import de.geolykt.s2dmenues.render.tosvg.path.PathCommand;

public final class TOSVGPath extends TOSVGShape {
    @Unmodifiable
    @NonNull
    public final List<@NonNull PathCommand> commands;

    @NonNull
    private final String cCommands;

    public TOSVGPath(@Unmodifiable @NonNull List<@NonNull PathCommand> commands, @NonNull String cCommands) {
        this.commands = commands;
        this.cCommands = cCommands;
    }

    @Override
    public void render(@NonNull PlutoVGCanvas target, @NonNull RectangleI viewport, @NonNull Matrix4dc parentViewMat) {
        try (PlutoVGPath path = new PlutoVGPath(this.cCommands); Arena arena = Arena.ofConfined()) {
            target.setOpacity(1F);
            target.setMatrix(PlutoVGMatrix.fromJOML(arena, this.getProjectionMatrix(viewport)));

            target.setColor(new PlutoVGColor(arena, this.getFillColor()));
            target.fill(path);
            target.setColor(new PlutoVGColor(arena, this.getStrokeColor()));
            target.stroke(path);
        }
    }

    @Override
    @NonNull
    protected Rectangle getExtents() {
        try (PlutoVGPath path = new PlutoVGPath(this.cCommands)) {
            return path.getBounds(true);
        }
    }
}
