package de.geolykt.s2dmenues;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.Gdx;

import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.EventPriority;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.lifecycle.ApplicationStartedEvent;
import de.geolykt.starloader.api.event.lifecycle.ApplicationStopEvent;
import de.geolykt.starloader.mod.Extension;

public class S2DMenues extends Extension {

    @Override
    public void initialize() {
        if (ApplicationStartedEvent.hasStarted()) {
            Gdx.app.postRunnable(MainMenuProvider::display);
        } else {
            EventManager.registerListener(new Listener() {
                @EventHandler(EventPriority.HIGH)
                public void onPostSart(@NotNull ApplicationStartedEvent evt) {
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
}
