package de.geolykt.s2dmenues.mixins;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.S2DI18N.ConfiguredTranslateable;
import de.geolykt.s2dmenues.bridge.I18NCapable;

import snoddasmannen.galimulator.ProceduralScenarioSource;

@Mixin(ProceduralScenarioSource.class)
public class ProceduralScenarioSourceMixins implements I18NCapable{
    @Override
    @NotNull
    public ConfiguredTranslateable s2dmenues$getLocalisation() {
        return S2DI18N.s2d("registries.scenario.procedural.galimulator." + ((Enum<?>) (Object) this).name().toLowerCase(Locale.ROOT));
    }
}
