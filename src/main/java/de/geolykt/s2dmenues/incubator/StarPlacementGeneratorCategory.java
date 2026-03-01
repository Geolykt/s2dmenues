package de.geolykt.s2dmenues.incubator;

import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.S2DI18N.ConfiguredTranslateable;
import de.geolykt.s2dmenues.bridge.I18NCapable;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.registry.RegistryKeyed;

/**
 * A category that contains one of, or multiple {@link StarPlacementGenerator}.
 *
 * <p>This allows star placement generators to be segmented more closely.
 *
 * <p>Two categories are considered distinct if they do not share identities
 * (as per the identity comparison operator {@code ==}), whilst also not being
 * equal to each other (as per {@link Object#equals(Object)}). Two categories
 * are considered the same if {@link Object#equals(Object)} is {@code true}.
 * However, they might also not share identities.
 */
public class StarPlacementGeneratorCategory implements RegistryKeyed, I18NCapable {

    @NotNull
    public static final StarPlacementGeneratorCategory FRACTAL = StarPlacementGeneratorCategory.makeBuiltinCategory("fractal");

    @NotNull
    public static final StarPlacementGeneratorCategory MAPS_ON_DISK = StarPlacementGeneratorCategory.makeBuiltinCategory("diskmaps");

    @NotNull
    public static final StarPlacementGeneratorCategory MISC = StarPlacementGeneratorCategory.makeBuiltinCategory("misc");

    @NotNull
    public static final StarPlacementGeneratorCategory PROCEDURAL = StarPlacementGeneratorCategory.makeBuiltinCategory("procedural");

    @NotNull
    public static final StarPlacementGeneratorCategory PROCEDURAL_MOVING = StarPlacementGeneratorCategory.makeBuiltinCategory("procedural_moving");

    @NotNull
    public static final StarPlacementGeneratorCategory QUICKMAPS = StarPlacementGeneratorCategory.makeBuiltinCategory("quickmaps");

    @NotNull
    @Contract(pure = false, value = "null -> fail; !null -> new")
    private static final StarPlacementGeneratorCategory makeBuiltinCategory(@NotNull String name) {
        NamespacedKey registryKey = NamespacedKey.fromString("s2dmenues", "registries.generators.categories." + Objects.requireNonNull(name, "'name' may not be null").toLowerCase(Locale.ROOT));
        return new StarPlacementGeneratorCategory(registryKey, S2DI18N.translate(registryKey));
    }

    @NotNull
    private final NamespacedKey registryKey;

    @NotNull
    private final ConfiguredTranslateable translateable;

    public StarPlacementGeneratorCategory(@NotNull NamespacedKey registryKey, @NotNull ConfiguredTranslateable translateable) {
        this.registryKey = registryKey;
        this.translateable = translateable;
        StarPlacementRegistry.CATEGORY_REGISTRY.register(registryKey, this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StarPlacementGeneratorCategory)) {
            return false;
        }

        return this.registryKey.equals(((StarPlacementGeneratorCategory) obj).registryKey);
    }

    @Override
    @NotNull
    public NamespacedKey getRegistryKey() {
        return this.registryKey;
    }

    @Override
    public int hashCode() {
        return this.registryKey.hashCode();
    }

    @Override
    @NotNull
    public ConfiguredTranslateable s2dmenues$getLocalisation() {
        return this.translateable;
    }

    @Override
    public void setRegistryKey(@NotNull NamespacedKey key) {
        if (!this.registryKey.equals(key)) {
            throw new IllegalArgumentException("Currently set registry key " + this.registryKey + " does not match set registry key " + key);
        }
    }
}
