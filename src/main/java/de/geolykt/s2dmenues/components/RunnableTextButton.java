package de.geolykt.s2dmenues.components;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import de.geolykt.s2dmenues.RunnableClickListener;

/**
 * Convenience class to set up a {@link TextButton} with a {@link RunnableClickListener} by default.
 */
public class RunnableTextButton extends TextButton {

    public RunnableTextButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Runnable action) {
        super(text, style);
        this.addListener(new RunnableClickListener(action));
    }

    public RunnableTextButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Consumer<RunnableTextButton> action) {
        super(text, style);
        this.addListener(new RunnableClickListener(() -> {
            action.accept(this);
        }));
        this.setWidth(300F);
        this.setHeight(30F);
    }
}
