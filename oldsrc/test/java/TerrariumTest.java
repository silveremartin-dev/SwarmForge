/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants;

import org.jants.util.Morton3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerrariumTest {

    private Terrarium terrarium;
    private final Morton3D mortonEncoder = new Morton3D();

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(0, 0, 0, 10, 10, 10);
    }

    @Test
    void testVoxelInsertionAndRetrieval() {
        // ARRANGE
        int x = 10, y = 5, z = 3;
        TerrariumCell cell = new TerrariumCell(x, y, z);
        cell.contents = TerrariumCell.contentsRock;

        // ACT
        terrarium.putCell(x, y, z, cell);

        // ASSERT
        assertEquals(cell, terrarium.getCell(x, y, z),
                "La récupération de la donnée doit correspondre à l'insertion.");
    }

    @Test
    void testVoxelUpdate() {
        // ARRANGE
        int x = 5, y = 5, z = 5;
        TerrariumCell cell = new TerrariumCell(x, y, z);
        cell.contents = TerrariumCell.contentsRock;
        terrarium.putCell(x, y, z, cell);

        // ACT
        cell.contents = TerrariumCell.contentsClay;
        terrarium.putCell(x, y, z, cell);

        // ASSERT
        assertEquals(cell, terrarium.getCell(x, y, z),
                "L'appel à putCell doit mettre à jour les données existantes.");
    }

    @Test
    void testSparseSpaceIsInitiallyNull() {
        // ARRANGE
        int xEmpty = 5, yEmpty = 5, zEmpty = 5;

        // ACT & ASSERT - Vérifier que l'espace est null
        assertNull(terrarium.getCell(xEmpty, yEmpty, zEmpty),
                "L'espace sparse (non inséré) doit retourner null.");
    }

    @Test
    void testVoxelRemoval() {
        // ARRANGE
        int x = 1, y = 1, z = 1;
        TerrariumCell cell = new TerrariumCell(x, y, z);
        cell.contents = TerrariumCell.contentsClay;
        terrarium.putCell(x, y, z, cell);
        assertNotNull(terrarium.getCell(x, y, z), "La cell doit exister avant la suppression.");

        // ACT
        terrarium.removeCell(x, y, z);

        // ASSERT
        assertNull(terrarium.getCell(x, y, z), "La cell doit être null après la suppression.");
    }

    @Test
    void testMortonCodeIntegrity() {
        // ARRANGE
        int x = 15, y = 17, z = 11;

        // ACT - Calculer le code de Morton attendu
        long expectedMortonCode = mortonEncoder.encode(x, y, z);

        // ACT - Simuler le décodage du code de Morton pour vérification
        int[] decodedCoords = mortonEncoder.decode(expectedMortonCode);

        // ASSERT
        assertArrayEquals(new int[]{x, y, z}, decodedCoords,
                "Le décodage du code de Morton doit retourner les coordonnées originales.");
    }
}