package de.geolykt.s2dmenues.render.tosvg;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

class TOSVGTests {
    public static void main(String[] args) {
        TOSVGRootObject.parse(TOSVGTests.readResourceStr("aur/icons/configure.svg"));
    }

    @NonNull
    private static String readResourceStr(@NotNull String resourceLocation) {
        try (InputStream is = TOSVGTests.class.getClassLoader().getResourceAsStream(resourceLocation)) {
            if (is == null) {
                throw new IOException("Resource '" + resourceLocation + "' is not located within the classpath.");
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read string from classpath", e);
        }
    }
}
