//! 3D Spatial Partitioning Index for ultra-fast neighbor queries.

const CELL_SIZE: f32 = 4.0; // 4.0m interaction cell size
const TABLE_SIZE: usize = 65536; // 64K buckets
const TABLE_MASK: usize = TABLE_SIZE - 1;
const CHAIN_CAPACITY: usize = 64;

pub struct SpatialGrid {
    entries: Vec<u32>,
    counts: Vec<u8>,
    keys: Vec<u64>,
    dirty: Vec<u32>,
    dirty_count: usize,
}

impl SpatialGrid {
    pub fn new() -> Self {
        Self {
            entries: vec![u32::MAX; TABLE_SIZE * CHAIN_CAPACITY],
            counts: vec![0; TABLE_SIZE],
            keys: vec![u64::MAX; TABLE_SIZE],
            dirty: vec![0; TABLE_SIZE],
            dirty_count: 0,
        }
    }

    #[inline(always)]
    pub fn clear(&mut self) {
        for i in 0..self.dirty_count {
            let bucket = self.dirty[i] as usize;
            self.counts[bucket] = 0;
            self.keys[bucket] = u64::MAX;
        }
        self.dirty_count = 0;
    }

    #[inline(always)]
    fn pack_coords(x: f32, y: f32, z: f32) -> u64 {
        let cx = (x / CELL_SIZE).floor() as i32;
        let cy = (y / CELL_SIZE).floor() as i32;
        let cz = (z / CELL_SIZE).floor() as i32;
        
        let ux = (cx & 0x1FFFFF) as u64;
        let uy = (cy & 0x1FFFFF) as u64;
        let uz = (cz & 0x1FFFFF) as u64;
        (ux << 42) | (uy << 21) | uz
    }

    #[inline(always)]
    pub fn insert(&mut self, entity_id: u32, x: f32, y: f32, z: f32) {
        let key = Self::pack_coords(x, y, z);
        let mut bucket = ((key ^ (key >> 32)) as usize) & TABLE_MASK;

        for _ in 0..TABLE_SIZE {
            let stored = self.keys[bucket];
            if stored == u64::MAX {
                self.keys[bucket] = key;
                if self.dirty_count < TABLE_SIZE {
                    self.dirty[self.dirty_count] = bucket as u32;
                    self.dirty_count += 1;
                }
                break;
            }
            if stored == key {
                break;
            }
            bucket = (bucket + 1) & TABLE_MASK;
        }

        let count = self.counts[bucket] as usize;
        if count < CHAIN_CAPACITY {
            self.entries[bucket * CHAIN_CAPACITY + count] = entity_id;
            self.counts[bucket] += 1;
        }
    }

    pub fn query_radius(&self, cx: f32, cy: f32, cz: f32, radius: f32, out_buf: &mut [u32]) -> usize {
        let min_x = cx - radius;
        let max_x = cx + radius;
        let min_y = cy - radius;
        let max_y = cy + radius;
        let min_z = cz - radius;
        let max_z = cz + radius;

        let start_cx = (min_x / CELL_SIZE).floor() as i32;
        let end_cx = (max_x / CELL_SIZE).floor() as i32;
        let start_cy = (min_y / CELL_SIZE).floor() as i32;
        let end_cy = (max_y / CELL_SIZE).floor() as i32;
        let start_cz = (min_z / CELL_SIZE).floor() as i32;
        let end_cz = (max_z / CELL_SIZE).floor() as i32;

        let mut written = 0;
        let max_out = out_buf.len();

        for x in start_cx..=end_cx {
            for y in start_cy..=end_cy {
                for z in start_cz..=end_cz {
                    let ux = (x & 0x1FFFFF) as u64;
                    let uy = (y & 0x1FFFFF) as u64;
                    let uz = (z & 0x1FFFFF) as u64;
                    let key = (ux << 42) | (uy << 21) | uz;

                    let mut bucket = ((key ^ (key >> 32)) as usize) & TABLE_MASK;
                    for _ in 0..TABLE_SIZE {
                        let stored = self.keys[bucket];
                        if stored == u64::MAX {
                            break;
                        }
                        if stored == key {
                            let count = self.counts[bucket] as usize;
                            for i in 0..count {
                                if written >= max_out {
                                    return written;
                                }
                                out_buf[written] = self.entries[bucket * CHAIN_CAPACITY + i];
                                written += 1;
                            }
                            break;
                        }
                        bucket = (bucket + 1) & TABLE_MASK;
                    }
                }
            }
        }

        written
    }
}
