package de.geolykt.s2dmenues.components;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Layout;

import de.geolykt.s2dmenues.FontConfig;

public class MSDFTextDrawable extends BaseDrawable {
    private final int align;
    @NotNull
    private final Color color;
    @NotNull
    private final CharSequence text;

    public MSDFTextDrawable(@NotNull CharSequence text) {
        this(text, Objects.requireNonNull(Color.WHITE));
    }

    public MSDFTextDrawable(@NotNull CharSequence text, @NotNull Color color) {
        this(text, color, Align.topLeft);
    }

    public MSDFTextDrawable(@NotNull CharSequence text, @NotNull Color color, int align) {
        super();
        this.text = text;
        this.color = color;
        this.align = align;
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        Layout layout = new Layout(FontConfig.getInstance().getPreferredFont());

        layout.setTargetWidth(width);
        layout.setBaseColor(this.color);
        layout.getFont().markup(this.text.toString(), layout);

        float drawY;
        if (Align.isTop(this.align)) {
            drawY = y + height - layout.getLine(0).height;
        } else if (Align.isBottom(this.align)) {
            drawY = y + layout.getHeight();
        } else {
            drawY = y + height / 2 + layout.getHeight() / 2;
        }

        ShaderProgram shader = batch.getShader();
        ShaderProgram fontShader = layout.getFont().shader;

        if (fontShader != null) {
            batch.setShader(fontShader);
        }

        layout.getFont().drawGlyphs(batch, layout, x, drawY, this.align);

        if (fontShader != null) {
            batch.setShader(shader);
        }
    }

    @Override
    public String toString() {
        return "MSDFTextDrawable['" + this.text + "']";
    }
}
