package de.geolykt.s2dmenues.components;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class FullViewportDrawable extends BaseDrawable {

    @NotNull
    private final Drawable delegate;

    public FullViewportDrawable(@NotNull Drawable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            throw new IllegalStateException("Something raised an GL20.GL_INVALID_VALUE before this method was called.");
        }

        IntBuffer viewport = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();

        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewport);

        int viewportX = viewport.get(0);
        int viewportY = viewport.get(1);
        int viewportW = viewport.get(2);
        int viewportH = viewport.get(3);

        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            throw new IllegalStateException("Gdx.gl.glGetIntegerv raised an GL20.GL_INVALID_VALUE - however that is possible!");
        }

        batch.end();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            throw new IllegalStateException("Gdx.gl.glViewport raised an GL20.GL_INVALID_VALUE after trying to set it!");
        }

        Matrix4 oldProjectionMatrix = batch.getProjectionMatrix().cpy();
        int fullWidth = Gdx.graphics.getBackBufferWidth();
        int fullHeight = Gdx.graphics.getBackBufferHeight();
        batch.setProjectionMatrix(new Matrix4().translate(-1F, -1F, 0F).scale(2F / fullWidth, 2F / fullHeight, 0F));
        batch.begin();

        this.delegate.draw(batch, 0, 0, fullWidth, fullHeight);
        batch.end();

        Gdx.gl.glViewport(viewportX, viewportY, viewportW, viewportH);

        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            throw new IllegalStateException("Gdx.gl.glViewport raised an GL20.GL_INVALID_VALUE after trying to reset it! X: " + viewportX + ", Y: " + viewportY + "; W: " + viewportW + ", H: " + viewportH);
        }

        batch.setProjectionMatrix(oldProjectionMatrix);
        batch.begin();
    }
}
