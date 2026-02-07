package de.geolykt.s2dmenues.components;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.github.tommyettinger.textra.TextraButton;

import de.geolykt.s2dmenues.RunnableClickListener;

/**
 * Convenience class to set up a {@link TextraButton} with a {@link RunnableClickListener} by default.
 */
public class RunnableTextraButton extends TextraButton {
    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Runnable action) {
        super(text, style);
        this.addListener(new RunnableClickListener(action));
    }

    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Consumer<@NotNull RunnableTextraButton> action) {
        super(text, style);
        this.addListener(new RunnableClickListener(() -> {
            action.accept(this);
        }));
        this.setWidth(300F);
        this.setHeight(30F);
    }
}
