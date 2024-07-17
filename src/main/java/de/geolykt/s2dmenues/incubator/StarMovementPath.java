package de.geolykt.s2dmenues.incubator;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.math.Vector2;

/**
 * An object that defines how a star should move when it is being ticked.
 * While this interface's {@link #update(Vector2) update method} takes in
 * an origin position and will mutate said position and return it as an output,
 * the method may also mutate the current {@link StarMovementPath} instance.
 * This behaviour stems from the way that galimulator's underlying implementation
 * of this feature is performed.
 *
 * <p>As such, {@link StarMovementPath} can be described as being "stateful",
 * with it's methods relying on this state. However, the internal state is
 * not exposed via this interface at this point in time.
 *
 * <p>Not intended to be implemented manually as of yet.
 */
@ApiStatus.Experimental
public interface StarMovementPath {

    /**
     * Updates the location {@link Vector2 vector} of the star which follows this
     * {@link StarMovementPath}, mutating the input vector as a result of this operation
     * and returning it.
     *
     * <p>Although the input vector describes the current position of the star,
     * implementors may choose to completely ignore the current state and blindly
     * overwrite the values within the vector. As such the input location is more
     * of a guidance rather than an authoritative source of the star's position.
     *
     * @param location The vector storing the star's position which should be mutated.
     * @return The mutated input vector, for chaining purposes.
     */
    @NotNull
    @Contract(pure = false, mutates = "this,param1", value = "!null -> param1; null -> fail")
    Vector2 update(@NotNull Vector2 location);

}
