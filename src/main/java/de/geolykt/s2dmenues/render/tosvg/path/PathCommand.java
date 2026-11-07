package de.geolykt.s2dmenues.render.tosvg.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

import de.geolykt.s2dmenues.render.tosvg.TOSubstringScanner;
import de.geolykt.s2dmenues.render.tosvg.path.EllipticalArcPathCommand.ArcSize;
import de.geolykt.s2dmenues.render.tosvg.path.EllipticalArcPathCommand.ArcSweep;

public abstract sealed class PathCommand permits MovePathCommand, LinePathCommand, VLinePathCommand, HLinePathCommand, CubicBezierPathCommand, SmoothCubicBezierPathCommand, SmoothQuadraticPathCommand, QuadraticBezierPathCommand, EllipticalArcPathCommand, ClosePathCommand {

    @NonNull
    @Unmodifiable
    @Contract(pure = true)
    public static List<@NonNull PathCommand> parseCommands(@NonNull String commandString) {
        List<@NonNull PathCommand> commands = new ArrayList<>();

        TOSubstringScanner scanner = new TOSubstringScanner(commandString, 0, commandString.length());
        scanner.skipWhitespace();

        while (scanner.hasRemaining()) {
            char command = scanner.readCharacter();
            scanner.skipWhitespace();
            boolean absolute = false;

            switch (command) {
                case 'M':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'm': {
                    commands.add(new MovePathCommand(absolute, scanner.readVector2()));

                    while (scanner.hasRemaining() && scanner.isNumericChar()) {
                        commands.add(new LinePathCommand(absolute, scanner.readVector2()));
                    }

                    break;
                }
                case 'L':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'l':
                    do {
                        commands.add(new LinePathCommand(absolute, scanner.readVector2()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                case 'V':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'v':
                    do {
                        commands.add(new VLinePathCommand(absolute, scanner.readNumber()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                case 'H':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'h':
                    do {
                        commands.add(new HLinePathCommand(absolute, scanner.readNumber()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                case 'C':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'c': {
                    do {
                        commands.add(new CubicBezierPathCommand(absolute, scanner.readVector2Multi(), scanner.readVector2Multi(), scanner.readVector2()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                }
                case 'S':
                    absolute = true;
                    // $FALL-THROUGH$
                case 's': {
                    do {
                        commands.add(new SmoothCubicBezierPathCommand(absolute, scanner.readVector2Multi(), scanner.readVector2()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                }
                case 'Q':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'q': {
                    do {
                        commands.add(new QuadraticBezierPathCommand(absolute, scanner.readVector2Multi(), scanner.readVector2()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                }
                case 'T':
                    absolute = true;
                    // $FALL-THROUGH$
                case 't': {
                    do {
                        commands.add(new SmoothQuadraticPathCommand(absolute, scanner.readVector2()));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                }
                case 'A':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'a': {
                    do {
                        Vector2dc radius = scanner.readVector2Multi();
                        double angleRad = scanner.readNumberMulti() * (Math.PI / 180.0D);
                        ArcSize size = scanner.readArcSizeMulti();
                        ArcSweep sweep = scanner.readArcSweepMulti();
                        Vector2dc endPoint = scanner.readVector2();

                        commands.add(new EllipticalArcPathCommand(absolute, radius, angleRad, size, sweep, endPoint));
                    } while (scanner.hasRemaining() && scanner.isNumericChar());

                    break;
                }
                case 'Z':
                    absolute = true;
                    // $FALL-THROUGH$
                case 'z':
                    commands.add(new ClosePathCommand(absolute));
                    break;
                default: {
                    throw new UnsupportedOperationException("Unsupported command: '" + command + "'");
                }
            }
        }

        return Collections.unmodifiableList(commands);
    }

    private final boolean absolute;

    public PathCommand(boolean absolute) {
        this.absolute = absolute;
    }

    /**
     * Get whether the command uses absolute coordinates (i.e. the command is uppercase).
     *
     * <p>If false, the command uses relative coordinates (i.e. the command is lowercase).
     *
     * @return Whether the command uses absolute coordinates.
     */
    @Contract(pure = true)
    public boolean isAbsolute() {
        return this.absolute;
    }
}
