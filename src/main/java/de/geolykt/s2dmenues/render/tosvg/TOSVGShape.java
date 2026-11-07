package de.geolykt.s2dmenues.render.tosvg;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.math.Rectangle;

import de.geolykt.s2dmenues.render.tosvg.path.PathCommand;

import xmlparser.model.XmlElement;

public abstract sealed class TOSVGShape implements TOSVGElement permits TOSVGPath, TOSVGEllipse {

    public static enum TransformBox {
        FILL_BOX;

        @NonNull
        public static TransformBox parseEnum(@NonNull String rhsExpression) {
            return switch(rhsExpression.strip()) {
                case "fill-box" -> TransformBox.FILL_BOX;
                default -> throw new UnsupportedOperationException("Unsupported transform-box value: " + rhsExpression);
            };
        }
    }

    @NonNull
    private static Vector4fc getDefaultFillColor() {
        return new Vector4f(1, 1, 1, 1); // white
    }

    @NonNull
    private static Vector4fc getDefaultStrokeColor() {
        return new Vector4f(); // black
    }

    @NonNull
    public static TOSVGShape parse(@NonNull XmlElement element) {
        TOSVGShape shape = switch (element.name) {
            case "path": {
                String commands = element.attributes.get("d");

                if (commands == null) {
                    throw new IllegalArgumentException("Path element '" + element.name + "' has no 'd' attribute.");
                }

                yield new TOSVGPath(PathCommand.parseCommands(commands), commands);
            }
            case "ellipse": {
                yield new TOSVGEllipse();
            }
            default: {
                throw new UnsupportedOperationException("Unexpected value for element name: " + element.name);
            }
        };

        shape.transformMatrix = TOSVGShape.parseTransform(element.attributes.get("transform"));

        String style = element.attributes.get("style");

        parseCSS:
        if (style != null) {
            style = style.strip();
            int insnStart = 0;
            int insnEnd = style.indexOf(';');

            while (true) {
                int keyEnd = style.indexOf(':', insnStart);

                boolean abrt;

                if ((abrt = insnEnd < 0)) {
                    insnEnd = style.length();
                }

                if (keyEnd < 0) {
                    throw new IllegalArgumentException("Cannot find left-hand-side expression in style attribute for element '" + element.name + "' within expression '" + style.substring(insnStart, insnEnd) + "'.");
                }

                String propertyName = style.substring(insnStart, keyEnd).strip();

                switch (propertyName) {
                case "fill":
                    shape.fillColor = Optional.of(TOSVGShape.parseColor(style.substring(keyEnd + 1, insnEnd)));
                    break;
                case "stroke":
                    shape.strokeColor = Optional.of(TOSVGShape.parseColor(style.substring(keyEnd + 1, insnEnd)));
                    break;
                case "transform-box":
                    shape.transformBox = Optional.of(TransformBox.parseEnum(style.substring(keyEnd + 1, insnEnd)));
                    break;
                case "transform-origin":
                    shape.transformOrigin = Optional.of(TransformOriginValue.parse(style, keyEnd + 1, insnEnd));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown/Unimplemented CSS property name: " + propertyName  + " within element '" + element.name + "'");
                }

                if (abrt || ++insnEnd == style.length()) {
                    // Final expression
                    break parseCSS;
                }

                insnEnd = style.indexOf(';', (insnStart = insnEnd));
            }
        }

        return shape;
    }

    /**
     * Parse a CSS color property value (right-hand-side expression).
     *
     * <p>Output format is RGBA, normalized to a range between 0.0 (inclusive) and 1.0 (inclusive).
     *
     * @param rhsExpression The right-hand-side expression to parse and resolve. Does not contain the trailing ';' but can contain arbitrary trailing and leading whitespace.
     * @return A {@link Vector4f} describing the color in RGBA notation.
     * @throws UnsupportedOperationException If the color name or function is not known or implemented.
     * @throws IllegalArgumentException If the given CSS property value is malformed and cannot be parsed, or uses syntax not implemented in this library.
     */
    @NonNull
    static Vector4f parseColor(@NonNull String rhsExpression) {
        rhsExpression = rhsExpression.strip();

        if (rhsExpression.charAt(0) == '#') {
            long hex = Long.valueOf(rhsExpression.substring(1), 16);

            if (hex > 0xFF_FF_FFL) {
                throw new IllegalStateException("Unexpected right-hand-side expression: " + rhsExpression);
            }

            return new Vector4f(((hex & 0xFF_00_00L) >> 16) / 255.0F, ((hex & 0x00_FF_00L) >> 8) / 255.0F, ((hex & 0x00_00_FFL) >> 0) / 255.0F, 1.0F);
        }

        int functionNameEnd = rhsExpression.indexOf('(');

        if (functionNameEnd < 0) {
            throw new UnsupportedOperationException("Unknown/Unimplemented color: " + rhsExpression);
        } else if (rhsExpression.charAt(rhsExpression.length() - 1) != ')') {
            throw new IllegalArgumentException("Cannot parse color: Invalid RHS expression: " + rhsExpression);
        } else {
            String functionName = rhsExpression.substring(0, functionNameEnd);

            return switch (functionName) {
                case "rgb": {
                    String[] argv = rhsExpression.substring(functionNameEnd + 1, rhsExpression.length() - 1).split("(,*\s+)|(\s*,\s*)");

                    if (argv.length != 3) {
                        throw new IllegalArgumentException("Invalid color function: The rgb() function takes in 3 number values.");
                    }

                    float r = Float.parseFloat(argv[0]);
                    float g = Float.parseFloat(argv[1]);
                    float b = Float.parseFloat(argv[2]);

                    yield new Vector4f(r / 255.0F, g / 255.0F, b / 255.0F, 1.0F);
                }
                default: {
                    throw new UnsupportedOperationException("Unknown/Unimplemented color function: " + functionName);
                }
            };
        }
    }

    /**
     * Parse a transform function call as per <a href="https://drafts.csswg.org/css-transforms/#transform-functions">the CSS-Transforms specification</a>.
     * The transform can be described as a 4 by 4 matrix and is implemented as advised in the <a href="https://drafts.csswg.org/css-transforms/#MatrixDefined">CSS-Transforms specification</a>.
     *
     * @param value The raw transform notation, may be null
     * @return A non-empty {@link Optional} containing the parsed transformation {@link Matrix4dc matrix} described by the value function if value is not null, an empty {@link Optional} otherwise.
     * @throws IllegalArgumentException If the transform cannot be parsed.
     * @throws UnsupportedOperationException If the transform function has not yet been implemented
     * but is known to TOSVG.
     */
    @NonNull
    static Optional<Matrix4dc> parseTransform(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        value = value.strip();
        int functionNameLength = value.indexOf('(');

        if (functionNameLength == -1) {
            throw new IllegalArgumentException("Transform without a transform function: " + value);
        } else if (value.charAt(value.length() - 1) != ')') {
            throw new IllegalArgumentException("Transform not a transform function (does not end with ')')");
        }

        String functionName = value.substring(0, functionNameLength);

        return Optional.of(switch (functionName) {
            case "matrix": {
                String[] argv = value.substring(functionNameLength + 1, value.length() - 1).split("(,*\s+)|(\s*,\s*)");

                if (argv.length != 6) {
                    throw new IllegalArgumentException("Invalid transform function: The matrix() function takes in 6 number values. Parsed arguments " + Arrays.toString(argv) + " from string: " + value.substring(functionNameLength + 1, value.length() - 1));
                }

                double a = Double.parseDouble(argv[0]);
                double b = Double.parseDouble(argv[1]);
                double c = Double.parseDouble(argv[2]);
                double d = Double.parseDouble(argv[3]);
                double e = Double.parseDouble(argv[4]);
                double f = Double.parseDouble(argv[5]);

                yield new Matrix4d(
                    // Beware: Matrix4d(double, …) uses a column-based format - i.e. mentally pretend that the matrix is transposed
                    a, b, 0, 0, // First column
                    c, d, 0, 0, // Second column
                    0, 0, 1, 0, // Third column
                    e, f, 0, 1  // Fourth column
                );
            }
            case "translate":
            case "translateX":
            case "translateY":
            case "scale":
            case "scaleX":
            case "scaleY":
            case "rotate":
            case "skew":
            case "skewX":
            case "skewY": {
                throw new UnsupportedOperationException("Transform function '" + functionName + "' has not yet been implemented (even if it is trivial to implement).");
            }
            default: {
                throw new IllegalArgumentException("Unknown function name: " + functionName);
            }
        });
    }

    @NonNull
    private Optional<Vector4fc> fillColor;
    @NonNull
    private Optional<Vector4fc> strokeColor;
    @NonNull
    protected Optional<TransformBox> transformBox;
    @NonNull
    protected Optional<Matrix4dc> transformMatrix;
    @NonNull
    protected Optional<TransformOriginValue> transformOrigin;

    public TOSVGShape() {
        this.transformMatrix = Optional.empty();
        this.fillColor = Optional.empty();
        this.strokeColor = Optional.empty();
        this.transformBox = Optional.empty();
        this.transformOrigin = Optional.empty();
    }

    @NonNull
    private Rectangle getBoxDimensions(@NonNull RectangleI viewport) {
        if (this.transformBox.isEmpty()) {
            throw new UnsupportedOperationException("transform-box not yet correctly implemented");
        }

        return switch (this.transformBox.get()) {
            case FILL_BOX -> this.getExtents();
        };
    }

    @NonNull
    protected abstract Rectangle getExtents();

    @NonNull
    public Vector4fc getFillColor() {
        return this.fillColor.isPresent() ? this.fillColor.get() : TOSVGShape.getDefaultFillColor();
    }

    @NonNull
    protected Matrix4dc getProjectionMatrix(@NonNull RectangleI viewport) {
        Matrix4dc matrix = this.transformMatrix.isPresent() ? this.transformMatrix.get() : new Matrix4d();

        if (this.transformOrigin.isEmpty()) {
            return matrix;
        }

        if (this.transformBox.isEmpty()) {
            throw new UnsupportedOperationException("Code path not yet implemented");
        }

        Vector3dc originTranslation = this.transformOrigin.get().getOriginTransformation(this.getBoxDimensions(viewport));

        return new Matrix4d().translate(originTranslation).mul(matrix).translate(originTranslation.negate(new Vector3d()));
    }

    @NonNull
    public Vector4fc getStrokeColor() {
        return this.strokeColor.isPresent() ? this.strokeColor.get() : TOSVGShape.getDefaultStrokeColor();
    }
}
