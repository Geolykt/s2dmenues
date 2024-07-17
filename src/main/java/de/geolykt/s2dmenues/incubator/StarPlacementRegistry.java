package de.geolykt.s2dmenues.incubator;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.lifecycle.RegistryRegistrationEvent;
import de.geolykt.starloader.api.registry.Registry;
import de.geolykt.starloader.api.resource.DataFolderProvider;

import snoddasmannen.galimulator.FractalStarGenerator;
import snoddasmannen.galimulator.ProceduralStarGenerator;

@Internal
public class StarPlacementRegistry extends Registry<StarPlacementGenerator> {
    public static final StarPlacementRegistry GENERATOR_REGISTRY = new StarPlacementRegistry();

    private StarPlacementRegistry() {
        LoggerFactory.getLogger(StarPlacementRegistry.class).info("Registering star placement generators");

        for (ProceduralStarGenerator psg : ProceduralStarGenerator.values()) {
            NamespacedKey key = NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_" + psg.name());
            this.register(key, new VanillaStarGeneratorWrapper(psg, key));
        }

        {
            NamespacedKey key = NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_FRACTAL");
            this.register(key, new VanillaStarGeneratorWrapper(new FractalStarGenerator(), key));
        }

        Path dataDir = DataFolderProvider.getProvider().provideAsPath();
        {
            Path quickmapsDirectory = dataDir.resolve("maps/quickmaps/");
            try (Stream<Path> files = Files.list(quickmapsDirectory)) {
                Iterator<Path> it = files.iterator();
                while (it.hasNext()) {
                    Path path = it.next();
                    if (Files.isDirectory(path)) {
                        continue;
                    }

                    try {
                        NamespacedKey key = NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_QUICKMAP_" + path.getFileName().toString().split("\\.", 2)[0].toUpperCase(Locale.ROOT).replace('-', '_'));
                        this.register(key, new LazyQuickmapPlacementGenerator(path, Paths.get("").toAbsolutePath().relativize(path.toAbsolutePath()).toString(), key));
                    } catch (RuntimeException e) {
                        LoggerFactory.getLogger(StarPlacementRegistry.class).error("Unable to load quickmap from file '{}'", path, e);
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(StarPlacementRegistry.class).error("Unable to list quickmaps: An unexpected I/O error occured", e);
            }
        }

        {
            Path mapsDirectory = dataDir.resolve("maps/");
            try (Stream<Path> files = Files.list(mapsDirectory)) {
                Iterator<Path> it = files.iterator();
                while (it.hasNext()) {
                    Path path = it.next();
                    if (!path.getFileName().toString().endsWith(".map") || Files.isDirectory(path)) {
                        continue;
                    }

                    Properties properties = new Properties();
                    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        properties.load(reader);
                        String name = properties.getProperty("name", path.getFileName().toString());
                        NamespacedKey key = NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_MAPDATA_" + name.toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_'));
                        this.register(key, new LazyMapdataPlacementGenerator(path, name, key));
                    } catch (RuntimeException | IOException e) {
                        LoggerFactory.getLogger(StarPlacementRegistry.class).error("Unable to load mapdata-based map from file '{}'", path, e);
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(StarPlacementRegistry.class).error("Unable to list quickmaps: An unexpected I/O error occured", e);
            }
        }

        EventManager.handleEvent(new RegistryRegistrationEvent(StarPlacementRegistry.GENERATOR_REGISTRY, StarPlacementGenerator.class, "STAR_PLACEMENT_GENERATOR"));
    }

    @Override
    @Nullable
    @Deprecated
    public StarPlacementGenerator getIntern(@NotNull String key) {
        throw new UnsupportedOperationException("Registry does not wrap an enum.");
    }

    @Override
    public void register(@NotNull NamespacedKey key, @NotNull StarPlacementGenerator value) {
        super.keyedValues.put(key, value);
    }

    @SuppressWarnings("null")
    @NotNull
    @UnmodifiableView
    public Collection<StarPlacementGenerator> valuesView() {
        return Collections.unmodifiableCollection(super.keyedValues.values());
    }
}
