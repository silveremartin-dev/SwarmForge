//! C Foreign Function Interface (FFI) bindings for Java 21 Project Panama.

use crate::entity::AntEntity;
use crate::pheromone::PheromoneGrid;
use crate::spatial::SpatialGrid;

pub struct NativeSimulationEngine {
    pub width: f32,
    pub depth: f32,
    pub height: f32,
    pub entities: Vec<Option<AntEntity>>,
    pub active_count: usize,
    pub next_id: u32,
    pub spatial: SpatialGrid,
    pub pheromones: PheromoneGrid,
    pub surface_temp: f32,
    pub surface_moisture: f32,
    pub leaf_litter: f32,
}

impl NativeSimulationEngine {
    pub fn new(w: i32, d: i32, h: i32) -> Self {
        Self {
            width: w as f32,
            depth: d as f32,
            height: h as f32,
            entities: Vec::with_capacity(16384),
            active_count: 0,
            next_id: 0,
            spatial: SpatialGrid::new(),
            pheromones: PheromoneGrid::new(w.max(1) as usize, d.max(1) as usize, h.max(1) as usize),
            surface_temp: 20.0,
            surface_moisture: 0.5,
            leaf_litter: 100.0,
        }
    }

    pub fn step(&mut self, dt: f32) {
        self.spatial.clear();
        self.pheromones.step_diffusion(dt);

        let w = self.width;
        let d = self.depth;
        let h = self.height;

        for opt in self.entities.iter_mut() {
            if let Some(agent) = opt {
                if agent.is_alive {
                    agent.update_kinematics(dt, w, d, h);
                    self.spatial.insert(agent.id, agent.x, agent.y, agent.z);
                }
            }
        }
    }
}

// ── C ABI Exported Functions ──────────────────────────────────────────────────

#[no_mangle]
pub extern "C" fn swarmforge_engine_create(w: i32, d: i32, h: i32) -> *mut NativeSimulationEngine {
    let engine = Box::new(NativeSimulationEngine::new(w, d, h));
    Box::into_raw(engine)
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_destroy(ptr: *mut NativeSimulationEngine) {
    if !ptr.is_null() {
        unsafe {
            drop(Box::from_raw(ptr));
        }
    }
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_step(ptr: *mut NativeSimulationEngine, dt: f32) {
    if let Some(engine) = unsafe { ptr.as_mut() } {
        engine.step(dt);
    }
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_spawn_entity(
    ptr: *mut NativeSimulationEngine,
    x: f32,
    y: f32,
    z: f32,
    caste: i32,
    energy: f32,
) -> i32 {
    if let Some(engine) = unsafe { ptr.as_mut() } {
        let id = engine.next_id;
        engine.next_id += 1;
        let agent = AntEntity::new(id, x, y, z, (caste & 0xFF) as u8, energy);

        if (id as usize) < engine.entities.len() {
            engine.entities[id as usize] = Some(agent);
        } else {
            engine.entities.push(Some(agent));
        }
        engine.active_count += 1;
        engine.spatial.insert(id, x, y, z);
        id as i32
    } else {
        -1
    }
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_despawn_entity(ptr: *mut NativeSimulationEngine, entity_id: i32) -> i32 {
    if entity_id < 0 {
        return 0;
    }
    if let Some(engine) = unsafe { ptr.as_mut() } {
        let idx = entity_id as usize;
        if idx < engine.entities.len() {
            if let Some(ref mut agent) = engine.entities[idx] {
                if agent.is_alive {
                    agent.is_alive = false;
                    engine.entities[idx] = None;
                    engine.active_count = engine.active_count.saturating_sub(1);
                    return 1;
                }
            }
        }
    }
    0
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_get_entity_count(ptr: *mut NativeSimulationEngine) -> i32 {
    if let Some(engine) = unsafe { ptr.as_ref() } {
        engine.active_count as i32
    } else {
        0
    }
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_get_entity_pos(
    ptr: *mut NativeSimulationEngine,
    entity_id: i32,
    out_xyz: *mut f32,
) -> i32 {
    if ptr.is_null() || out_xyz.is_null() || entity_id < 0 {
        return 0;
    }
    if let Some(engine) = unsafe { ptr.as_ref() } {
        let idx = entity_id as usize;
        if idx < engine.entities.len() {
            if let Some(ref agent) = engine.entities[idx] {
                if agent.is_alive {
                    unsafe {
                        *out_xyz.offset(0) = agent.x;
                        *out_xyz.offset(1) = agent.y;
                        *out_xyz.offset(2) = agent.z;
                    }
                    return 1;
                }
            }
        }
    }
    0
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_query_radius(
    ptr: *mut NativeSimulationEngine,
    x: f32,
    y: f32,
    z: f32,
    radius: f32,
    out_buf: *mut i32,
    max_count: i32,
) -> i32 {
    if ptr.is_null() || out_buf.is_null() || max_count <= 0 {
        return 0;
    }
    if let Some(engine) = unsafe { ptr.as_ref() } {
        let mut u32_buf = vec![0u32; max_count as usize];
        let found = engine.spatial.query_radius(x, y, z, radius, &mut u32_buf);
        for i in 0..found {
            unsafe {
                *out_buf.offset(i as isize) = u32_buf[i] as i32;
            }
        }
        found as i32
    } else {
        0
    }
}

#[no_mangle]
pub extern "C" fn swarmforge_engine_update_boundaries(
    ptr: *mut NativeSimulationEngine,
    temp: f32,
    moisture: f32,
    litter: f32,
) {
    if let Some(engine) = unsafe { ptr.as_mut() } {
        engine.surface_temp = temp;
        engine.surface_moisture = moisture;
        engine.leaf_litter = litter;
    }
}
