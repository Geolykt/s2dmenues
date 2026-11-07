package de.geolykt.s2dmenues.render.tosvg;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record RectangleI(int x, int y, int width, int height) {
    @NonNull
    public static Optional<RectangleI> parseOpt(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        String[] splits = value.split(" ");

        int x = Integer.parseInt(splits[0]);
        int y = Integer.parseInt(splits[1]);
        int w = Integer.parseInt(splits[2]);
        int h = Integer.parseInt(splits[3]);

        return Optional.of(new RectangleI(x, y, w, h));
    }
}
