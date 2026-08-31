package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Compact 16-bit Packed Endocrine & Hormonal Vector Component.
 * Encodes Juvenile Hormone (JH), Ecdysone (ECD), and Octopamine (OCT) titers
 * into two packed 16-bit short primitives (0..65535 resolution), halving entity memory
 * footprint while maintaining high physiological fidelity.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class EndocrineBitpackComponent extends Component {

    private static final float SCALE = 65535.0f;

    public short packedJhEcd = 0; // High 16 bits = JH, Low 16 bits = ECD
    public short packedOct = 0;   // Octopamine

    public void setJuvenileHormone(float val) {
        int scaled = Math.min(65535, Math.max(0, (int) (val * SCALE)));
        packedJhEcd = (short) scaled;
    }

    public float getJuvenileHormone() {
        return (packedJhEcd & 0xFFFF) / SCALE;
    }

    public void setOctopamine(float val) {
        int scaled = Math.min(65535, Math.max(0, (int) (val * SCALE)));
        packedOct = (short) scaled;
    }

    public float getOctopamine() {
        return (packedOct & 0xFFFF) / SCALE;
    }
}
