package de.geolykt.s2dmenues.components;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Align;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.Drawing;

public class TextDrawable extends BaseDrawable {
    @NotNull
    private final CharSequence text;
    @NotNull
    private final Color color;
    private final int align;

    public TextDrawable(@NotNull CharSequence text) {
        this(text, NullUtils.requireNotNull(Color.WHITE));
    }

    public TextDrawable(@NotNull CharSequence text, @NotNull Color color) {
        this(text, color, Align.topLeft);
    }

    public TextDrawable(@NotNull CharSequence text, @NotNull Color color, int align) {
        super();
        this.text = text;
        this.color = color;
        this.align = align;
    }

    @Override
    public String toString() {
        return "TextDrawable['" + this.text + "']";
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        BitmapFont spaceFont = Drawing.getSpaceFont();
        GlyphLayout layout = new GlyphLayout(spaceFont, this.text, this.color, width, this.align, true);
        float drawY;
        if (Align.isTop(this.align)) {
            drawY = y + height;
        } else if (Align.isBottom(this.align)) {
            drawY = y + layout.height;
        } else {
            drawY = y + height/2 + layout.height/2;
        }
        spaceFont.draw(batch, layout, x, drawY);
    }
}
