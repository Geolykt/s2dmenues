package de.geolykt.s2dmenues.render.tosvg;

import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final record Length(double value, @NonNull LengthUnit unit) {
    public enum LengthUnit {
        PERCENT,
        PIXELS
    }

    public final record Length2c(@NonNull Length x, @NonNull Length y) {}
    public final record RectangleLength(double x, double y, double w, double h, LengthUnit unit) {}

    @Contract(pure = true)
    public double toPixels(double fullLength) {
        return switch (this.unit) {
            case PERCENT -> this.value * 0.01D * fullLength;
            case PIXELS -> this.value;
        };
    }

    @NonNull
    public static Optional<Length> parseOpt(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        if (value.endsWith("px")) {
            value = value.substring(0, value.length() - 2).strip();

            return Optional.of(new Length(Double.parseDouble(value), LengthUnit.PIXELS));
        } else {
            try {
                return Optional.of(new Length(Double.parseDouble(value.strip()), LengthUnit.PIXELS));
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Unsupported unit for length: " + value, nfe);
            }
        }
    }

    @NonNull
    public static Length2c parseVec2(final @NotNull String value, final int start, final int end) {
        int idx0 = start; // Once finalized, points to first digit character

        while (Character.isWhitespace(value.charAt(idx0))) {
            idx0++; // Skip leading whitespace
        }

        int idx1 = idx0; // Once finalized, points to first non-digit character
        char ch;

        while (!Character.isWhitespace(ch = value.charAt(++idx1)) && ch != '%'); // Skip to next whitespace, breaking on '%'

        LengthUnit unitX;

        int idx2;

        if (ch == '%') {
            // '%' does permit lack of whitespace. Somehow - so special handling is needed here.
            unitX = LengthUnit.PERCENT;
            idx2 = idx1 + 1;
        } else {
            idx2 = idx1;

            while (Character.isWhitespace(value.charAt(++idx2))); // Skip whitespace for idx2
            while (!Character.isDigit(value.charAt(--idx1))); // Return idx1 to last digit character

            idx1++; // Point to first non-digit char

            unitX = switch(value.substring(idx1, idx2)) {
                case "px" -> LengthUnit.PIXELS;
                default -> throw new UnsupportedOperationException("Unknown unit: '" + value.substring(idx1, idx2) + "' within: " + value.substring(start, end));
            };
        }

        double valueX = Double.parseDouble(value.substring(idx0, idx1));

        int idx3 = end;

        while (!Character.isWhitespace(value.charAt(--idx3))); // Return idx3 to last non-whitespace character in string

        int idx4 = idx3;

        while (!Character.isDigit(value.charAt(--idx4))); // Return idx4 to the last digit character in string

        idx4++; // Point to first non-digit char instead

        System.err.println(value.substring(idx0));
        System.err.println(value.substring(idx1));
        System.err.println(value.substring(idx2));
        System.err.println(value.substring(idx3));
        System.err.println(value.substring(idx4));

        double valueY = Double.parseDouble(value.substring(idx2, idx3));

        LengthUnit unitY = switch (value.substring(idx4, idx3)) {
            case "%" -> LengthUnit.PERCENT;
            case "px" -> LengthUnit.PIXELS;
            default -> throw new UnsupportedOperationException("Unknown unit: '" + value.substring(idx4, idx3) + "' within: " + value.substring(start, end));
        };

        return new Length2c(new Length(valueX, unitX), new Length(valueY, unitY));
    }
}
