package de.geolykt.s2dmenues.components.event;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.utils.Disposable;

public class ActorLifecycle implements Disposable {
    @NotNull
    private final Actor actor;

    @NotNull
    private final List<@NotNull Entry<@NotNull Actor, @NotNull EventListener>> listeners = new ArrayList<>();

    @NotNull
    private final Stage stage;

    public ActorLifecycle(@NotNull Stage stage, @NotNull Actor actor) {
        this.stage = stage;
        this.actor = actor;

        this.stage.addActor(actor);
        this.stage.setKeyboardFocus(actor);
        this.stage.setScrollFocus(actor);
    }

    @Override
    public void dispose() {
        for (@NotNull Entry<@NotNull Actor, @NotNull EventListener> entry : this.listeners) {
            entry.getKey().removeListener(entry.getValue());
        }

        this.listeners.clear();

        this.stage.getRoot().removeActor(this.actor, true);
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = " -> this")
    public ActorLifecycle disposeOnUnfocus() {
        this.registerListener(this.actor, (listenedEvent) -> {
            if (listenedEvent instanceof FocusEvent && !((FocusEvent) listenedEvent).isFocused()) {
                this.dispose();
            }

            return false;
        });

        this.registerStageListener((listenedEvent) -> {
            if (listenedEvent instanceof ModalDialogZIndexChangedEvent && ((ModalDialogZIndexChangedEvent) listenedEvent).getZIndex() != 0) {
                this.dispose();
            }
            return false;
        });

        return this;
    }

    @NotNull
    @Contract(pure = false, mutates = "this, param1", value = "_, _ -> this")
    public ActorLifecycle registerListener(@NotNull Actor listenerOwner, @NotNull EventListener listener) {
        this.listeners.add(new SimpleImmutableEntry<>(listenerOwner, listener));
        listenerOwner.addListener(listener);

        return this;
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = "_ -> this")
    public ActorLifecycle registerStageListener(@NotNull EventListener listener) {
        return this.registerListener(Objects.requireNonNull(this.stage.getRoot(), "Stage#getRoot may not be null"), listener);
    }
}
