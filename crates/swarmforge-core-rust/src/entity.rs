/// Representation of an individual insect entity in the native Rust engine.
#[derive(Debug, Clone, Copy, PartialEq)]
pub struct AntEntity {
    pub id: u32,
    pub x: float32,
    pub y: float32,
    pub z: float32,
    pub vx: float32,
    pub vy: float32,
    pub vz: float32,
    pub heading: float32,
    pub caste: u8,
    pub energy: float32,
    pub health: float32,
    pub is_alive: bool,
}

#[allow(non_camel_case_types)]
type float32 = f32;

impl AntEntity {
    pub fn new(id: u32, x: f32, y: f32, z: f32, caste: u8, energy: f32) -> Self {
        Self {
            id,
            x,
            y,
            z,
            vx: 0.0,
            vy: 0.0,
            vz: 0.0,
            heading: 0.0,
            caste,
            energy,
            health: 100.0,
            is_alive: true,
        }
    }

    #[inline(always)]
    pub fn update_kinematics(&mut self, dt: f32, width: f32, depth: f32, height: f32) {
        if !self.is_alive {
            return;
        }

        // Apply velocities with dampening and bounds clipping
        self.x += self.vx * dt;
        self.y += self.vy * dt;
        self.z += self.vz * dt;

        // Boundary constraints
        self.x = self.x.clamp(0.0, width);
        self.y = self.y.clamp(0.0, depth);
        self.z = self.z.clamp(0.0, height);

        // Natural basal metabolism consumption: E_loss = basal_rate * dt
        let basal_metabolism_rate = 0.01;
        self.energy = (self.energy - basal_metabolism_rate * dt).max(0.0);
        if self.energy <= 0.0 {
            self.health = (self.health - 5.0 * dt).max(0.0);
            if self.health <= 0.0 {
                self.is_alive = false;
            }
        }
    }
}
