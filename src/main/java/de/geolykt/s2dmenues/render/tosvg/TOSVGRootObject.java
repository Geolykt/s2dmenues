package de.geolykt.s2dmenues.render.tosvg;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Unmodifiable;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.jspecify.annotations.NonNull;

import de.geolykt.s2dmenues.render.plutovg.PlutoVGCanvas;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGSurface;

import xmlparser.XmlParser;
import xmlparser.model.XmlElement;
import xmlparser.model.XmlElement.XmlTextElement;

public final class TOSVGRootObject extends AbstractSVGElementGroup {

    @NonNull
    @Unmodifiable
    private static final Set<String> IGNORE_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "sodipodi:namedview",
        "inkscape:perspective",
        "bx:grid"
    )));

    @NonNull
    public static TOSVGRootObject parse(@NonNull String data) {
        return TOSVGRootObject.parse(Objects.requireNonNull(XmlParser.newXmlParser().conserveWhitespace().build().fromXml(data)));
    }

    @NonNull
    public static TOSVGRootObject parse(@NonNull XmlElement svgXml) {
        if (!svgXml.name.equals("svg")) {
            throw new IllegalArgumentException("Element name is '" + svgXml.name + "', expected 'svg'");
        }

        TOSVGRootObject svg = new TOSVGRootObject();

        svg.width = Length.parseOpt(XMLUtil.optAttributeString(svgXml, "width"));
        svg.height = Length.parseOpt(XMLUtil.optAttributeString(svgXml, "height"));
        svg.viewBox = RectangleI.parseOpt(XMLUtil.optAttributeString(svgXml, "viewBox"));

        for (XmlElement element : svgXml.children) {
            if (element instanceof XmlTextElement || TOSVGRootObject.IGNORE_NAMES.contains(element.name)) {
                continue;
            } else if (element.name.equals("defs")) {
                for (XmlElement definition : element.children) {
                    if (definition instanceof XmlTextElement || TOSVGRootObject.IGNORE_NAMES.contains(definition.name)) {
                        continue;
                    }

                    throw new UnsupportedOperationException("Unsupported element in <defs> statement: " + definition.name);
                }
            } else if (element.name.equals("g")) {
                SVGGroup group = new SVGGroup();

                for (XmlElement groupElement : element.children) {
                    if (groupElement instanceof XmlTextElement || TOSVGRootObject.IGNORE_NAMES.contains(groupElement.name)) {
                        continue;
                    }

                    group.addChild(TOSVGShape.parse(groupElement));
                }

                svg.addChild(group);
            } else {
                svg.addChild(TOSVGShape.parse(element));
            }
        }

        return svg;
    }

    @NonNull
    private Optional<Length> height;
    @NonNull
    private Optional<RectangleI> viewBox;
    @NonNull
    private Optional<Length> width;
    @NonNull
    private final List<@NonNull TOSVGShape> defines;

    protected TOSVGRootObject() {
        this.width = Optional.empty();
        this.height = Optional.empty();
        this.viewBox = Optional.empty();
        this.defines = new ArrayList<>();
    }

    public final void renderTo(@NonNull ByteBuffer segment, long offset, int destinationWidth, int destinationHeight, long stride) {
        this.renderTo(MemorySegment.ofBuffer(segment), offset, destinationWidth, destinationHeight, stride);
    }

    public void renderTo(@NonNull MemorySegment segment, long offset, int destinationWidth, int destinationHeight, long stride) {
        segment = segment.asSlice(offset);

        if ((segment.address() & 0b11) != 0) {
            throw new IllegalStateException("segment.address() + offset is not aligned for a RGBA8888 structure.");
        } else if (stride > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The requested stride exceeds the integer limit.");
        }

        RectangleI viewport;
        Matrix4dc projectionMatrix;

        if (this.viewBox.isPresent()) {
            viewport = this.viewBox.get();
            projectionMatrix = new Matrix4d().translate(viewport.x(), viewport.y(), 0.0D).scale(((double) viewport.width()) / destinationWidth, ((double) viewport.height()) / destinationHeight, 1.0D);
        } else {
            viewport = new RectangleI(0, 0, destinationWidth, destinationHeight);
            projectionMatrix = new Matrix4d();
        }

        try (PlutoVGSurface surface = new PlutoVGSurface(segment, destinationWidth, destinationHeight, (int) stride)) {
            try (PlutoVGCanvas canvas = new PlutoVGCanvas(surface)) {
                this.render(canvas, this.viewBox.get(), projectionMatrix);
            }

//            surface.writeToPNG("XIF.png");
            surface.convertDataToRGBA8888Inplace();
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
    }
}
