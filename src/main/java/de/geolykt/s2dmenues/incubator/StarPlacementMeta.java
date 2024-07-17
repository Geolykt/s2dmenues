package de.geolykt.s2dmenues.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.api.CoordinateGrid;

/**
 * The {@link StarPlacementMeta} stores metadata about the location of a star,
 * as well as other metadata that is attached to the location of a star.
 * This presently includes star movement paths, if present. But in the future
 * it may also include other related metadata such as the star's name (this could
 * be useful for naming stars after a city at a given location).
 *
 * <p>More precisely, the {@link StarPlacementMeta} contains all data generated
 * for a given star by a {@link StarPlacementGenerator}.
 *
 * <p>This object primarily is intended for use in the galaxy generation process,
 * as well as UIs previewing the galaxy generation process. After generating a
 * galaxy this object can be quite meaningless, which is why in general instances
 * of this class will not be held in memory for long.
 */
public class StarPlacementMeta {

    private final float originX;
    private final float originY;
    @Nullable
    private final StarMovementPath path;

    @Contract(pure = true)
    public StarPlacementMeta(float x, float y, @Nullable StarMovementPath path) {
        this.originX = x;
        this.originY = y;
        this.path = path;
    }

    /**
     * Obtains the X-coordinate of where the star should be created.
     * The coordinates are in board coordinates, as per {@link CoordinateGrid#BOARD}.
     *
     * <p>For stars which have movement paths defined, the coordinate of a star
     * may drift over time. This method only defines where the star should initially
     * be located at, but after the star's creation, this value can be meaningless.
     *
     * <p>Note that even for stars with no movements paths the stars can be manually be
     * repositioned by the player (albeit only through the sandbox mode). Henceforth
     * even in that case the value should not be relied upon after the star's creation.
     *
     * @return The x-value of the coordinate where the star should be initially placed.
     */
    @Contract(pure = true, value = "-> _")
    public float getX() {
        return this.originX;
    }

    /**
     * Obtains the Y-coordinate of where the star should be created.
     * The coordinates are in board coordinates, as per {@link CoordinateGrid#BOARD}.
     *
     * <p>For stars which have movement paths defined, the coordinate of a star
     * may drift over time. This method only defines where the star should initially
     * be located at, but after the star's creation, this value can be meaningless.
     *
     * <p>Note that even for stars with no movements paths the stars can be manually be
     * repositioned by the player (albeit only through the sandbox mode). Henceforth
     * even in that case the value should not be relied upon after the star's creation.
     *
     * @return The y-value of the coordinate where the star should be initially placed.
     */
    @Contract(pure = true, value = "-> _")
    public float getY() {
        return this.originY;
    }

    /**
     * Obtains the star's movement path, which dictates how a star should move when it
     * is being ticked after the world creation process. This method returns null if no
     * movement path is defined, however the star's position could still be mutated by
     * other gameplay mechanics during live play.
     *
     * <p>The {@link StarMovementPath} is likely to be unique to the specific star, as
     * different stars are likely to have (slightly) different paths.
     *
     * @return The star's movement path, or null if not present
     */
    @Nullable
    @Contract(pure = true, value = "-> _")
    public StarMovementPath getPath() {
        return this.path;
    }
}
