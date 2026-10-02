# SwarmForge Core - Native Rust Simulation Engine

High-performance native simulation backend for SwarmForge, designed to be called seamlessly from Java 21 via **Project Panama (Foreign Function & Memory API - JEP 454)**.

---

## ⚡ Performance Highlights
- **Zero GC Overhead**: Entity arrays, Morton-coded spatial structures, and diffusion matrices live off-heap in native memory.
- **Cache-Optimized Memory Layout**: Contiguous Struct-of-Arrays (SoA) and flat array linear probing for sub-microsecond neighbor queries.
- **Zero-Copy Interop**: Direct memory segment mapping (`java.lang.foreign.MemorySegment`) between Java and Rust.

---

## 🛠️ Building the Native Library

### Requirements
- Rust toolchain (1.75+ with Cargo)

### Build Command
```bash
# From within native/swarmforge-core-rust/
cargo build --release
```

This compiles:
- **Windows**: `target/release/swarmforge_core_rust.dll`
- **Linux**: `target/release/libswarmforge_core_rust.so`
- **macOS**: `target/release/libswarmforge_core_rust.dylib`

### Running SwarmForge with Native Engine
```bash
# Enable Rust engine via system property
java -Dswarmforge.engine=rust -jar swarmforge-server/target/swarmforge-server.jar

# Or via environment variable
export SWARMFORGE_ENGINE=rust
```
