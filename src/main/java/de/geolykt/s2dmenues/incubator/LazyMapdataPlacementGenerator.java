package de.geolykt.s2dmenues.incubator;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.files.FileHandle;

import de.geolykt.starloader.api.NamespacedKey;

import snoddasmannen.galimulator.MapData;

public class LazyMapdataPlacementGenerator implements StarPlacementGenerator {

    @Nullable
    private MapData mapData;
    @NotNull
    private final String mapName;
    @NotNull
    private final Path mapPath;
    @NotNull
    private final NamespacedKey registryKey;

    public LazyMapdataPlacementGenerator(@NotNull Path mapPath, @NotNull String mapName, @NotNull NamespacedKey registryKey) {
        this.mapPath = mapPath;
        this.mapName = mapName;
        this.registryKey = registryKey;
    }

    @Override
    @NotNull
    public Collection<@NotNull StarPlacementMeta> generatePlacements(int count, float maxX, float maxY) {
        MapData mapData = this.mapData;
        if (mapData == null) {
            mapData = new MapData(new FileHandle(this.mapPath.toFile()));
            this.mapData = mapData;
        }
        return VanillaStarGeneratorWrapper.generatePlacements(Objects.requireNonNull(mapData.getGenerator()), count, maxX, maxY);
    }

    @Override
    @NotNull
    public String getDisplayCategory() {
        return "Maps on disk";
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return this.mapName;
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
        MapData mapData = this.mapData;
        if (mapData == null) {
            mapData = new MapData(new FileHandle(this.mapPath.toFile()));
            this.mapData = mapData;
        }

        return (de.geolykt.starloader.api.Map) mapData;
    }
}
