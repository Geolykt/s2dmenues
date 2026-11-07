package de.geolykt.s2dmenues.render.plutovg;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

/**
 * java.lang.foreign bindings to the plutovg and plutosvg libraries.
 *
 * <p>For internal use only; this class will not load if the plutovg and plutosvg libraries
 * are not available on the native library path.
 */
@Internal
public final class PlutoVGFFI {

    @Nullable
    static String lastErrorMessage;

    /**
     *(plutosvg_document_t* surface) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOSVG_DOCUMENT_DESTROY;

    /**
     *(const char* data, int length, float width, float height, plutovg_destroy_func_t destroy_func, void* closure) -&gt; plutosvg_document_t*
     */
    @NonNull
    static final MethodHandle MH_PLUTOSVG_DOCUMENT_LOAD_DATA;

    /**
     *(const plutosvg_document_t* document, const char* id, int width, int height, const plutovg_color_t* current_color, plutosvg_palette_func_t palette_func, void* closure) -&gt; plutovg_surface_t*
     */
    @NonNull
    static final MethodHandle MH_PLUTOSVG_DOCUMENT_RENDER;

    /**
     * (plutovg_surface_t* surface) -&gt; plutovg_canvas_t*
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_CREATE;

    /**
     * (plutovg_canvas_t* canvas) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_DESTROY;

    /**
     *(plutovg_canvas_t* canvas, const plutovg_path_t* path) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_FILL_PATH;

    /**
     *(plutovg_canvas_t* canvas, const float x, const float y, const float w, const float h) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_FILL_RECT;

    /**
     *(plutovg_canvas_t* canvas, const plutovg_path_t* path) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_STROKE_PATH;

    /**
     *(plutovg_canvas_t* canvas, const plutovg_color_t* color) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_SET_COLOR;

    /**
     *(plutovg_canvas_t* canvas, const float opacity) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_SET_OPACITY;

    /**
     *(plutovg_canvas_t* canvas, const plutovg_matrix_t* matrix) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_SET_MATRIX;

    /**
     *(plutovg_canvas_t* canvas, const float x, const float y, const float w, const float h) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CANVAS_STROKE_RECT;

    /**
     * (unsigned char* destination, const unsigned char* source, int width, int height, int stride) -&gt; void
     *
     * <p>Converts a premultiplied ARGB8888 to a non-premultiplied RGBA8888 buffer. Source and destination buffers have the
     * same stride. Source and destination buffers may overlap.
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_CONVERT_ARGB_TO_RGBA;

    /**
     *(void) -&gt; plutovg_path_create*
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_PATH_CREATE;

    /**
     *(plutovg_path_t* path) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_PATH_DESTROY;

    /**
     *(const plutovg_path_t* path, plutovg_rect_t* extents, boolean tight) .&gt; float
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_PATH_EXTENTS;

    /**
     *(plutovg_path_t* path, const char* data, int length) -&gt; boolean
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_PATH_PARSE;

    /**
     * (int width, int height) -&gt; plutovg_surface_t*
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_CREATE;

    /**
     * (unsigned char* data, int width, int height, int stride) -&gt; plutovg_surface_t*
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_CREATE_FOR_DATA;

    /**
     *(plutovg_surface_t* surface) -&gt; void
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_DESTROY;

    /**
     *(plutovg_surface_t* surface) -&gt; unsigned char*
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_GET_DATA;

    /**
     *(plutovg_surface_t* surface) -&gt; int
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_GET_HEIGHT;

    /**
     *(plutovg_surface_t* surface) -&gt; int
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_GET_STRIDE;

    /**
     *(plutovg_surface_t* surface) -&gt; int
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_GET_WIDTH;

    /**
     *(plutovg_surface_t* surface, const char* filename) -&gt; boolean
     */
    @NonNull
    static final MethodHandle MH_PLUTOVG_SURFACE_WRITE_TO_PNG;

    static final boolean PLUTOSVG_WITH_DEBUG_DOCUMENT_LOAD;
    static final boolean PLUTOSVG_WITH_DEBUG_DOCUMENT_RENDER;

    static {
        SymbolLookup plutoVG, plutoSVG;

        try {
            plutoSVG = SymbolLookup.libraryLookup(Path.of("libplutosvg.so.0.0.8"), Arena.global());
            LoggerFactory.getLogger(PlutoVGFFI.class).debug("Using locally provided libplutosvg bindings.");
        } catch (IllegalArgumentException expected) {
            try {
                plutoSVG = SymbolLookup.libraryLookup("libplutosvg.so.0.0.8", Arena.global());
            } catch (IllegalArgumentException e1) {
                try {
                    plutoSVG = SymbolLookup.libraryLookup("libplutosvg.so.0", Arena.global());
                } catch (IllegalArgumentException e2) {
                    e2.addSuppressed(e1);
                    throw new LinkageError("Cannot resolve library plutosvg, version 0.X (preferring 0.0.8) packaged as a *.so file", e2);
                }
            }

            LoggerFactory.getLogger(PlutoVGFFI.class).debug("Using system libplutosvg bindings.");
        }

        try {
            plutoVG = SymbolLookup.libraryLookup("libplutovg.so.1.3.3", Arena.global());
        } catch (IllegalArgumentException e1) {
            try {
                plutoVG = SymbolLookup.libraryLookup("libplutovg.so.1", Arena.global());
            } catch (IllegalArgumentException e2) {
                e2.addSuppressed(e1);
                throw new LinkageError("Cannot resolve library plutovg, version 1.X (preferring 1.3.3) packaged as a *.so file", e2);
            }
        }

        Linker linker = Linker.nativeLinker();

        MH_PLUTOVG_CANVAS_CREATE = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_create"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_CANVAS_FILL_RECT = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_fill_rect"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT));
        MH_PLUTOVG_CANVAS_FILL_PATH = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_fill_path"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_CANVAS_SET_COLOR = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_set_color"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_CANVAS_SET_OPACITY = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_set_opacity"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT));
        MH_PLUTOVG_CANVAS_SET_MATRIX = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_set_matrix"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_CANVAS_STROKE_RECT = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_stroke_rect"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT));
        MH_PLUTOVG_CANVAS_STROKE_PATH = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_stroke_path"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_CANVAS_DESTROY = linker.downcallHandle(plutoVG.findOrThrow("plutovg_canvas_destroy"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        MH_PLUTOVG_CONVERT_ARGB_TO_RGBA = linker.downcallHandle(plutoVG.findOrThrow("plutovg_convert_argb_to_rgba"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        MH_PLUTOVG_PATH_CREATE = linker.downcallHandle(plutoVG.findOrThrow("plutovg_path_create"), FunctionDescriptor.of(ValueLayout.ADDRESS));
        MH_PLUTOVG_PATH_DESTROY = linker.downcallHandle(plutoVG.findOrThrow("plutovg_path_destroy"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        MH_PLUTOVG_PATH_EXTENTS = linker.downcallHandle(plutoVG.findOrThrow("plutovg_path_extents"), FunctionDescriptor.of(ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));
        MH_PLUTOVG_PATH_PARSE = linker.downcallHandle(plutoVG.findOrThrow("plutovg_path_parse"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        MH_PLUTOVG_SURFACE_CREATE = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_create"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        MH_PLUTOVG_SURFACE_CREATE_FOR_DATA = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_create_for_data"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        MH_PLUTOVG_SURFACE_DESTROY = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_destroy"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        MH_PLUTOVG_SURFACE_GET_DATA = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_get_data"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOVG_SURFACE_GET_WIDTH = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_get_width"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        MH_PLUTOVG_SURFACE_GET_HEIGHT = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_get_height"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        MH_PLUTOVG_SURFACE_GET_STRIDE = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_get_stride"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        MH_PLUTOVG_SURFACE_WRITE_TO_PNG = linker.downcallHandle(plutoVG.findOrThrow("plutovg_surface_write_to_png"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        MH_PLUTOSVG_DOCUMENT_DESTROY = linker.downcallHandle(plutoSVG.findOrThrow("plutosvg_document_destroy"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

        Optional<MemorySegment> debugDocumentLoadAddr = plutoSVG.find("stianloader_dbg_plutosvg_document_load_from_data");

        if (debugDocumentLoadAddr.isPresent()) {
            MethodHandle documentLoadMH = linker.downcallHandle(debugDocumentLoadAddr.get(), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            MemorySegment setErrorMessageUCStub;

            try {
                setErrorMessageUCStub = linker.upcallStub(MethodHandles.lookup().findStatic(PlutoVGFFI.class, "setErrorMessage", MethodType.methodType(void.class, MemorySegment.class, int.class)), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), Arena.global());
            } catch (ReflectiveOperationException e) {
                throw new LinkageError("Cannot create upcall stub for the setErrorMessage function", e);
            }

            documentLoadMH = MethodHandles.insertArguments(documentLoadMH, 6, setErrorMessageUCStub);

            MH_PLUTOSVG_DOCUMENT_LOAD_DATA = documentLoadMH;
            PLUTOSVG_WITH_DEBUG_DOCUMENT_LOAD = true;
        } else {
            MH_PLUTOSVG_DOCUMENT_LOAD_DATA = linker.downcallHandle(plutoSVG.findOrThrow("plutosvg_document_load_from_data"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            PLUTOSVG_WITH_DEBUG_DOCUMENT_LOAD = false;
        }

        Optional<MemorySegment> debugDocumentRenderAddr = plutoSVG.find("stianloader_dbg_plutosvg_document_render_to_surface");

        if (debugDocumentRenderAddr.isPresent()) {
            MethodHandle documentRenderMH = linker.downcallHandle(debugDocumentRenderAddr.get(), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            MemorySegment renderLogUCStub;

            try {
                renderLogUCStub = linker.upcallStub(MethodHandles.lookup().findStatic(PlutoVGFFI.class, "onRenderLog", MethodType.methodType(void.class, MemorySegment.class, int.class)), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), Arena.global());
            } catch (ReflectiveOperationException e) {
                throw new LinkageError("Cannot create upcall stub for the onRenderLog function", e);
            }

            documentRenderMH = MethodHandles.insertArguments(documentRenderMH, 7, renderLogUCStub);

            MH_PLUTOSVG_DOCUMENT_RENDER = documentRenderMH;
            PLUTOSVG_WITH_DEBUG_DOCUMENT_RENDER = true;
        } else {
            MH_PLUTOSVG_DOCUMENT_RENDER = linker.downcallHandle(plutoSVG.findOrThrow("plutosvg_document_render_to_surface"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            PLUTOSVG_WITH_DEBUG_DOCUMENT_RENDER = false;
        }
    }

    @Internal // This function is called via the linker; This annotation is just there to make static analysis tools shut up.
    private static void onRenderLog(@NotNull MemorySegment segment, int len) {
        String msg = new String(segment.reinterpret(len).toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);

        if (PlutoVGFFI.lastErrorMessage == null || PlutoVGFFI.lastErrorMessage.isEmpty()) {
            PlutoVGFFI.lastErrorMessage = msg;
        } else {
            PlutoVGFFI.lastErrorMessage += "\n" + msg;
        }
    }

    @Internal // This function is called via the linker; This annotation is just there to make static analysis tools shut up.
    private static void setErrorMessage(@NotNull MemorySegment segment, int len) {
        PlutoVGFFI.lastErrorMessage = new String(segment.reinterpret(len).toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);
    }

    /**
     * Hidden constructor.
     */
    private PlutoVGFFI() {
        throw new AssertionError();
    }
}
