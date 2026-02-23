package de.geolykt.s2dmenues.components;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.github.tommyettinger.textra.TextraLabel;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.starloader.api.gui.Drawing;

public class DynamicTextraLabel extends TextraLabel {

    @Nullable
    private final Supplier<@NotNull String> textSupplier;

    public DynamicTextraLabel(@NotNull String text) {
        this(text, (Color) null);
    }

    public DynamicTextraLabel(@NotNull String text, @Nullable Color fontColor) {
        this(text, new LabelStyle(FontConfig.getInstance().getPreferredFont(), fontColor, null));
    }

    public DynamicTextraLabel(@NotNull String text, @NotNull LabelStyle textraLabelStyle) {
        super(text, textraLabelStyle);
        this.wrap = true;
        this.textSupplier = null;
    }

    public DynamicTextraLabel(@NotNull Supplier<@NotNull String> text) {
        this(text, (Color) null);
    }

    public DynamicTextraLabel(@NotNull Supplier<@NotNull String> text, @Nullable Color fontColor) {
        this(text, new LabelStyle(FontConfig.getInstance().getPreferredFont(), fontColor, null));
    }

    public DynamicTextraLabel(@NotNull Supplier<@NotNull String> text, @NotNull LabelStyle textraLabelStyle) {
        super(text.get(), textraLabelStyle);
        this.wrap = true;
        this.textSupplier = text;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Supplier<String> text = this.textSupplier;

        if (text != null) {
            this.setText(text.get());
        }

        this.setFont(FontConfig.getInstance().getPreferredFont(), true);

        if (this.getDebug()) {
            batch.draw(Drawing.getTextureProvider().getSinglePixelSquare(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        super.draw(batch, parentAlpha);
    }
}
