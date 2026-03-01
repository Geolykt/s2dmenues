package de.geolykt.s2dmenues.components.msdf;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.github.tommyettinger.textra.Styles.WindowStyle;
import com.github.tommyettinger.textra.TextraDialog;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.s2dmenues.components.event.ModalDialogZIndexChangedEvent;

public abstract class S2DDialog extends TextraDialog {
    public S2DDialog(@NotNull String title, @NotNull WindowStyle windowStyle) {
        super(title, windowStyle, FontConfig.getInstance().getPreferredFont());
    }

    public S2DDialog(@NotNull Supplier<@NotNull String> title, @NotNull WindowStyle windowStyle) {
        super(title.get(), windowStyle, FontConfig.getInstance().getPreferredFont());
        ((DynamicTextraLabel) this.getTitleLabel()).setTextSupplier(title);
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

    @Override
    public boolean setZIndex(int index) {
        if (super.setZIndex(index)) {
            ModalDialogZIndexChangedEvent event = new ModalDialogZIndexChangedEvent();
            event.setZIndex(index);

            this.fire(event);

            return true;
        }

        return false;
    }
}
