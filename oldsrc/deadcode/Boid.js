class Boid {
  constructor(x, y) {
    this.acceleration = createVector(0, 0);
    this.velocity = p5.Vector.random2D();
    this.position = createVector(x, y);
    this.r = 3.0; // Taille (rayon du triangle)
    this.maxSpeed = 3; // Vitesse maximale
    this.maxForce = 0.05; // Force de direction maximale
    this.color = color(random(360), 70, 70); // Couleur HSL aléatoire
  }

  run(boids) {
    this.flock(boids); // Applique les règles de Boids
    this.update(); // Met à jour position/vitesse
    this.borders(); // Gère les bords de l'écran (wraparound)
    this.render(); // Dessine le Boid
  }

  // Applique une force à l'accélération
  applyForce(force) {
    this.acceleration.add(force);
  }

  // Calcule et applique les trois règles
  flock(boids) {
    let separation = this.separate(boids);
    let alignment = this.align(boids);
    let cohesion = this.cohesion(boids);

    // Ajuste l'importance de chaque force (poids)
    separation.mult(1.5);
    alignment.mult(1.0);
    cohesion.mult(1.0);

    // Applique les forces
    this.applyForce(separation);
    this.applyForce(alignment);
    this.applyForce(cohesion);
  }

  // Mises à jour de la physique
  update() {
    this.velocity.add(this.acceleration);
    this.velocity.limit(this.maxSpeed);
    this.position.add(this.velocity);
    // Réinitialise l'accélération à 0 pour le prochain cycle
    this.acceleration.mult(0);
  }

  // Dessine le Boid sous forme de triangle
  render() {
    // Calcule l'angle pour orienter le triangle dans la direction de la vitesse
    let theta = this.velocity.heading() + radians(90);
    fill(this.color);
    stroke(255);
    push();
    translate(this.position.x, this.position.y);
    rotate(theta);
    beginShape();
    vertex(0, -this.r * 2);
    vertex(-this.r, this.r * 2);
    vertex(this.r, this.r * 2);
    endShape(CLOSE);
    pop();
  }

  // Gère l'effet "wraparound" aux bords de l'écran
  borders() {
    if (this.position.x < -this.r) this.position.x = width + this.r;
    if (this.position.y < -this.r) this.position.y = height + this.r;
    if (this.position.x > width + this.r) this.position.x = -this.r;
    if (this.position.y > height + this.r) this.position.y = -this.r;
  }

  // === Les trois règles de Boids ===

  // 1. Separation : Cherche les Boids proches et s'éloigne d'eux
  separate(boids) {
    let desiredSeparation = 25.0; // Distance minimale souhaitée
    let steer = createVector(0, 0);
    let count = 0;

    for (let other of boids) {
      let d = p5.Vector.dist(this.position, other.position);
      // Si le Boid est un voisin et est trop proche
      if ((d > 0) && (d < desiredSeparation)) {
        // Calcule le vecteur pointant loin du voisin
        let diff = p5.Vector.sub(this.position, other.position);
        diff.normalize();
        diff.div(d); // Plus il est proche, plus la force est grande
        steer.add(diff);
        count++;
      }
    }

    if (count > 0) {
      steer.div(count); // Moyenne de la direction
    }

    if (steer.mag() > 0) {
      steer.normalize();
      steer.mult(this.maxSpeed);
      steer.sub(this.velocity);
      steer.limit(this.maxForce);
    }
    return steer;
  }

  // 2. Alignment : Vise à correspondre à la vitesse moyenne des voisins
  align(boids) {
    let neighborhoodRadius = 50; // Rayon de perception
    let sum = createVector(0, 0);
    let count = 0;

    for (let other of boids) {
      let d = p5.Vector.dist(this.position, other.position);
      if ((d > 0) && (d < neighborhoodRadius)) {
        sum.add(other.velocity);
        count++;
      }
    }

    if (count > 0) {
      sum.div(count);
      sum.normalize();
      sum.mult(this.maxSpeed);
      // Implémentation du "steering" : force = vitesse_désirée - vitesse_actuelle
      let steer = p5.Vector.sub(sum, this.velocity); 
      steer.limit(this.maxForce);
      return steer;
    } else {
      return createVector(0, 0);
    }
  }

  // 3. Cohesion : Vise à se diriger vers la position moyenne des voisins
  cohesion(boids) {
    let neighborhoodRadius = 50; // Rayon de perception
    let sum = createVector(0, 0); // Pour stocker la somme des positions
    let count = 0;

    for (let other of boids) {
      let d = p5.Vector.dist(this.position, other.position);
      if ((d > 0) && (d < neighborhoodRadius)) {
        sum.add(other.position);
        count++;
      }
    }

    if (count > 0) {
      sum.div(count); // Calcul de la position moyenne (centre de masse)
      return this.seek(sum); // Se diriger vers ce point
    } else {
      return createVector(0, 0);
    }
  }
  
  // Fonction utilitaire pour "poursuivre" un point cible
  seek(target) {
    let desired = p5.Vector.sub(target, this.position); // Vecteur de la position actuelle à la cible
    desired.normalize();
    desired.mult(this.maxSpeed);
    
    let steer = p5.Vector.sub(desired, this.velocity);
    steer.limit(this.maxForce);
    return steer;
  }
}