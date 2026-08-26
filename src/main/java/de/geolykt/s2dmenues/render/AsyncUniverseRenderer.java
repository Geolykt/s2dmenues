package de.geolykt.s2dmenues.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.components.drawables.FullViewportDrawable;
import de.geolykt.s2dmenues.render.aur.AURStarRenderer;
import de.geolykt.s2dmenues.render.aur.AURStarlaneRenderer;
import de.geolykt.s2dmenues.render.itf.IStarRenderer;
import de.geolykt.s2dmenues.render.itf.IStarlaneRenderer;
import de.geolykt.s2dmenues.render.itf.IStarlaneRenderer.StarlanePrototype;
import de.geolykt.scs.SCSCoreLogic;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;
import de.geolykt.starloader.api.gui.Drawing;

import snoddasmannen.galimulator.GalFX;

public class AsyncUniverseRenderer extends Widget implements Disposable {

    private static final float CULL_EDGE_WIDTH = AsyncUniverseRenderer.GRANULARITY_FACTOR * 32;
    public static final float GRANULARITY_FACTOR = 0.035F;

    private static class AURGestureHandler extends GestureAdapter {
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            GalFX.panTranslate(-deltaX, deltaY);

            return true;
        }
    }

    private static class AURInputAdapter extends InputAdapter {
        @Override
        public boolean scrolled(float amountX, float amountY) {
            GalFX.zoom((amountX + amountY) * 0.1F + 1F);

            return true;
        }
    }

    @NotNull
    private IStarRenderer starRenderer = new AURStarRenderer();

    @NotNull
    private IStarlaneRenderer starlaneRenderer = new AURStarlaneRenderer();

    private boolean disposed = false;

    public void registerInputListeners() {
        Gdx.input.setInputProcessor(new InputMultiplexer(Gdx.input.getInputProcessor(), new GestureDetector(new AURGestureHandler()), new AURInputAdapter()));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (this.disposed) {
            throw new IllegalStateException("Illegal attempt of use-after-free");
        }

        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            try {
                throw new IllegalStateException("Something raised an GL20.GL_INVALID_VALUE before this method was called.");
            } catch (IllegalStateException e) {
                LoggerFactory.getLogger(FullViewportDrawable.class).error("Attempting to recover from potentially fatal GL error!", e);
            }
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

        this.renderUniverse(batch);

        batch.end();

        Gdx.gl.glViewport(viewportX, viewportY, viewportW, viewportH);

        if (Gdx.gl.glGetError() == GL20.GL_INVALID_VALUE) {
            throw new IllegalStateException("Gdx.gl.glViewport raised an GL20.GL_INVALID_VALUE after trying to reset it! X: " + viewportX + ", Y: " + viewportY + "; W: " + viewportW + ", H: " + viewportH);
        }

        batch.setProjectionMatrix(oldProjectionMatrix);
        batch.begin();
    }

    private void renderUniverse(@NotNull Batch batch) {
        @NotNull Star[] stars = Galimulator.getUniverse().getStarsView().toArray(new @NotNull Star[0]);
        Rectangle frustumMBR = Drawing.getBoardCameraAABB();
        Rectangle cullArea = new Rectangle(frustumMBR.x - AsyncUniverseRenderer.CULL_EDGE_WIDTH, frustumMBR.y - AsyncUniverseRenderer.CULL_EDGE_WIDTH, frustumMBR.getWidth() + AsyncUniverseRenderer.CULL_EDGE_WIDTH * 2, frustumMBR.getHeight() + AsyncUniverseRenderer.CULL_EDGE_WIDTH * 2);
        List<@NotNull Star> starsCulled = new ArrayList<>(); // Stars that survived the culling process
        SequencedSet<@NotNull StarlanePrototype> starlanes = new TreeSet<>();

        for (Star star : stars) {
            if (cullArea.contains(star.getCoordinates())) {
                starsCulled.add(star);

                @NotNull Star[] neighbours = star.getNeighbourList().toArray(new @NotNull Star[0]);

                for (Star neighbour : neighbours) {
                    if (star.getUID() < neighbour.getUID()) {
                        starlanes.add(new StarlanePrototype(star, neighbour));
                    }
                }
            }
        }

        Matrix4 oldTransformMat = batch.getTransformMatrix();
        Matrix4 oldProjMat = batch.getProjectionMatrix();

        batch.flush();
        batch.setTransformMatrix(new Matrix4());
        batch.setProjectionMatrix(Drawing.getBoardCamera().combined);

        SCSCoreLogic.drawRegionsSync(starsCulled, batch, cullArea);
        this.starRenderer.renderStars(starsCulled, batch);
        this.starlaneRenderer.renderStarlanes(starlanes, batch);

        batch.flush();
        batch.setTransformMatrix(oldTransformMat);
        batch.setProjectionMatrix(oldProjMat);
    }

    @Override
    public void dispose() {
        if (this.disposed) {
            throw new IllegalStateException("Illegal attempt of use-after-free");
        }

        if (this.starRenderer instanceof Disposable d) {
            d.dispose();
        }

        if (this.starlaneRenderer instanceof Disposable d) {
            d.dispose();
        }

        this.disposed = true;
    }
}
