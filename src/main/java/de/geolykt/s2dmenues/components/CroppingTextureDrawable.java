package de.geolykt.s2dmenues.components;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Disposable;

public class CroppingTextureDrawable extends BaseDrawable implements Disposable {

    @NotNull
    private final Texture texture;
    private final float textureWidth;
    private final float textureHeight;
    private final boolean ownsTexture;

    public CroppingTextureDrawable(@NotNull Texture texture, boolean ownsTexture) {
        this.texture = texture;
        this.ownsTexture = ownsTexture;
        this.textureWidth = texture.getWidth();
        this.textureHeight = texture.getHeight();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        final float proportionU = Math.min(1F, width / this.textureWidth);
        final float proportionV = Math.min(1F, height / this.textureHeight);

        final float u2;
        final float v2;

        if (proportionU > proportionV) {
            v2 = proportionV / proportionU;
            u2 = 1F;
        } else {
            u2 = proportionU / proportionV;
            v2 = 1F;
        }

        batch.draw(this.texture, x, y, width, height, 0F, 1F, u2, 1F - v2);
    }

    @Override
    public void dispose() {
        if (this.ownsTexture) {
            this.texture.dispose();
        }
    }
}
