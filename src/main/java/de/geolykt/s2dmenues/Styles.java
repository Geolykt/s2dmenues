package de.geolykt.s2dmenues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.components.FullViewportDrawable;
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
    public final TextButtonStyle confirmButtonStyle;
    @NotNull
    public final LabelStyle labelStyleGeneric;
    @NotNull
    public final ScrollPaneStyle scrollPaneStyle;
    @NotNull
    public final SplitPaneStyle splitPaneStyle;
    @NotNull
    public final TextFieldStyle textFieldStyle;
    @NotNull
    public final WindowStyle windowStylePlastic;
    @NotNull
    public final WindowStyle windowStyleTranslucent;
    @NotNull
    public final WindowStyle windowStyleMainMenu;

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

        this.confirmButtonStyle = new TextButtonStyle();
        this.confirmButtonStyle.font = Drawing.getSpaceFont();
        this.confirmButtonStyle.fontColor = Color.WHITE;
        this.confirmButtonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x20AF20FF), 0.5F);
        this.confirmButtonStyle.up.setMinWidth(300F);
        this.confirmButtonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x7FAF7FFF), 0.5F);
        this.confirmButtonStyle.over.setMinWidth(300F);
        this.confirmButtonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x00AF0000FF), 0.5F);
        this.confirmButtonStyle.down.setMinWidth(300F);
        this.confirmButtonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x7C489AFF), 0.5F);
        this.confirmButtonStyle.disabled.setMinWidth(300F);

        this.windowStyleTranslucent = new WindowStyle();
        this.windowStyleTranslucent.titleFont = Drawing.getSpaceFont();
        this.windowStyleTranslucent.titleFontColor = Color.WHITE;
        this.windowStyleTranslucent.background = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0xFF00007F), 0.5F);

        {
            // Eclipse is acting very very strange today. Further analysis may be required.
            Drawable tmp = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(new Color(0x80808080));
            if (tmp == null) {
                LoggerFactory.getLogger(Styles.class).error("new keyword yielded null??? Check your memory.");
                throw new InternalError();
            }
            this.windowStyleTranslucent.stageBackground = new FullViewportDrawable(tmp);
        }

        this.windowStylePlastic = new WindowStyle();
        this.windowStylePlastic.titleFont = Drawing.getSpaceFont();
        this.windowStylePlastic.titleFontColor = Color.WHITE;
        this.windowStylePlastic.background = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x7F7F7FFF), 0.5F);

        {
            Drawable tmp = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(new Color(0x80808080));
            if (tmp == null) {
                LoggerFactory.getLogger(Styles.class).error("new keyword yielded null??? Check your memory.");
                throw new InternalError();
            }
            this.windowStylePlastic.stageBackground = new FullViewportDrawable(tmp);
        }

        this.splitPaneStyle = new SplitPaneStyle();
        this.splitPaneStyle.handle = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.splitPaneStyle.handle.setMinHeight(15F);

        this.scrollPaneStyle = new ScrollPaneStyle();
        this.scrollPaneStyle.vScrollKnob = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.LIGHT_GRAY);
        this.scrollPaneStyle.vScrollKnob.setMinHeight(8);
        this.scrollPaneStyle.vScrollKnob.setMinWidth(4);

        this.textFieldStyle = new TextFieldStyle();
        this.textFieldStyle.font = Drawing.getSpaceFont();
        this.textFieldStyle.fontColor = Color.WHITE;
        this.textFieldStyle.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(Color.LIGHT_GRAY), 0.66F);
        this.textFieldStyle.background.setLeftWidth(10F);
        this.textFieldStyle.cursor = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.textFieldStyle.cursor.setMinWidth(3F);

        this.labelStyleGeneric = new LabelStyle(Drawing.getSpaceFont(), Color.WHITE);

        this.windowStyleMainMenu = new WindowStyle();
        this.windowStyleMainMenu.titleFont = Drawing.getSpaceFont();
        this.windowStyleMainMenu.titleFontColor = Color.WHITE;
        this.windowStyleMainMenu.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0.8F, 0.5F, 0.5F, 0.6F), 0.2F);
    }

    public void dispose() {
        if (this == Styles.instance) {
            Styles.instance = null;
        }
    }
}
