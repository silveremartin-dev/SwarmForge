package org.swarmforge.core.simulation.gpu;

import com.aparapi.Kernel;

/**
 * OpenCL / Aparapi Hardware-Accelerated Kernel for Mandibular Biomechanics and Cuticular Strike Physics.
 *
 * Computes mandibular closure torque, cuticular stress distribution, and mechanical puncture
 * force during predation, substrate excavation, and inter-colonial combat across thousands of entities in parallel.
 *
 * Formula:
 * \[F_{\text{bite}} = k_{\text{caste}} \cdot M_{\text{mass}}^{0.67} \cdot \eta_{\text{mandible}}\]
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class MandibularBiomechanicsKernel extends Kernel {

    private final int agentCount;
    private final float[] bodyMassGrams;
    private final float[] mandibleLeverRatios;
    private final float[] cuticularWearRatios;
    private final float[] strikeVelocities;
    private final float[] outputBiteForcesNewtons;
    private final float[] outputChitinStressMpa;

    public MandibularBiomechanicsKernel(int agentCount,
                                        float[] bodyMassGrams,
                                        float[] mandibleLeverRatios,
                                        float[] cuticularWearRatios,
                                        float[] strikeVelocities,
                                        float[] outputBiteForcesNewtons,
                                        float[] outputChitinStressMpa) {
        this.agentCount = agentCount;
        this.bodyMassGrams = bodyMassGrams;
        this.mandibleLeverRatios = mandibleLeverRatios;
        this.cuticularWearRatios = cuticularWearRatios;
        this.strikeVelocities = strikeVelocities;
        this.outputBiteForcesNewtons = outputBiteForcesNewtons;
        this.outputChitinStressMpa = outputChitinStressMpa;
    }

    @Override
    public void run() {
        int i = getGlobalId();
        if (i < agentCount) {
            float mass = bodyMassGrams[i];
            float leverRatio = mandibleLeverRatios[i];
            float wear = cuticularWearRatios[i];
            float velocity = strikeVelocities[i];

            // Allometric muscle scaling: F ~ M^(2/3)
            float scaledMass = (float) Math.pow(mass, 0.67);
            float baseForce = 12.5f * scaledMass * leverRatio;

            // Wear degradation factor (1.0 = sharp, 0.2 = heavily blunted)
            float sharpnessFactor = 1.0f - 0.8f * wear;
            float kineticBonus = 0.5f * mass * velocity * velocity;

            float netForce = (baseForce * sharpnessFactor) + (kineticBonus * 0.1f);
            outputBiteForcesNewtons[i] = Math.max(0.0f, netForce);

            // Chitin stress concentration: \sigma = F / A_contact
            float contactAreaMm2 = 0.02f + 0.08f * wear;
            float stressMpa = netForce / contactAreaMm2;
            outputChitinStressMpa[i] = stressMpa;
        }
    }
}
