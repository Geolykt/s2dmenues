package de.geolykt.s2dmenues.components;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.TextraButton;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.s2dmenues.RunnableClickListener;

/**
 * Convenience class to set up a {@link TextraButton} with a {@link RunnableClickListener} by default.
 */
public class RunnableTextraButton extends TextraButton {
    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Consumer<@NotNull RunnableTextraButton> action) {
        super(text, style);
        this.getTextraLabel().wrap = true;
        this.addListener(new RunnableClickListener(() -> {
            action.accept(this);
        }));
        this.setWidth(300F);
        this.setHeight(30F);
    }

    public RunnableTextraButton(@NotNull String text, @NotNull TextButtonStyle style, @NotNull Runnable action) {
        super(text, style);
        this.getTextraLabel().wrap = true;
        this.addListener(new RunnableClickListener(action));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.getTextraLabel().setFont(FontConfig.getInstance().getPreferredFont());
        super.draw(batch, parentAlpha);
    }
}
