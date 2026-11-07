Note: The current state of this repository is rather unstable as it is undergoing major refractors.

# s2dmenues

The worst mod everyone asked for.

## Hard dependencies:

- Requires Galimulator 5.0.2, exactly.
- Requires SLL 4.0.0-a20250819 or newer.
- Requires SLAPI 4.0.0-a20260925 or newer.
- Requires LWJGL 3.4.2 or newer (excluding 3.5.X or newer).
- Requires lwjgl-harfbuzz 3.4.2 or newer
- Requires Wayland with zwp_text_input_v3 (e.g. kde-plasma version 6.7.4).
- Requires evdev (should be installed on most Linux/BSD systems).
- Requires libxkbcommon packaged as an *.so file (should be installed on most Linux/BSD systems).
- Requires libwayland-client packaged as an *.so file
- Requires **plutovg** 1.3.3 or newer but older than 2.0.0 (older 1.X releases might be fine but untested) packaged as an *.so file
- Requires **plutosvg** 0.0.8 or newer but older than 1.0.0 (older might be fine but untested) packaged as an *.so file

The abovementioned dependencies must be installed separately to this mod. Native dependencies
must be placed on the system/user native library path (e.g. installed in `/usr/lib64/`).

For debugging purposes it is recommended to build plutosvg from https://github.com/stianloader/plutosvg
The built `libplutosvg.so.0.0.8` can be then placed in the current working directly; S2DMenues will then
use 

## On X11 (for debugging only)

This mod can run on X11 for debugging purposes only.
Running with X11 leads to keyboard-related issues due to issues in GLFW,
using X11 is only intended for use with renderdoc which does not support
Wayland at this point in time.

- Requires libxkbcommon-x11 packaged as an *.so file
- Requires libx11-xcb packaged as an *.so file

## Hard incompatibilities

- Incompatible with LWJGL 2.X

## Fully unuspported environment

- Incompatible with Cocoa
- Incompatible with Win32

For Windows systems, please use WSL to run this mod using Wayland.
On Mac systems, you're on your own but there should be some way of running Wayland on a mac device.
