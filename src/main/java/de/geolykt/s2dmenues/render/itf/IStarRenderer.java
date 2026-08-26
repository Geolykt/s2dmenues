package de.geolykt.s2dmenues.render.itf;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.Batch;

import de.geolykt.starloader.api.empire.Star;

/**
 * The {@link IStarRenderer} interface renders the stars themselves.
 *
 * <p>This render process is done after rendering star regions, but before
 * drawing starlanes, actors, sprawl, UI, et cetera.
 */
public interface IStarRenderer {

    /**
     * Render the given stars onto the surface described by the given {@link Batch}.
     *
     * <p>Note that whilst stars have been culled so they are in rough proximity of the frustum,
     * certain stars can be rather far away from the frustum, up to 32 granularity factors
     * for non-rotated cameras and for rotated cameras (or when applying 3D transformations on the camera)
     * the distance might be even larger. However, at the same time, it should be noted that
     * stars are not infinitely small, so if further (more exact) frustum culling is required,
     * the sizes of the stars need to be taken into account accordingly.
     *
     * <p>When performing any kind of culling operation, as well as rendering operations overall,
     * it should be taken note that the board might be projected using 3D transformations. Implementations
     * should take care to properly scale according to this fact.
     *
     * <p>Like all render methods, this method is called asynchronously.
     *
     * <p>Implementations should make sure to bind their shaders as needed. The currently bound
     * shader might not match {@link Batch#getShader()} for the given surface.
     *
     * @param stars The stars that should be rendered.
     * @param surface The surface onto which rendering operations should be performed. The necessary transformation
     * matrices have already been set in prior operations to the given surface.
     */
    void renderStars(@NotNull List<@NotNull Star> stars, @NotNull Batch surface);
}
