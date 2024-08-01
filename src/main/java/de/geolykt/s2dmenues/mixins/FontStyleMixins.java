package de.geolykt.s2dmenues.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.maltaisn.msdfgdx.FontStyle;

import de.geolykt.s2dmenues.bridge.FontStyleMarkerInterface;

@Mixin(FontStyle.class)
public class FontStyleMixins implements FontStyleMarkerInterface {
    @Unique
    private boolean useInnerShadow;

    @Unique
    private boolean useShadow;

    @Override
    public boolean s2dmenues$useInnerShadow() {
        return this.useInnerShadow;
    }

    @Override
    public void s2dmenues$useInnerShadow(boolean useInnerShadow) {
        this.useInnerShadow = useInnerShadow;
    }

    @Override
    public boolean s2dmenues$useShadow() {
        return this.useShadow;
    }

    @Override
    public void s2dmenues$useShadow(boolean useShadow) {
        this.useShadow = useShadow;
    }
}
