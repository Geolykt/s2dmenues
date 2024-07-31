package de.geolykt.s2dmenues.incubator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.NamespacedKey;

import snoddasmannen.galimulator.FractalStarGenerator;
import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.ProceduralStarGenerator;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.StarGenerator;
import snoddasmannen.galimulator.StarPath;

public class VanillaStarGeneratorWrapper implements StarPlacementGenerator {

    @NotNull
    static Collection<@NotNull StarPlacementMeta> generatePlacements(@NotNull StarGenerator generator, int count, float maxX, float maxY) {
        float starCountOld = Space.starCount;
        Space.starCount = count;
        generator.prepareGenerator();
        Collection<@NotNull StarPlacementMeta> out = new ArrayList<>();
        float sclX = maxX / generator.getMaxX();
        float sclY = maxY / generator.getMaxY();

        while (count-- != 0) {
            snoddasmannen.galimulator.Star star = generator.generateStar();
            float x = (float) (star.x * sclX);
            float y = (float) (star.y * sclY);
            StarMovementPath path;
            StarPath galimPath = star.getPath();
            if (galimPath == null) {
                path = null;
            } else {
                path = new StarPathWrapper(galimPath, star, sclX, sclY);
            }
            StarPlacementMeta meta = new StarPlacementMeta(x, y, path);
            out.add(meta);
        }

        Space.starCount = starCountOld;
        return out;
    }

    @NotNull
    private final StarGenerator generator;

    @NotNull
    private final NamespacedKey key;

    public VanillaStarGeneratorWrapper(@NotNull StarGenerator generator, @NotNull NamespacedKey key) {
        this.generator = generator;
        this.key = key;
    }

    @Override
    @NotNull
    public Collection<@NotNull StarPlacementMeta> generatePlacements(int count, float maxX, float maxY) {
        return VanillaStarGeneratorWrapper.generatePlacements(this.generator, count, maxX, maxY);
    }

    @Override
    @NotNull
    public String getDisplayCategory() {
        if (this.generator instanceof ProceduralStarGenerator) {
            return this.generator.hasMovingStars() ? "Procedural moving" : "Procedural";
        } else if (this.generator instanceof FractalStarGenerator) {
            return "Fractal";
        } else {
            return "Uncategorised";
        }
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return Objects.requireNonNull(this.generator.name());
    }

    @Override
    @NotNull
    public NamespacedKey getRegistryKey() {
        return this.key;
    }

    @Override
    public void setRegistryKey(@NotNull NamespacedKey key) {
        if (this.key.equals(key)) {
            return;
        }
        throw new UnsupportedOperationException("Cannot alter registry key of wrapper");
    }

    @Override
    @NotNull
    @Deprecated
    public de.geolykt.starloader.api.@NotNull Map toLegacyMap() {
        return (de.geolykt.starloader.api.Map) new MapData(this.generator);
    }
}
