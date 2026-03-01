package de.geolykt.s2dmenues.components.event;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class RunnableClickListener extends InputListener {
    @NotNull
    private final Runnable runnable;

    public RunnableClickListener(@NotNull Runnable runnable) {
        this.runnable = Objects.requireNonNull(runnable, "'runnable' may not be null");
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true; // We need to return true in order to be able to listen for touchUp. Don't ask why - most likely an intended feature.
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        this.runnable.run();
    }
}
