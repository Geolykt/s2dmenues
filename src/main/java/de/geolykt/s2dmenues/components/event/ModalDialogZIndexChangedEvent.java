package de.geolykt.s2dmenues.components.event;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;

import de.geolykt.s2dmenues.components.msdf.S2DDialog;

public class ModalDialogZIndexChangedEvent extends Event {
    private int zIndex;

    @Override
    public void setTarget(Actor targetActor) {
        super.setTarget((S2DDialog) targetActor);
    }

    @Override
    public S2DDialog getTarget() {
        return (S2DDialog) super.getTarget();
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = "_ -> this")
    public ModalDialogZIndexChangedEvent setZIndex(int zIndex) {
        this.zIndex = zIndex;

        return this;
    }

    public int getZIndex() {
        return this.zIndex;
    }

    @Override
    public void reset() {
        super.reset();
        this.zIndex = 0;
    }
}
