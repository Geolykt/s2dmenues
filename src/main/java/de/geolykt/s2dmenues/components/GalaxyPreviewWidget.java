package de.geolykt.s2dmenues.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.impl.gui.GLScissorState;

import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.StarGenerator;
import snoddasmannen.galimulator.StarPath;

public class GalaxyPreviewWidget extends Widget implements Disposable {

    private static class StarPositionMeta {
        private static final Vector2 TMP = new Vector2();

        private final float maxX;
        private final float maxY;
        @Nullable
        private final StarPath path;
        private float pathAngle;
        private final float pathAngleVelocity;
        private final Vector2 rawPosition;

        public StarPositionMeta(snoddasmannen.galimulator.Star star, float maxX, float maxY) {
            this.rawPosition = star.getCoordinates();
            this.path = star.getPath();
            this.pathAngle = star.pathAngle;
            this.pathAngleVelocity = star.getPathAngleVelocity();
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public Vector2 getCurrentPosition() {
            return StarPositionMeta.TMP.set(this.rawPosition).scl(0.5F / this.maxX, 0.5F / this.maxY).add(0.5F, 0.5F);
        }

        public void updatePosition() {
            StarPath path = this.path;
            if (path == null) {
                return;
            }
            path.a();
            this.pathAngle += this.pathAngleVelocity;
            this.rawPosition.set(path.b(this.pathAngle));
        }
    }

    private static final int MAX_MESH_STARS = 0x1000;
    private static final int MAX_MESH_STARS_MASK = 0x0FFF;
    private static final float BOX_SIZE_HALF = 6;

    private static final VertexAttribute ATTRIBUTE_CENTER_POSITION = new VertexAttribute(Usage.Generic, 2, GL20.GL_FLOAT, false, "a_centerpos");
    private static final VertexAttribute ATTRIBUTE_VERTEX_POSITION = new VertexAttribute(Usage.Position, 2, GL20.GL_FLOAT, false, ShaderProgram.POSITION_ATTRIBUTE);

    private boolean dirty = false;
    @Nullable
    private MapData lastMap = null;
    @NotNull
    private final GenGalaxyWindow parent;
    @NotNull
    private final List<StarPositionMeta> positions = new ArrayList<>();
    @NotNull
    private final Mesh starDrawingMesh = new Mesh(false, GalaxyPreviewWidget.MAX_MESH_STARS * 4, GalaxyPreviewWidget.MAX_MESH_STARS * 5, GalaxyPreviewWidget.ATTRIBUTE_VERTEX_POSITION, GalaxyPreviewWidget.ATTRIBUTE_CENTER_POSITION);
    @NotNull
    private final ShaderProgram starDrawingShader;
    private final float @NotNull[] vertices = new float[GalaxyPreviewWidget.MAX_MESH_STARS * 16];

    public GalaxyPreviewWidget(@NotNull GenGalaxyWindow parent) {
        this.parent = parent;
        StringBuilder vertexShader = new StringBuilder();
        StringBuilder fragmentShader = new StringBuilder();
        try (InputStream vert = GalaxyPreviewWidget.class.getClassLoader().getResourceAsStream("preview-star-renderer.vert");
                Reader vertReader = new InputStreamReader(vert, StandardCharsets.UTF_8);
                BufferedReader vertReaderBuffered = new BufferedReader(vertReader);
                InputStream frag = GalaxyPreviewWidget.class.getClassLoader().getResourceAsStream("preview-star-renderer.frag");
                Reader fragReader = new InputStreamReader(frag, StandardCharsets.UTF_8);
                BufferedReader fragReaderBuffered = new BufferedReader(fragReader)) {
            for (String ln = vertReaderBuffered.readLine(); ln != null; ln = vertReaderBuffered.readLine()) {
                vertexShader.append(ln).append('\n');
            }
            for (String ln = fragReaderBuffered.readLine(); ln != null; ln = fragReaderBuffered.readLine()) {
                fragmentShader.append(ln).append('\n');
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load shaders required for drawing stars in the galaxy preview.", e);
        }
        this.starDrawingShader = new ShaderProgram(vertexShader.toString(), fragmentShader.toString());

        short[] indices = new short[GalaxyPreviewWidget.MAX_MESH_STARS * 5];
        // 0, 1, 2, 3, <RESET>, 4, 5, 6, 7, <RESET>, 8, 9, [...]
        for (int i = GalaxyPreviewWidget.MAX_MESH_STARS; i-- != 0;) {
            int baseAddrW = i * 5;
            int baseAddrR = i * 4;
            indices[baseAddrW] = (short) (baseAddrR);
            indices[baseAddrW + 1] = (short) (baseAddrR + 1);
            indices[baseAddrW + 2] = (short) (baseAddrR + 2);
            indices[baseAddrW + 3] = (short) (baseAddrR + 3);
            indices[baseAddrW + 4] = (short) (0xFFFF);
        }
        this.starDrawingMesh.setIndices(indices);
    }

    @Override
    public void dispose() {
        this.starDrawingShader.dispose(); // Beware: this may reset the current shader. However, I am not entirely worried about this.
        this.starDrawingMesh.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        TextureRegion region = Drawing.getTextureProvider().getSinglePixelSquare();

        MapData renderData = this.parent.getMapdata();
        StarGenerator generator = renderData.getGenerator();
        if (!generator.allowPreview()) {
            Color c = Color.RED.cpy();
            c.a = parentAlpha;
            batch.setColor(c);
            batch.draw(region, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            return;
        } else {
            Color c = Color.BLUE.cpy();
            c.a = parentAlpha;
            batch.setColor(c);
            batch.draw(region, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        if (this.lastMap != renderData || this.dirty) {
            this.lastMap = renderData;
            this.dirty = false;
            // FIXME sloppy code - could cause data inconsistency issues
            int starCount = this.parent.getGalaxySize();
            float starCountOld = Space.starCount;
            Space.starCount = starCount;
            generator.prepareGenerator();
            this.positions.clear();
            while (starCount-- != 0) {
                this.positions.add(new StarPositionMeta(generator.generateStar(), generator.getMaxX(), generator.getMaxY()));
            }
            Space.starCount = starCountOld;
        }

        org.lwjgl.opengl.GL31.glPrimitiveRestartIndex(0xFFFF);
        Gdx.gl20.glEnable(org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART);

        batch.end();
        this.starDrawingShader.bind();
        Matrix4 projectedTransformationMatrix = batch.getProjectionMatrix().cpy().mul(batch.getTransformMatrix());
        this.starDrawingShader.setUniformMatrix("u_projTrans", projectedTransformationMatrix);
        this.starDrawingShader.setUniformf("u_parentAlpha", parentAlpha);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendEquation(GL20.GL_FUNC_ADD);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        GLScissorState scissor = GLScissorState.captureScissor();
        Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST);
        boolean clip = this.clipBegin(this.getX() + 2, this.getY() + 2, this.getWidth() - 4, this.getHeight() - 4);

        float[] vertices = this.vertices;
        Mesh mesh = this.starDrawingMesh;
        int totalSize, i;
        i = totalSize = this.positions.size();
        while (i-- != 0) {
            StarPositionMeta posMeta = this.positions.get(i);
            int baseAddress = (i & GalaxyPreviewWidget.MAX_MESH_STARS_MASK) * 16;
            for (int j = 0; j < 8; j++) {
                posMeta.updatePosition();
            }
            Vector2 pos = posMeta.getCurrentPosition();
            float centerX = this.getX() + (pos.x * this.getWidth());
            float centerY = this.getY() + (pos.y * this.getHeight());

            vertices[baseAddress] = centerX - GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 1] = centerY - GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 2] = centerX;
            vertices[baseAddress + 3] = centerY;

            vertices[baseAddress + 4] = centerX + GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 5] = centerY - GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 6] = centerX;
            vertices[baseAddress + 7] = centerY;

            vertices[baseAddress + 8] = centerX - GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 9] = centerY + GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 10] = centerX;
            vertices[baseAddress + 11] = centerY;

            vertices[baseAddress + 12] = centerX + GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 13] = centerY + GalaxyPreviewWidget.BOX_SIZE_HALF;
            vertices[baseAddress + 14] = centerX;
            vertices[baseAddress + 15] = centerY;

            if ((i & GalaxyPreviewWidget.MAX_MESH_STARS_MASK) == 0) {
                int batchStarCount = Math.min(totalSize - i, GalaxyPreviewWidget.MAX_MESH_STARS);
                mesh.setVertices(vertices, 0, batchStarCount * 16);
                mesh.render(this.starDrawingShader, GL20.GL_TRIANGLE_STRIP, 0, batchStarCount * 5, true);
            }
        }

        if (clip) {
            this.clipEnd();
        }
        scissor.reapplyState();

        Gdx.gl20.glDisable(org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART);
        Gdx.gl20.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    @Override
    public float getMinHeight() {
        return 30F;
    }

    @Override
    public float getMinWidth() {
        return 30F;
    }

    @Override
    public float getPrefHeight() {
        return 100F;
    }

    @Override
    public float getPrefWidth() {
        return 100F;
    }

    public void reset() {
        this.dirty = true;
    }
}
