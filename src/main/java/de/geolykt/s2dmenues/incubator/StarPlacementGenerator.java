package de.geolykt.s2dmenues.incubator;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.registry.RegistryKeyed;

/**
 * The {@link StarPlacementGenerator} defines where stars should be generated
 * as well as other metadata described by {@link StarPlacementMeta} such as
 * Star movement paths.
 *
 * <p>Not intended to be implemented manually as of yet.
 */
@ApiStatus.Experimental
public interface StarPlacementGenerator extends RegistryKeyed {
    /**
     * Generate <code>count</code> locations from stars, with the position of the stars
     * being defined by the semantics of this {@link StarPlacementGenerator} instance.
     *
     * <p>The locations of stars should be constrained between <code>-maxX</code>/<code>-maxY
     * </code> and <code>maxX</code>/<code>maxY</code>. While implementors should ensure
     * that these constraints are not violated (even when accounting for the {@link StarMovementPath}),
     * callers of this method should expect that implementors could go out of bounds. This
     * is most crucially the case with the vanilla "moving spiral" generator, where out-of-bounds
     * stars occur rather frequently and can in some circumstances even be forced to occur.
     *
     * <p>This method should not be invoked asynchronously. Further, it may mutate the internal
     * state of the {@link StarPlacementGenerator}, although implementors are recommended to
     * try to encapsulate the generator enough, but the vanilla star placement generators
     * are not implemented in a way where they care about our design/consistency requirements.
     *
     * <p>The random source/seed used for generation is left undefined.
     *
     * @param count The amount of stars to generate
     * @param maxX The maximum X coordinate to place stars in
     * @param maxY The maximum Y coordinate to place stars in
     * @return The generated star locations.
     */
    @NotNull
    Collection<@NotNull StarPlacementMeta> generatePlacements(int count, float maxX, float maxY);

    /**
     * Obtains the user-friendly display name of the {@link StarPlacementGenerator}, as it is
     * being displayed on UIs.
     *
     * @return The human-friendly display name.
     */
    @NotNull
    String getDisplayName();

    /**
     * Obtains the user-friendly display name of the category of the {@link StarPlacementGenerator}, as it is
     * being displayed on UIs. This is used to batch together similar generators, as otherwise
     * interfaces would become too cluttered.
     *
     * @return The human-friendly display name of the category wherein this generator lies in.
     */
    @NotNull
    String getDisplayCategory();

    @NotNull
    @Deprecated
    de.geolykt.starloader.api.@NotNull Map toLegacyMap();
}
