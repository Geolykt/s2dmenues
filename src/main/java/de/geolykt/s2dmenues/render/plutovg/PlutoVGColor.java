package de.geolykt.s2dmenues.render.plutovg;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

import org.joml.Vector4fc;
import org.jspecify.annotations.NonNull;

public final class PlutoVGColor {

    @NonNull
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_FLOAT.withName("r"),
        ValueLayout.JAVA_FLOAT.withName("g"),
        ValueLayout.JAVA_FLOAT.withName("b"),
        ValueLayout.JAVA_FLOAT.withName("a")
    ).withName("plutovg_color_t");

    public static final long OFFSET_A = PlutoVGColor.LAYOUT.byteOffset(PathElement.groupElement("a"));
    public static final long OFFSET_B = PlutoVGColor.LAYOUT.byteOffset(PathElement.groupElement("b"));
    public static final long OFFSET_G = PlutoVGColor.LAYOUT.byteOffset(PathElement.groupElement("g"));
    public static final long OFFSET_R = PlutoVGColor.LAYOUT.byteOffset(PathElement.groupElement("r"));

    @NonNull
    private final MemorySegment address;

    public PlutoVGColor(@NonNull Arena arena, float r, float g, float b, float a) {
        this.address = arena.allocate(PlutoVGColor.LAYOUT, 1);
        this.address.set(ValueLayout.JAVA_FLOAT, OFFSET_R, r);
        this.address.set(ValueLayout.JAVA_FLOAT, OFFSET_G, g);
        this.address.set(ValueLayout.JAVA_FLOAT, OFFSET_B, b);
        this.address.set(ValueLayout.JAVA_FLOAT, OFFSET_A, a);
    }

    public PlutoVGColor(@NonNull Arena arena, @NonNull Vector4fc jomlColor) {
        this(arena, jomlColor.x(), jomlColor.y(), jomlColor.z(), jomlColor.w());
    }

    @NonNull
    public final MemorySegment getAddress() {
        return this.address;
    }
}
