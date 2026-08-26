package de.geolykt.s2dmenues.render.itf;

import java.util.SequencedCollection;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.Batch;

import de.geolykt.starloader.api.empire.Star;

public interface IStarlaneRenderer {
    public static record StarlanePrototype(@NotNull Star starA, @NotNull Star starB) implements Comparable<@NotNull StarlanePrototype> {
        @Override
        public int compareTo(@NotNull StarlanePrototype o) {
            int ret = Integer.compareUnsigned(this.starA.getUID(), o.starA.getUID());

            if (ret != 0) {
                return ret;
            } else {
                return Integer.compareUnsigned(this.starB.getUID(), o.starB.getUID());
            }
        }
    }

    void renderStarlanes(@NotNull SequencedCollection<@NotNull StarlanePrototype> starlanes, @NotNull Batch surface);
}
