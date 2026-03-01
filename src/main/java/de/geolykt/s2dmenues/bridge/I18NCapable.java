package de.geolykt.s2dmenues.bridge;

import org.jetbrains.annotations.NotNull;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.S2DI18N.ConfiguredTranslateable;

/**
 * The {@link I18NCapable} interface represents an object which has a name or singular text entry
 * that can be localised, or in other words translated via S2DMenues's {@link S2DI18N} infrastructure.
 *
 * @since 0.3.0
 */
public interface I18NCapable {
    @NotNull
    ConfiguredTranslateable s2dmenues$getLocalisation();
}
