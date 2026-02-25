package de.geolykt.s2dmenues.bridge;

import org.jetbrains.annotations.NotNull;

import de.geolykt.s2dmenues.S2DI18N.ConfiguredTranslateable;

public interface I18NCapable {
    @NotNull
    ConfiguredTranslateable s2dmenues$getLocalisation();
}
