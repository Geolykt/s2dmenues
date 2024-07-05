package de.geolykt.s2dmenues;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuStage extends Stage {

    @NotNull
    private final Drawable background;
    private final boolean disposeBackground;

    public MainMenuStage(@NotNull Drawable background, boolean disposeBackground) {
        super(new ScreenViewport());
        this.background = background;
        this.disposeBackground = disposeBackground;
    }

    @Override
    public void draw() {
        this.getBatch().setProjectionMatrix(this.getViewport().getCamera().projection);
        this.getBatch().setColor(1, 1, 1, 1);
        this.getBatch().begin();
        float w = this.getViewport().getWorldWidth();
        float h = this.getViewport().getWorldHeight();
        this.background.draw(this.getBatch(), -w/2, -h/2, w, h);
        this.getBatch().end();
        super.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!this.disposeBackground) {
            return;
        }
        if (this.background instanceof Disposable) {
            ((Disposable) this.background).dispose();
        } else if (this.background instanceof TextureRegionDrawable) {
            ((TextureRegionDrawable) this.background).getRegion().getTexture().dispose();
        }
    }
}
