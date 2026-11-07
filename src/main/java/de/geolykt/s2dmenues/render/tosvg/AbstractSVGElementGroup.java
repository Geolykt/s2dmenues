package de.geolykt.s2dmenues.render.tosvg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.joml.Matrix4dc;
import org.jspecify.annotations.NonNull;

import de.geolykt.s2dmenues.render.plutovg.PlutoVGCanvas;

abstract sealed class AbstractSVGElementGroup implements TOSVGElement permits TOSVGRootObject, SVGGroup {

    @NonNull
    private final List<@NonNull TOSVGElement> children = new ArrayList<>();

    protected void addChild(@NonNull TOSVGElement element) {
        this.children.add(Objects.requireNonNull(element));
    }

    @Override
    public void render(@NonNull PlutoVGCanvas target, @NonNull RectangleI viewport, @NonNull Matrix4dc parentViewMat) {
        for (TOSVGElement shape : this.children) {
            shape.render(target, viewport, parentViewMat);
        }
    }
}
