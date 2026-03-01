package de.geolykt.s2dmenues.components.msdf;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.Styles.TextFieldStyle;
import com.github.tommyettinger.textra.TextraField;

import de.geolykt.s2dmenues.FontConfig;

public class DynamicTextraField extends TextraField {
    public DynamicTextraField(@NotNull String text, @NotNull TextFieldStyle style) {
        super(text, style);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.label.setFont(FontConfig.getInstance().getPreferredFont(), true);
        super.draw(batch, parentAlpha);
    }
}
