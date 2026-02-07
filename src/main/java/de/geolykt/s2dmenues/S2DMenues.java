package de.geolykt.s2dmenues;

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
import de.geolykt.starloader.mod.Extension;

public class S2DMenues extends Extension {

    @NotNull
    public static final Path MOD_DATA_DIR = Starloader.getInstance().getModDirectory().resolve("s2dmenues");

    @Override
    public void initialize() {
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
        });
    }

    static {
        if (com.badlogic.gdx.Version.isLower(1, 14, 0)) {
            LoggerFactory.getLogger(S2DMenues.class).info("The runtime version of libGDX is out of date. Using Mass ASM for compatibility.");
            MinestomRootClassLoader.getInstance().addASMTransformer(new TextraMASMTransformer());
        }
    }
}
