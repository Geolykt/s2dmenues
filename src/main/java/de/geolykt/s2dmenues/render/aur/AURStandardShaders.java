package de.geolykt.s2dmenues.render.aur;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import de.geolykt.starloader.api.Locateable;

public class AURStandardShaders {
    public static final VertexAttribute ATTRIBUTE_CENTER_POSITION = new VertexAttribute(Usage.Generic, 2, GL20.GL_FLOAT, false, "a_centerpos");
    public static final VertexAttribute ATTRIBUTE_VERTEX_POSITION = new VertexAttribute(Usage.Position, 2, GL20.GL_FLOAT, false, ShaderProgram.POSITION_ATTRIBUTE);
    public static final VertexAttribute ATTRIBUTE_VERTEX_COLOR = VertexAttribute.ColorPacked();

    public static final int RADIAL_MAX_INDICES = 0x1000;
    public static final int RADIAL_MAX_INDICES_MASK = 0x0FFF;
    public static final int PRIMITIVE_RESET_INDEX = 0xFFFF;

    @Nullable
    private static Mesh radialMesh;

    @Nullable
    private static Mesh radialColorMesh;

    @NotNull
    public static final String UNIFORM_PROJECTION_TRANSFORMATION_MATRIX = "u_projTrans";

    @NotNull
    public static Mesh getRadialMesh() {
        Mesh mesh = AURStandardShaders.radialMesh;

        if (mesh != null) {
            return mesh;
        }

        AURStandardShaders.radialMesh = mesh = new Mesh(false, AURStandardShaders.RADIAL_MAX_INDICES * 4, AURStandardShaders.RADIAL_MAX_INDICES * 5, AURStandardShaders.ATTRIBUTE_VERTEX_POSITION, AURStandardShaders.ATTRIBUTE_CENTER_POSITION);

        short[] indices = new short[AURStandardShaders.RADIAL_MAX_INDICES * 5];

        // 0, 1, 2, 3, <RESET>, 4, 5, 6, 7, <RESET>, 8, 9, [...]
        for (int i = AURStandardShaders.RADIAL_MAX_INDICES; i-- != 0;) {
            int baseAddrW = i * 5;
            int baseAddrR = i * 4;
            indices[baseAddrW] = (short) (baseAddrR);
            indices[baseAddrW + 1] = (short) (baseAddrR + 1);
            indices[baseAddrW + 2] = (short) (baseAddrR + 2);
            indices[baseAddrW + 3] = (short) (baseAddrR + 3);
            indices[baseAddrW + 4] = (short) AURStandardShaders.PRIMITIVE_RESET_INDEX;
        }

        mesh.setIndices(indices);

        return mesh;
    }

    @NotNull
    public static Mesh getRadialColorMesh() {
        Mesh mesh = AURStandardShaders.radialColorMesh;

        if (mesh != null) {
            return mesh;
        }

        AURStandardShaders.radialMesh = mesh = new Mesh(false, AURStandardShaders.RADIAL_MAX_INDICES * 4, AURStandardShaders.RADIAL_MAX_INDICES * 5, AURStandardShaders.ATTRIBUTE_VERTEX_POSITION, AURStandardShaders.ATTRIBUTE_CENTER_POSITION, AURStandardShaders.ATTRIBUTE_VERTEX_COLOR);

        short[] indices = new short[AURStandardShaders.RADIAL_MAX_INDICES * 5];

        // 0, 1, 2, 3, <RESET>, 4, 5, 6, 7, <RESET>, 8, 9, [...]
        for (int i = AURStandardShaders.RADIAL_MAX_INDICES; i-- != 0;) {
            int baseAddrW = i * 5;
            int baseAddrR = i * 4;
            indices[baseAddrW] = (short) (baseAddrR);
            indices[baseAddrW + 1] = (short) (baseAddrR + 1);
            indices[baseAddrW + 2] = (short) (baseAddrR + 2);
            indices[baseAddrW + 3] = (short) (baseAddrR + 3);
            indices[baseAddrW + 4] = (short) AURStandardShaders.PRIMITIVE_RESET_INDEX;
        }

        mesh.setIndices(indices);

        return mesh;
    }

    public static void dispose() {
        Mesh mesh = AURStandardShaders.radialMesh;

        if (mesh != null) {
            AURStandardShaders.radialMesh = null;
            mesh.dispose();
        }
    }

    public static <T extends Locateable> void renderRadialColor(@NotNull List<@NotNull T> container, float radius, @NotNull ShaderProgram shader, @NotNull Function<@NotNull T, @NotNull Color> colorFunction) {
        final int maxElements = Math.min(container.size(), AURStandardShaders.RADIAL_MAX_INDICES);
        float[] vertices = new float[maxElements * 20];

        org.lwjgl.opengl.GL31.glPrimitiveRestartIndex(AURStandardShaders.PRIMITIVE_RESET_INDEX);
        Gdx.gl20.glEnable(org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART);

        Mesh radialMesh = AURStandardShaders.getRadialColorMesh();

        int i;
        int containerSize = i = container.size();

        while (i-- != 0) {
            int baseAddress = (i & AURStandardShaders.RADIAL_MAX_INDICES_MASK) * 20;
            T obj = container.get(i);
            float x = obj.getX();
            float y = obj.getY();
            float color = colorFunction.apply(obj).toFloatBits();

            vertices[baseAddress] = x - radius;
            vertices[baseAddress + 1] = y - radius;
            vertices[baseAddress + 2] = x;
            vertices[baseAddress + 3] = y;
            vertices[baseAddress + 4] = color;

            vertices[baseAddress + 5] = x + radius;
            vertices[baseAddress + 6] = y - radius;
            vertices[baseAddress + 7] = x;
            vertices[baseAddress + 8] = y;
            vertices[baseAddress + 9] = color;

            vertices[baseAddress + 10] = x - radius;
            vertices[baseAddress + 11] = y + radius;
            vertices[baseAddress + 12] = x;
            vertices[baseAddress + 13] = y;
            vertices[baseAddress + 14] = color;

            vertices[baseAddress + 15] = x + radius;
            vertices[baseAddress + 16] = y + radius;
            vertices[baseAddress + 17] = x;
            vertices[baseAddress + 18] = y;
            vertices[baseAddress + 19] = color;

            if ((i & AURStandardShaders.RADIAL_MAX_INDICES_MASK) == 0) {
                radialMesh.setVertices(vertices, 0, Math.min(containerSize - i, AURStandardShaders.RADIAL_MAX_INDICES) * 20);
                radialMesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, Math.min(containerSize - i, AURStandardShaders.RADIAL_MAX_INDICES) * 5, true);
            }
        }
    }

    public static void renderRadial(@NotNull List<@NotNull ? extends Locateable> container, float radius, @NotNull ShaderProgram shader) {
        final int maxElements = Math.min(container.size(), AURStandardShaders.RADIAL_MAX_INDICES);
        float[] vertices = new float[maxElements * 16];

        org.lwjgl.opengl.GL31.glPrimitiveRestartIndex(AURStandardShaders.PRIMITIVE_RESET_INDEX);
        Gdx.gl20.glEnable(org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART);

        Mesh radialMesh = AURStandardShaders.getRadialMesh();

        int i;
        int containerSize = i = container.size();

        while (i-- != 0) {
            int baseAddress = (i & AURStandardShaders.RADIAL_MAX_INDICES_MASK) * 16;
            Locateable obj = container.get(i);
            float x = obj.getX();
            float y = obj.getY();

            vertices[baseAddress] = x - radius;
            vertices[baseAddress + 1] = y - radius;
            vertices[baseAddress + 2] = x;
            vertices[baseAddress + 3] = y;

            vertices[baseAddress + 4] = x + radius;
            vertices[baseAddress + 5] = y - radius;
            vertices[baseAddress + 6] = x;
            vertices[baseAddress + 7] = y;

            vertices[baseAddress + 8] = x - radius;
            vertices[baseAddress + 9] = y + radius;
            vertices[baseAddress + 10] = x;
            vertices[baseAddress + 11] = y;

            vertices[baseAddress + 12] = x + radius;
            vertices[baseAddress + 13] = y + radius;
            vertices[baseAddress + 14] = x;
            vertices[baseAddress + 15] = y;

            if ((i & AURStandardShaders.RADIAL_MAX_INDICES_MASK) == 0) {
                radialMesh.setVertices(vertices, 0, Math.min(containerSize - i, AURStandardShaders.RADIAL_MAX_INDICES) * 16);
                radialMesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, Math.min(containerSize - i, AURStandardShaders.RADIAL_MAX_INDICES) * 5, true);
            }
        }
    }
}
