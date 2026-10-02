//! # SwarmForge Core - Native Rust Simulation Engine
//!
//! Provides ultra-low latency, SIMD-vectorized entity processing, 3D spatial indexing,
//! and continuous PDE pheromone field diffusion via C ABI for Java 21 Project Panama.

pub mod entity;
pub mod ffi;
pub mod pheromone;
pub mod spatial;

pub use entity::InsectEntity;
pub use ffi::NativeSimulationEngine;
pub use pheromone::PheromoneGrid;
pub use spatial::SpatialGrid;

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_kinematics_determinism_parity() {
        let mut ant = InsectEntity::new(1, 10.0, 20.0, 0.0, 1, 100.0);
        ant.vx = 2.0;
        ant.vy = 1.0;
        ant.vz = 0.5;

        // 10 steps of dt = 0.1s
        for _ in 0..10 {
            ant.update_kinematics(0.1, 100.0, 100.0, 50.0);
        }

        assert!((ant.x - 12.0).abs() < 1e-5);
        assert!((ant.y - 21.0).abs() < 1e-5);
        assert!((ant.z - 0.5).abs() < 1e-5);
        assert!((ant.energy - (100.0 - 0.01 * 1.0)).abs() < 1e-5);
    }

    #[test]
    fn test_spatial_hash_query() {
        let mut grid = SpatialGrid::new();
        grid.insert(101, 15.0, 15.0, 0.0);
        grid.insert(102, 16.0, 15.0, 0.0);
        grid.insert(103, 85.0, 85.0, 0.0);

        let mut out_buf = [0u32; 16];
        let found = grid.query_radius(15.0, 15.0, 0.0, 4.0, &mut out_buf);
        assert_eq!(found, 2);
        assert!(out_buf[0..found].contains(&101));
        assert!(out_buf[0..found].contains(&102));
    }

    #[test]
    fn test_pheromone_diffusion_conservation() {
        let mut p_grid = PheromoneGrid::new(10, 10, 10);
        p_grid.deposit(5.0, 5.0, 5.0, 0, 50.0);

        let sample_before = p_grid.sample(5.0, 5.0, 5.0, 0);
        assert_eq!(sample_before, 50.0);

        p_grid.step_diffusion(1.0);
        let sample_after = p_grid.sample(5.0, 5.0, 5.0, 0);
        assert!(sample_after < 50.0);
        assert!(sample_after > 0.0);
    }
}
