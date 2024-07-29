package de.geolykt.s2dmenues;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuStage extends Stage {

    @NotNull
    private final Drawable background;
    private final List<Runnable> cleanupHandlers = new ArrayList<>();
    private final boolean disposeBackground;

    public MainMenuStage(@NotNull Drawable background, boolean disposeBackground) {
        // TODO the viewport induces harder to read text; perhaps we should use a scaling font such as MSDF or plainly
        // a font with a more adapted resolution. MSDF might be a good try - we shall see.
        super(new FitViewport(Gdx.graphics.getWidth() - 160, Gdx.graphics.getHeight() - 90));

        this.background = background;
        this.disposeBackground = disposeBackground;
    }

    public void addCleanupHandler(@NotNull Runnable handler) {
        this.cleanupHandlers.add(handler);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.cleanupHandlers.forEach(Runnable::run);
        if (!this.disposeBackground) {
            return;
        }

        if (this.background instanceof Disposable) {
            ((Disposable) this.background).dispose();
        } else if (this.background instanceof TextureRegionDrawable) {
            ((TextureRegionDrawable) this.background).getRegion().getTexture().dispose();
        }
    }

    @Override
    public void draw() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        Matrix4 projectionMatrix = this.getBatch().getProjectionMatrix().set(
                -1F, -1F, 0,
                0, 0, 0, 0,
                2F / w, 2F / h, 0
        );

        this.getBatch().setProjectionMatrix(projectionMatrix);
        this.getBatch().setColor(1, 1, 1, 1);
        Gdx.gl20.glViewport(0, 0, w, h);
        this.getBatch().begin();
        this.background.draw(this.getBatch(), 0, 0, w, h);
        this.getBatch().end();

        this.getViewport().apply();
        super.draw();
    }
}
