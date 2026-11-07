package de.geolykt.s2dmenues.render.plutovg;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4dc;
import org.jspecify.annotations.NonNull;

public class PlutoVGMatrix {
    @NonNull
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_FLOAT.withName("a"),
        ValueLayout.JAVA_FLOAT.withName("b"),
        ValueLayout.JAVA_FLOAT.withName("c"),
        ValueLayout.JAVA_FLOAT.withName("d"),
        ValueLayout.JAVA_FLOAT.withName("e"),
        ValueLayout.JAVA_FLOAT.withName("f")
    ).withName("plutovg_matrix_t");

    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_A = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("a"));
    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_B = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("b"));
    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_C = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("c"));
    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_D = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("d"));
    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_E = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("e"));
    @NonNull
    public static final VarHandle VH_PLTUOVG_MATRIX_F = PlutoVGMatrix.LAYOUT.varHandle(PathElement.groupElement("f"));

    @NonNull
    @Contract(pure = true)
    @CheckReturnValue
    public static PlutoVGMatrix fromJOML(@NonNull Arena arena, @NonNull Matrix4dc mat) {
        if (!mat.isAffine()) {
            throw new IllegalArgumentException("Cannot convert JOML Matrix4dc to a plutovg_matrix_t: Matrix is not affine.");
        } else if (mat.m02() != 0 || mat.m12() != 0 || mat.m22() != 1 || mat.m32() != 0) {
            throw new IllegalArgumentException("Cannot convert JOML Matrix4dc to a plutovg_matrix_t: penultimate row is not [0, 0, 1, 0].");
        } else if (mat.m20() != 0 || mat.m21() != 0) {
            throw new IllegalArgumentException("Cannot convert JOML Matrix4dc to a plutovg_matrix_t: penultimate column is not [0, 0, 1, 0]. Output mat:\n " + mat);
        } else {
            return new PlutoVGMatrix(arena, (float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11(), (float) mat.m30(), (float) mat.m31());
        }
    }

    @NonNull
    private final MemorySegment address;

    public PlutoVGMatrix(@NonNull Arena arena, float a, float b, float c, float d, float e, float f) {
        this.address = arena.allocate(PlutoVGMatrix.LAYOUT, 1);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_A.setRelease(this.address, 0L /* base offset */, a);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_B.setRelease(this.address, 0L, b);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_C.setRelease(this.address, 0L, c);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_D.setRelease(this.address, 0L, d);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_E.setRelease(this.address, 0L, e);
        PlutoVGMatrix.VH_PLTUOVG_MATRIX_F.setRelease(this.address, 0L, f);
    }

    @NonNull
    public final MemorySegment getAddress() {
        return this.address;
    }
}
