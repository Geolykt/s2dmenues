package de.geolykt.s2dmenues.components.msdf;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.github.tommyettinger.textra.TextraButton;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.s2dmenues.RunnableClickListener;

/**
 * Convenience class to set up a {@link TextraButton} with a {@link RunnableClickListener} by default.
 */
public class RunnableTextraButton extends TextraButton {
    @NotNull
    private final Supplier<@NotNull String> textProvider;

    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Consumer<@NotNull RunnableTextraButton> action) {
        this(() -> text, style, action);
    }

    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Runnable action) {
        this(() -> text, style, action);
    }

    public RunnableTextraButton(@NotNull Supplier<@NotNull String> text, @NotNull TextButtonStyle style) {
        super(text.get(), style);
        this.getTextraLabel().wrap = true;
        this.setWidth(300F);
        this.setHeight(30F);
        this.textProvider = text;
    }

    public RunnableTextraButton(@NotNull Supplier<@NotNull String> text, @NotNull TextButtonStyle style, @NotNull Consumer<@NotNull RunnableTextraButton> action) {
        this(text, style);
        this.addListener(new RunnableClickListener(() -> {
            action.accept(this);
        }));
    }

    public RunnableTextraButton(@NotNull Supplier<@NotNull String> text, @NotNull TextButtonStyle style, @NotNull Runnable action) {
        this(text, style);
        this.addListener(new RunnableClickListener(action));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.setText(this.textProvider.get());
        this.getTextraLabel().setFont(FontConfig.getInstance().getPreferredFont());
        super.draw(batch, parentAlpha);
    }

    @Override
    public void setText(String text) {
        if (this.getText().equals(text)) {
            return;
        }

        super.setText(text);
    }
}
