package de.geolykt.s2dmenues.render.tosvg;

import org.joml.Matrix4dc;
import org.jspecify.annotations.NonNull;

import de.geolykt.s2dmenues.render.plutovg.PlutoVGCanvas;

sealed interface TOSVGElement permits AbstractSVGElementGroup, TOSVGShape {
    void render(@NonNull PlutoVGCanvas target, @NonNull RectangleI viewport, @NonNull Matrix4dc parentViewMat);
}
