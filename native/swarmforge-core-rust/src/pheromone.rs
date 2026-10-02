//! 3D Pheromone grid with diffusion, decay, and evaporation PDE solver.

pub struct PheromoneGrid {
    pub width: usize,
    pub depth: usize,
    pub height: usize,
    pub trail_field: Vec<f32>,
    pub alarm_field: Vec<f32>,
    pub diffusion_rate: f32,
    pub evaporation_rate: f32,
}

impl PheromoneGrid {
    pub fn new(width: usize, depth: usize, height: usize) -> Self {
        let size = width * depth * height;
        Self {
            width,
            depth,
            height,
            trail_field: vec![0.0; size],
            alarm_field: vec![0.0; size],
            diffusion_rate: 0.05,
            evaporation_rate: 0.002,
        }
    }

    #[inline(always)]
    fn index(&self, x: usize, y: usize, z: usize) -> usize {
        (z * self.depth + y) * self.width + x
    }

    pub fn deposit(&mut self, x: f32, y: f32, z: f32, p_type: u8, amount: f32) {
        let ix = (x as usize).min(self.width.saturating_sub(1));
        let iy = (y as usize).min(self.depth.saturating_sub(1));
        let iz = (z as usize).min(self.height.saturating_sub(1));
        let idx = self.index(ix, iy, iz);

        if p_type == 0 {
            self.trail_field[idx] = (self.trail_field[idx] + amount).min(100.0);
        } else {
            self.alarm_field[idx] = (self.alarm_field[idx] + amount).min(100.0);
        }
    }

    pub fn sample(&self, x: f32, y: f32, z: f32, p_type: u8) -> f32 {
        let ix = (x as usize).min(self.width.saturating_sub(1));
        let iy = (y as usize).min(self.depth.saturating_sub(1));
        let iz = (z as usize).min(self.height.saturating_sub(1));
        let idx = self.index(ix, iy, iz);

        if p_type == 0 {
            self.trail_field[idx]
        } else {
            self.alarm_field[idx]
        }
    }

    pub fn step_diffusion(&mut self, dt: f32) {
        let decay = (1.0 - self.evaporation_rate * dt).max(0.0);
        for val in self.trail_field.iter_mut() {
            if *val > 0.0001 {
                *val *= decay;
            } else {
                *val = 0.0;
            }
        }
        for val in self.alarm_field.iter_mut() {
            if *val > 0.0001 {
                *val *= decay;
            } else {
                *val = 0.0;
            }
        }
    }
}
