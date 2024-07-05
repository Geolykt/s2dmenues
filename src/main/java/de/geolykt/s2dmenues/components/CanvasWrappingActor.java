package de.geolykt.s2dmenues.components;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;

import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasContext;
import de.geolykt.starloader.api.gui.canvas.CanvasSettings;
import de.geolykt.starloader.api.gui.canvas.MultiCanvas;
import de.geolykt.starloader.impl.gui.GLScissorState;

@Deprecated
public class CanvasWrappingActor extends Widget {

    @NotNull
    private final CanvasSettings settings;
    @NotNull
    private final CanvasContext ctx;
    @SuppressWarnings("all")
    @NotNull
    private final Canvas canvas;

    public CanvasWrappingActor(@NotNull Canvas canvas) {
        if (canvas instanceof MultiCanvas) {
            throw new IllegalArgumentException("Multi-canvases are not supported yet.");
        }
        this.canvas = canvas;
        this.settings = canvas.getCanvasSettings();
        this.ctx = canvas.getContext();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeightI();
    }

    @Override
    public float getPrefWidth() {
        return this.ctx.getWidth();
    }

    private int getHeightI() {
        if (this.settings.hasHeader()) {
            return this.ctx.getHeight() + 32;
        } else {
            return this.ctx.getHeight();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.validate();
        Color tint = new Color(this.getColor());
        tint.a *= parentAlpha;

        if (tint.a == 0) {
            return;
        }

        batch.setColor(tint);

        // TODO batch.flush() here?
        GLScissorState oldScissor = GLScissorState.captureScissor();
        if (oldScissor.enabled) {
            GLScissorState.glScissor(oldScissor.x + (int) this.getX(), oldScissor.y + (int) this.getY(), this.ctx.getWidth(), this.getHeightI());
        } else {
            Gdx.gl.glEnable(GL11.GL_SCISSOR_TEST);
            GLScissorState.glScissor(0, 0, this.ctx.getWidth(), this.getHeightI());
        }

        if (this.settings.getBackgroundColor().a != 0) {
            this.drawBackground(batch);
        }

        if (this.settings.hasHeader()) {
            this.drawHeader(batch);
        }

        try {
//            renderChildren(batch);
//            Camera origin = this.getStage().getCamera();
            OrthographicCamera internalCamera = new OrthographicCamera(this.getPrefWidth(), this.getPrefHeight());
            Matrix4 originProjectionMatrix = new Matrix4(batch.getProjectionMatrix());
            batch.setProjectionMatrix(internalCamera.combined);
            ctx.render((SpriteBatch) batch, internalCamera);
            batch.setProjectionMatrix(originProjectionMatrix);
        } finally {
            // TODO batch.flush(); ???
            oldScissor.reapplyState();
            if (!oldScissor.enabled) {
                GLScissorState.forgetScissor();
            }
        }
    }

    private void drawBackground(Batch batch) {
        NinePatch ninepatch = Drawing.getTextureProvider().getAlternateWindowNinepatch();
        ninepatch.setColor(this.settings.getBackgroundColor());
        ninepatch.draw(batch, this.getX(), this.getY(), this.getPrefWidth(), this.getPrefHeight());
    }

    private void drawHeader(Batch batch) {
        NinePatch ninepatch = Drawing.getTextureProvider().getAlternateWindowNinepatch();
        ninepatch.setColor(this.settings.getHeaderColor());
        ninepatch.draw(batch, this.getX(), this.getY() + this.ctx.getHeight(), this.getPrefWidth(), 32F);
        BitmapFont font = Drawing.getSpaceFont();
        font.setColor(Color.WHITE);
        font.draw(batch, this.settings.getHeaderText(), this.getX(), this.getY() + this.ctx.getHeight(), this.getWidth(), Align.left, false);
    }
}
