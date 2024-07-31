package de.geolykt.s2dmenues.components;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.maltaisn.msdfgdx.FontStyle;
import com.maltaisn.msdfgdx.MsdfFont;
import com.maltaisn.msdfgdx.MsdfShader;
import com.maltaisn.msdfgdx.widget.MsdfLabel;

public class MSDFTextButton extends RunnableTextButton {

    public static class MSDFButtonStyle extends TextButtonStyle {
        @NotNull
        public final MsdfFont font;
        @NotNull
        public final FontStyle fontStyleUp;

        public MSDFButtonStyle(@NotNull MsdfFont font, @NotNull FontStyle fontStyleUp) {
            this.font = font;
            this.fontStyleUp = fontStyleUp;
            super.font = font.getFont();
        }
    }

    @NotNull
    private final MSDFButtonStyle style;

    public MSDFTextButton(@NotNull String text, @NotNull MSDFButtonStyle style, @NotNull Consumer<MSDFTextButton> action) {
        super(text, style, v -> action.accept((MSDFTextButton) v));

        this.style = style;
        Skin skin = new Skin();
        skin.add(this.style.fontStyleUp.getFontName(), this.style.font);
        skin.add("default", new MsdfShader());
        Label label = new MsdfLabel(text, skin, this.style.fontStyleUp);
        label.setAlignment(Align.center);
        this.setLabel(label);
    }

    public MSDFTextButton(@NotNull String text, @NotNull MSDFButtonStyle style, @NotNull Runnable action) {
        super(text, style, action);

        this.style = style;
        Skin skin = new Skin();
        skin.add(this.style.fontStyleUp.getFontName(), this.style.font);
        skin.add("default", new MsdfShader());
        Label label = new MsdfLabel(text, skin, this.style.fontStyleUp);
        label.setAlignment(Align.center);
        this.setLabel(label);
    }

    @Override
    @NotNull
    public MSDFButtonStyle getStyle() {
        return this.style;
    }
}
