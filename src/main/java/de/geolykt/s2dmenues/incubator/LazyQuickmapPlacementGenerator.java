package de.geolykt.s2dmenues.incubator;

import java.nio.file.Path;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.api.NamespacedKey;

import snoddasmannen.galimulator.FloatmapStarGenerator;
import snoddasmannen.galimulator.MapData;

public class LazyQuickmapPlacementGenerator implements StarPlacementGenerator {

    @NotNull
    private final String bitmapPath;
    @Nullable
    private FloatmapStarGenerator floatmapStarGenerator;
    @NotNull
    private final Path quickmapPath;
    @NotNull
    private final NamespacedKey registryKey;

    public LazyQuickmapPlacementGenerator(@NotNull Path quickmapPath, @NotNull String bitmapPath, @NotNull NamespacedKey registryKey) {
        this.quickmapPath = quickmapPath;
        this.bitmapPath = bitmapPath;
        this.registryKey = registryKey;
    }

    @Override
    @NotNull
    public Collection<@NotNull StarPlacementMeta> generatePlacements(int count, float maxX, float maxY) {
        FloatmapStarGenerator starGenerator = this.floatmapStarGenerator;
        if (starGenerator == null) {
            starGenerator = new FloatmapStarGenerator(this.bitmapPath, 100);
            this.floatmapStarGenerator = starGenerator;
        }
        return VanillaStarGeneratorWrapper.generatePlacements(starGenerator, count, maxX, maxY);
    }

    @Override
    @NotNull
    public String getDisplayCategory() {
        return "Quickmaps";
    }

    @Override
    @NotNull
    public String getDisplayName() {
        String filename = this.quickmapPath.getFileName().toString();
        int dotindex = filename.lastIndexOf('.');
        if (dotindex < 0) {
            return filename;
        }
        return filename.substring(0, dotindex);
    }

    @Override
    @NotNull
    public NamespacedKey getRegistryKey() {
        return this.registryKey;
    }

    @Override
    public void setRegistryKey(@NotNull NamespacedKey key) {
        if (this.registryKey.equals(key)) {
            return;
        }
        throw new UnsupportedOperationException("Cannot alter registry key of wrapper");
    }

    @Override
    @NotNull
    @Deprecated
    public de.geolykt.starloader.api.@NotNull Map toLegacyMap() {
        if (this.floatmapStarGenerator == null) {
            this.floatmapStarGenerator = new FloatmapStarGenerator(this.bitmapPath, 100);
        }
        return (de.geolykt.starloader.api.Map) new MapData(this.floatmapStarGenerator);
    }
}
