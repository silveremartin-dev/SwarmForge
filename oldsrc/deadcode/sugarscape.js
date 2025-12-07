// --- PARAMÈTRES GLOBAUX ---
const GRID_SIZE = 50; // La grille sera 50x50
let cellSize;
let sugarGrid = []; // Le tableau 2D pour le sucre
let agents = [];
const INITIAL_AGENTS = 100;

// --- CLASSE AGENT (L'individu) ---
class Agent {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.sugar = floor(random(5, 25));    // Richesse initiale
        this.metabolism = floor(random(1, 4)); // Consommation par tick
        this.vision = floor(random(1, 6));     // Distance de vision
        this.age = 0;
        this.maxAge = floor(random(60, 100));
        this.isAlive = true;
    }

    // Règle M (Move and Metabolize)
    moveAndEat() {
        if (!this.isAlive) return;

        // 1. Chercher le meilleur emplacement de sucre (dans la vision et inoccupé)
        let bestX = this.x, bestY = this.y;
        let maxSugar = -1;

        // Parcourt une fenêtre carrée définie par this.vision autour de l'agent
        for (let dx = -this.vision; dx <= this.vision; dx++) {
            for (let dy = -this.vision; dy <= this.vision; dy++) {
                // Calcule les nouvelles coordonnées avec le comportement toroïdal (wraparound)
                let newX = (this.x + dx + GRID_SIZE) % GRID_SIZE;
                let newY = (this.y + dy + GRID_SIZE) % GRID_SIZE;
                
                // On s'assure que la case n'est pas déjà occupée par un autre agent.
                let occupied = agents.some(a => a.isAlive && a.x === newX && a.y === newY);
                
                if (!occupied && sugarGrid[newX][newY] > maxSugar) {
                    maxSugar = sugarGrid[newX][newY];
                    bestX = newX;
                    bestY = newY;
                }
            }
        }
        
        // 2. Mouvement et Collecte
        this.x = bestX;
        this.y = bestY;
        
        // Collecte tout le sucre de la nouvelle case
        this.sugar += sugarGrid[this.x][this.y];
        sugarGrid[this.x][this.y] = 0; // Le sucre est instantanément consommé

        // 3. Métabolisme et Mort
        this.sugar -= this.metabolism;
        this.age++;

        if (this.sugar <= 0 || this.age > this.maxAge) {
            this.die();
        }
    }

    die() {
        this.isAlive = false;
    }

    display() {
        if (!this.isAlive) return;
        
        // La couleur de l'agent pourrait refléter sa richesse
        let wealthColor = map(this.sugar, 0, 100, 100, 360); // HSL Hue
        fill(wealthColor, 100, 50); // Couleur vive
        noStroke();
        ellipse(this.x * cellSize + cellSize / 2, this.y * cellSize + cellSize / 2, cellSize * 0.8, cellSize * 0.8);
    }
}

// --- FONCTIONS P5.JS ---

function setup() {
    createCanvas(600, 600);
    cellSize = width / GRID_SIZE;
    frameRate(10); // Ralentir la simulation pour la visualisation
    colorMode(HSL, 360, 100, 100); 

    initializeSugarscape();
    initializeAgents();
}

function draw() {
    // 1. Règle G : Croissance du sucre (Régénération Immédiate pour Sugarscape 1)
    growSugar();

    // 2. Règle M et R : Mouvement, Métabolisme et Remplacement des agents
    // L'ordre des agents est important, on les mélange
    shuffle(agents, true); 
    
    // Les agents se déplacent et mangent
    for (let agent of agents) {
        agent.moveAndEat();
    }
    
    // Supprimer les agents morts et les remplacer (Règle R - Remplacement)
    let livingAgents = agents.filter(a => a.isAlive);
    let deadAgentsCount = agents.length - livingAgents.length;
    agents = livingAgents;

    for (let i = 0; i < deadAgentsCount; i++) {
        addRandomAgent(); // Remplacement : un nouvel agent "jeune" naît
    }

    // 3. Affichage
    drawGrid();
    for (let agent of agents) {
        agent.display();
    }
}

// --- FONCTIONS UTILITAIRES ---

// Crée la grille de sucre (avec deux "pics" de sucre)
function initializeSugarscape() {
    for (let i = 0; i < GRID_SIZE; i++) {
        sugarGrid[i] = [];
        for (let j = 0; j < GRID_SIZE; j++) {
            // Logique de création de deux pics (simplifiée)
            let distanceToPeak1 = dist(i, j, GRID_SIZE * 0.25, GRID_SIZE * 0.75);
            let distanceToPeak2 = dist(i, j, GRID_SIZE * 0.75, GRID_SIZE * 0.25);
            
            // La quantité de sucre est inversement proportionnelle à la distance
            let maxCapacity1 = map(distanceToPeak1, 0, GRID_SIZE / 2, 10, 0);
            let maxCapacity2 = map(distanceToPeak2, 0, GRID_SIZE / 2, 10, 0);
            
            // La capacité maximale est le max des deux pics
            let capacity = floor(max(maxCapacity1, maxCapacity2));
            sugarGrid[i][j] = capacity;
        }
    }
}

// Règle G : Régénération du sucre (Immmediate Growback)
function growSugar() {
    for (let i = 0; i < GRID_SIZE; i++) {
        for (let j = 0; j < GRID_SIZE; j++) {
            // Le sucre repousse jusqu'à sa capacité maximale originale
            let capacity = getOriginalCapacity(i, j);
            if (sugarGrid[i][j] < capacity) {
                sugarGrid[i][j] = capacity; 
            }
        }
    }
}

// Calcule la capacité maximale originale (pour la régénération)
function getOriginalCapacity(i, j) {
    let distanceToPeak1 = dist(i, j, GRID_SIZE * 0.25, GRID_SIZE * 0.75);
    let distanceToPeak2 = dist(i, j, GRID_SIZE * 0.75, GRID_SIZE * 0.25);
    
    let maxCapacity1 = map(distanceToPeak1, 0, GRID_SIZE / 2, 10, 0);
    let maxCapacity2 = map(distanceToPeak2, 0, GRID_SIZE / 2, 10, 0);
    
    return floor(max(maxCapacity1, maxCapacity2));
}


// Initialise les agents
function initializeAgents() {
    for (let i = 0; i < INITIAL_AGENTS; i++) {
        addRandomAgent();
    }
}

// Ajoute un agent à une position aléatoire non occupée
function addRandomAgent() {
    let x, y;
    do {
        x = floor(random(GRID_SIZE));
        y = floor(random(GRID_SIZE));
    } while (agents.some(a => a.isAlive && a.x === x && a.y === y));

    agents.push(new Agent(x, y));
}

// Dessine l'environnement (la grille de sucre)
function drawGrid() {
    for (let i = 0; i < GRID_SIZE; i++) {
        for (let j = 0; j < GRID_SIZE; j++) {
            // Plus il y a de sucre, plus la case est jaune/foncée
            let sugarLevel = sugarGrid[i][j];
            let brightness = map(sugarLevel, 0, 10, 0, 100);
            fill(40, 100, brightness); // Teinte jaune
            noStroke();
            rect(i * cellSize, j * cellSize, cellSize, cellSize);
        }
    }
}