package de.geolykt.s2dmenues.render.aur;

import java.util.SequencedCollection;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import de.geolykt.s2dmenues.render.AsyncUniverseRenderer;
import de.geolykt.s2dmenues.render.itf.IStarlaneRenderer;
import de.geolykt.starloader.api.empire.Star;

public class AURStarlaneRenderer implements IStarlaneRenderer {
    @Override
    public void renderStarlanes(@NotNull SequencedCollection<@NotNull StarlanePrototype> starlanes,
            @NotNull Batch surface) {
        ShapeRenderer renderer = new ShapeRenderer();

        renderer.setProjectionMatrix(surface.getProjectionMatrix());
        renderer.setTransformMatrix(surface.getTransformMatrix());

        surface.getShader().bind();
        renderer.begin(ShapeType.Filled);
        renderer.setColor(Color.LIGHT_GRAY);

        for (StarlanePrototype proto : starlanes) {
            Star starA = proto.starA();
            Star starB = proto.starB();

            renderer.rectLine(starA.getCoordinates(), starB.getCoordinates(), AsyncUniverseRenderer.GRANULARITY_FACTOR / 5F);
        }

        renderer.end();
        renderer.dispose();
    }
}
