package de.geolykt.s2dmenues.components;

import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class NOPActor extends Widget {

    private final int width;
    private final int height;

    public NOPActor(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public float getPrefHeight() {
        return this.height;
    }

    @Override
    public float getPrefWidth() {
        return this.width;
    }
}
