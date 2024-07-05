package de.geolykt.s2dmenues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.starloader.api.gui.Drawing;

public class Styles implements Disposable {

    @Nullable
    private static Styles instance;

    @NotNull
    public static Styles getInstance() {
        Styles instance = Styles.instance;
        if (instance == null) {
            Styles.instance = instance = new Styles();
        }
        return instance;
    }

    @NotNull
    public final TextButtonStyle buttonStyle;
    @NotNull
    public final TextButtonStyle cancelButtonStyle;
    @NotNull
    public final WindowStyle windowStyleTranslucent;
    @NotNull
    public final SplitPaneStyle splitPaneStyle;
    @NotNull
    public final ScrollPaneStyle scrollPaneStyle;

    private Styles() {
        this.buttonStyle = new TextButtonStyle();
        this.buttonStyle.font = Drawing.getSpaceFont();
        this.buttonStyle.fontColor = Color.WHITE;
        this.buttonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFE5B3EFF), 0.66F);
        this.buttonStyle.up.setMinWidth(300F);
        this.buttonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFF3814FF), 0.75F);
        this.buttonStyle.over.setMinWidth(300F);
        this.buttonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.buttonStyle.down.setMinWidth(300F);
        this.buttonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.buttonStyle.disabled.setMinWidth(300F);

        this.cancelButtonStyle = new TextButtonStyle();
        this.cancelButtonStyle.font = Drawing.getSpaceFont();
        this.cancelButtonStyle.fontColor = Color.WHITE;
        this.cancelButtonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xAF2020FF), 0.5F);
        this.cancelButtonStyle.up.setMinWidth(300F);
        this.cancelButtonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xAF7F7FFF), 0.5F);
        this.cancelButtonStyle.over.setMinWidth(300F);
        this.cancelButtonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0xAF0000FF), 0.5F);
        this.cancelButtonStyle.down.setMinWidth(300F);
        this.cancelButtonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.cancelButtonStyle.disabled.setMinWidth(300F);

        this.windowStyleTranslucent = new WindowStyle();
        this.windowStyleTranslucent.titleFont = Drawing.getSpaceFont();
        this.windowStyleTranslucent.titleFontColor = Color.WHITE;
        this.windowStyleTranslucent.background = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0xFF00007F), 0.5F);
        this.windowStyleTranslucent.stageBackground = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(new Color(0x80808080));

        this.splitPaneStyle = new SplitPaneStyle();
        this.splitPaneStyle.handle = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.splitPaneStyle.handle.setMinHeight(15F);

        this.scrollPaneStyle = new ScrollPaneStyle();
        this.scrollPaneStyle.vScrollKnob = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.LIGHT_GRAY);
        this.scrollPaneStyle.vScrollKnob.setMinHeight(8);
        this.scrollPaneStyle.vScrollKnob.setMinWidth(4);
    }

    public void dispose() {
        if (this == Styles.instance) {
            Styles.instance = null;
        }
    }
}
