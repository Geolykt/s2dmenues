package de.geolykt.s2dmenues.render.aur;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.S2DMenues;
import de.geolykt.s2dmenues.render.AsyncUniverseRenderer;
import de.geolykt.s2dmenues.render.itf.IStarRenderer;
import de.geolykt.starloader.api.empire.Star;
import de.geolykt.starloader.api.gui.Drawing;

public class AURStarRenderer implements IStarRenderer, Disposable {
    @Nullable
    private ShaderProgram shader;

    @Override
    public void renderStars(@NotNull List<@NotNull Star> stars, @NotNull Batch surface) {
        ShaderProgram shader = this.shader;

        if (shader == null) {
            this.shader = shader = new ShaderProgram(S2DMenues.readStringFromResources("aur/star.vert"), S2DMenues.readStringFromResources("aur/star.frag"));
        }

        surface.flush();

        shader.bind();
        shader.setUniformMatrix(AURStandardShaders.UNIFORM_PROJECTION_TRANSFORMATION_MATRIX, Drawing.getBoardCamera().combined);

        AURStandardShaders.renderRadialColor(stars, AsyncUniverseRenderer.GRANULARITY_FACTOR, shader, star -> {
            return star.getEmpire().getGDXColor();
        });

        surface.flush();
    }

    @Override
    public void dispose() {
        ShaderProgram shader = this.shader;

        if (shader != null) {
            this.shader = null;
            shader.dispose();
        }
    }
}
