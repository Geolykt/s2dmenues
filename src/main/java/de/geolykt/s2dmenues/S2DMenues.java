package de.geolykt.s2dmenues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;

import de.geolykt.s2dmenues.asm.TextraMASMTransformer;
import de.geolykt.starloader.Starloader;
import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.EventPriority;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.lifecycle.ApplicationStartedEvent;
import de.geolykt.starloader.api.event.lifecycle.ApplicationStopEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackedEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent;
import de.geolykt.starloader.api.resource.DataFolderProvider;
import de.geolykt.starloader.mod.Extension;

public class S2DMenues extends Extension {

    @Nullable
    private static JSONObject loadedConfig;

    @NotNull
    public static final Path MOD_DATA_DIR = Starloader.getInstance().getModDirectory().resolve(S2DMenues.MOD_ID);

    public static final String MOD_ID = "s2dmenues"; // This is giving me FML vibes. Oh well, nostalgia is a good thing, no?

    @NotNull
    public static final AtomicBoolean MOD_OPTION_REVISED_GALAXY_TYPE_SELECTION = new AtomicBoolean(true);

    static {
        if (com.badlogic.gdx.Version.isLower(1, 14, 0)) {
            LoggerFactory.getLogger(S2DMenues.class).warn("The runtime version of libGDX is out of date. Using Mass ASM for compatibility.");
            MinestomRootClassLoader.getInstance().addASMTransformer(new TextraMASMTransformer());
        }
    }

    public static void loadConfig() throws IOException {
        Path loadedConfigPath = S2DMenues.MOD_DATA_DIR.resolve("config.json");

        @NotNull
        JSONObject loadedConfig;
        if (Files.notExists(loadedConfigPath)) {
            loadedConfig = new JSONObject();
        } else {
            try {
                loadedConfig = new JSONObject(new String(Files.readAllBytes(loadedConfigPath), StandardCharsets.UTF_8));
            } catch (JSONException | IOException e) {
                LoggerFactory.getLogger(S2DMenues.class).error("Unable to load currently present configuration file. Using default configurations instead.", e);
                loadedConfig = new JSONObject();
            }
        }

        JSONObject fontJson = loadedConfig.optJSONObject("font");
        JSONObject i18nJson = loadedConfig.optJSONObject("i18n");

        if (fontJson == null) {
            fontJson = new JSONObject();
        }

        if (i18nJson == null) {
            i18nJson = new JSONObject();
        }

        S2DMenues.MOD_OPTION_REVISED_GALAXY_TYPE_SELECTION.set(loadedConfig.optBoolean("revised-galaxy-type-selection", S2DMenues.MOD_OPTION_REVISED_GALAXY_TYPE_SELECTION.get()));

        S2DMenues.loadedConfig = loadedConfig;
        FontConfig.start(S2DMenues.MOD_DATA_DIR, fontJson);
        S2DI18N.start(i18nJson);
    }

    @NotNull
    public static String readStringFromResources(@NotNull String filepath) {
        Path resourceLocation = DataFolderProvider.getProvider().provideAsPath().resolve("mods").resolve(S2DMenues.MOD_ID).resolve(filepath);

        if (Files.notExists(resourceLocation)) {
            resourceLocation = S2DMenues.MOD_DATA_DIR.resolve(filepath);

            if (Files.notExists(resourceLocation)) {
                try (InputStream is = S2DMenues.class.getClassLoader().getResourceAsStream(filepath)) {
                    if (is == null) {
                        throw new IOException("Resource '" + filepath + "' is not located within the mod's classpath.");
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];

                    for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
                        baos.write(buffer, 0, read);
                    }

                    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to read string from jar", e);
                }
            }
        }

        try {
            return new String(Files.readAllBytes(resourceLocation), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static synchronized void saveConfig() {
        JSONObject loadedConfig = S2DMenues.loadedConfig;

        if (loadedConfig == null) {
            throw new IllegalStateException("'loadedConfig' is null. #saveConfig called before #loadConfig?");
        }

        loadedConfig.put("font", FontConfig.getInstance().saveConfig());
        loadedConfig.put("i18n", S2DI18N.saveConfig());
        loadedConfig.put("revised-galaxy-type-selection", S2DMenues.MOD_OPTION_REVISED_GALAXY_TYPE_SELECTION.get());

        try {
            Files.write(S2DMenues.MOD_DATA_DIR.resolve("config.json"), loadedConfig.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (JSONException | IOException e) {
            LoggerFactory.getLogger(S2DMenues.class).error("Cannot save configuration file.", e);
        }
    }

    @Override
    public void initialize() {
        try {
            Files.createDirectories(S2DMenues.MOD_DATA_DIR);
            S2DMenues.loadConfig();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (ApplicationStartedEvent.hasStarted()) {
            Gdx.app.postRunnable(MainMenuProvider::display);
        } else {
            EventManager.registerListener(new Listener() {
                @EventHandler(EventPriority.HIGH)
                public void onPostStart(@NotNull ApplicationStartedEvent evt) {
                    MainMenuProvider.display();
                }
            });
        }

        EventManager.registerListener(new Listener() {
            @EventHandler
            public void onAtlasSitched(@NotNull AtlasPackedEvent evt) throws IOException {
                FontConfig.getInstance().bakeFonts(evt);
            }

            @EventHandler
            public void onAtlasStitch(@NotNull AtlasPackingEvent evt) throws IOException {
                FontConfig.getInstance().registerTextures(evt);
            }

            @EventHandler
            public void onStop(@NotNull ApplicationStopEvent evt) {
                try {
                    Styles.getInstance().dispose();
                    TextureCache.getInstance().dispose();
                } catch (RuntimeException e) {
                    S2DMenues.this.getLogger().error("Unable to dispose resources. The exception itself probably doesn't cause any harm, but it is adviseable to look into it's cause.", e);
                }
            }
        });
    }
}
