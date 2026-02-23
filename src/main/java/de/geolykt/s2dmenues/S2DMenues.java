package de.geolykt.s2dmenues;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
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
import de.geolykt.starloader.mod.Extension;

public class S2DMenues extends Extension {

    public static final String MOD_ID = "s2dmenues"; // This is giving off FML vibes. Oh well, nostalgia is a good thing, no?

    @NotNull
    public static final Path MOD_DATA_DIR = Starloader.getInstance().getModDirectory().resolve(S2DMenues.MOD_ID);

    @Override
    public void initialize() {
        try {
            FontConfig.start(S2DMenues.MOD_DATA_DIR);
            S2DI18N.start();
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
            public void onStop(@NotNull ApplicationStopEvent evt) {
                try {
                    Styles.getInstance().dispose();
                    TextureCache.getInstance().dispose();
                } catch (RuntimeException e) {
                    S2DMenues.this.getLogger().error("Unable to dispose resources. The exception itself probably doesn't cause any harm, but it is adviseable to look into it's cause.", e);
                }
            }

            @EventHandler
            public void onAtlasStitch(@NotNull AtlasPackingEvent evt) throws IOException {
                FontConfig.getInstance().registerTextures(evt);
            }

            @EventHandler
            public void onAtlasSitched(@NotNull AtlasPackedEvent evt) throws IOException {
                FontConfig.getInstance().bakeFonts(evt);
            }
        });
    }

    static {
        if (com.badlogic.gdx.Version.isLower(1, 14, 0)) {
            LoggerFactory.getLogger(S2DMenues.class).info("The runtime version of libGDX is out of date. Using Mass ASM for compatibility.");
            MinestomRootClassLoader.getInstance().addASMTransformer(new TextraMASMTransformer());
        }
    }
}
