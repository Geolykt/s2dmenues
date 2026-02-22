package de.geolykt.s2dmenues.components;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.github.tommyettinger.textra.Styles.WindowStyle;
import com.github.tommyettinger.textra.TextraDialog;

import de.geolykt.s2dmenues.FontConfig;

public abstract class S2DDialog extends TextraDialog {
    public S2DDialog(@NotNull String title, @NotNull WindowStyle windowStyle) {
        super(title, windowStyle, FontConfig.getInstance().getPreferredFont());
    }

    @Override
    @NotNull
    protected DynamicTextraLabel newLabel(String text, Font font, @Nullable Color color) {
        return new DynamicTextraLabel(Objects.requireNonNull(text, "'text' may not be null"), color);
    }

    @Override
    @NotNull
    protected DynamicTextraLabel newLabel(String text, LabelStyle style) {
        return new DynamicTextraLabel(Objects.requireNonNull(text, "'text' may not be null"), Objects.requireNonNull(style, "'style' may not be null"));
    }
}
