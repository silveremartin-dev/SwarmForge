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

/**
 * An API to simulate ant colonies.
 *
 * For some general documentation on ants, see https://www.antwiki.org/wiki/Welcome_to_AntWiki
 *
 *  Probably with only a few work, would be suited to simulate other eusocial colonies, starting with bees, wasps and termites.
 *
 * TODO:
 * A server to exchange Species, Maps, Simulations
 * A server to run distributed Simulations
 * A way to save and load nests designs and heightmaps
 * Shortcuts + icons + launchers
 * A 3D display interface, using either java3d or jmonkeyengine or lwjgl
 * add ColorMap to heightmap
 * A command line only run (no gui)
 * May be switch some constants to enum and change some classes names to Singleton, etc according to https://refactoring.guru/
 * may be have a generic cell system form other space filling tessellations: https://en.wikipedia.org/wiki/Honeycomb_%28geometry%29#Space-filling_polyhedra.5B2.5D for example using truncated octahedra or dodecahedrons
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

