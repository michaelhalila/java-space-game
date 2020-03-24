package fallinginvaders;

// Package contains: (19.12.2018)
//
// FallingInvaders.java - main program
// 
// Enums:
//
//  CraftType
//  MoveOrder
//  Particle
//
// Game logic components (one of each per game):
//
//  EnemyCommand - manages enemy ships and asteroids
//  GameState - manages score and game over
//  MoveLogic - gives enemy ships and asteroids their movement orders
//  ParticleSimulation - manages the background particles
//
// Entities (multiple copies per game):
//
//  Craft - player and enemy ships, and asteroids, are instances of Craft
//  Pew - ships fire pews; pews also score the Craft they destroy
//  Point - for easily listing x,y co-ordinates for the ParticleSimulation
//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Michael Halila
 */
public class FallingInvaders extends Application {

    // Program structure (19.12.2018)
    //
    // 1. Setup: graphics and game logic elements
    //  1.1 graphics
    //  1.2 particle simulation and canvas to draw it on
    //  1.3 create player ship
    //  1.4 create lists for other objects and create starting enemies
    //  1.5 set Scene
    //  1.6 HashMap for getting keyboard inputs
    //
    // 2. main game loop
    //  2.1 draw particles
    //  2.2 Execute keyboard commands unless gameover
    //  2.3 update pews
    //  2.4 update asteroids
    //  2.5 update enemy ships
    //  2.5.1 blow shit up
    //  2.6 blow up player ship if necessary
    //  2.7 update score
    //  2.8 update display texts
    //  2.9 delete destroyed objects
    //  2.10 check for game over
    //  2.11 get more enemies
    //
    // 3.0 animate particles
    //
    // 4.0 one-third-second maintenance: clean up explosions, allow player to fire again
    
    
    
    public final static int WIDTH = 600;
    public final static int LENGTH = 900;
    public static int FIRSTZONE = WIDTH / 3;
    public static int SECONDZONE = FIRSTZONE * 2;
    public static double SECOND = 1 * 1E9;
    public boolean playerHasFired = false;
    public boolean playerShipBlownUp = false;
    public Point previousExplosion = new Point(0, 0);

    @Override
    public void start(Stage window) throws Exception {
        
        //1. Setup: graphic and game logic elements

        Random random = new Random();
        GameState game = new GameState();
        MoveLogic movelogic = new MoveLogic();
        Pane pane = new Pane();
        EnemyCommand enemycommand = new EnemyCommand(pane, movelogic);
        
        //1.1 graphics

        Text titleText = new Text("Score: 0");

        pane.setStyle("-fx-background-color: black;");
        pane.setPrefSize(WIDTH, LENGTH);
        Text gameOverText = new Text((WIDTH / 2) - 50, 100, "");
        gameOverText.setFill(javafx.scene.paint.Color.HOTPINK);

        HBox bottomBox = new HBox();
        Text helpText = new Text("Arrow keys move, Spacebar fires!  * ");
        bottomBox.getChildren().add(helpText);
        Text damageText = new Text(" Hull damage: 0 / 3");
        bottomBox.getChildren().add(damageText);

        VBox verticalBox = new VBox();
        verticalBox.getChildren().add(titleText);
        verticalBox.getChildren().add(pane);
        verticalBox.getChildren().add(bottomBox);
        
        //1.2 particle simulation and canvas to draw it on

        ParticleSimulation particles = new ParticleSimulation();

        Canvas particleBackground = new Canvas(WIDTH, LENGTH);
        GraphicsContext particleDraw = particleBackground.getGraphicsContext2D();
        pane.getChildren().add(particleBackground);
        pane.getChildren().add(gameOverText);

        //1.3 player ship
        
        Craft playerShip = Craft.createPlayerShip();
        playerShip.getForm().setFill(javafx.scene.paint.Color.AZURE);
        pane.getChildren().add(playerShip.getForm());

        //1.4 create lists for other objects and create starting enemies
        
        ArrayList<Craft> otherObjects = new ArrayList<>();
        ArrayList<Pew> pews = new ArrayList<>();
        ArrayList<Circle> explosions = new ArrayList<>();
        ArrayList<Craft> asteroids = new ArrayList<>();

        otherObjects.addAll(enemycommand.initialize());
        asteroids.addAll(enemycommand.initializeAsteroids());

        //1.5 set Scene
        Scene scene = new Scene(verticalBox);

        // 1.6 HashMap for getting keyboard inputs
        Map<KeyCode, Boolean> keysPressed = new HashMap<>();
        scene.setOnKeyPressed(event -> {
            keysPressed.put(event.getCode(), Boolean.TRUE);
        });
        scene.setOnKeyReleased(event -> {
            keysPressed.put(event.getCode(), Boolean.FALSE);
        });

        //2 main game loop
        
        new AnimationTimer() {

            private long previousUpdate;

            @Override
            public void handle(long rightAboutNow) {

                //2.1 draw particles
                particleDraw.setFill(Color.BLACK);
                particleDraw.fillRect(0, 0, WIDTH, LENGTH);
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < LENGTH; y++) {
                        Particle particle = particles.getContent(x, y);
                        if (particle.equals(Particle.EMPTY)) {
                            continue;
                        }
                        if (particle.equals(Particle.LIGHT)) {
                            particleDraw.setFill(Color.LIGHTGRAY);
                            particleDraw.fillRect(x, y, 1, 1);
                            continue;
                        }
                        if (particle.equals(Particle.HEAVY)) {
                            particleDraw.setFill(Color.DARKGREY);
                            particleDraw.fillRect(x, y, 1, 1);
                            continue;
                        }
                        if (particle.equals(Particle.PLAYER)) {
                            particleDraw.setFill(Color.AZURE);
                            particleDraw.fillRect(x, y, 1, 1);
                            continue;
                        }
                        if (particle.equals(Particle.ICE)) {
                            particleDraw.setFill(Color.FLORALWHITE);
                            particleDraw.fillRect(x, y, 1, 1);
                            continue;
                        }
                        if (particle.equals(Particle.EXPLOSION)) {
                            //at the moment these are never on screen long enough to be seen?
                            particleDraw.setFill(Color.ORANGE);
                            particleDraw.fillRect(x, y, 1, 1);
                            continue;
                        }
                        if (particle.equals(Particle.BURNT)) {
                            particleDraw.setFill(Color.BLACK);
                            particleDraw.fillRect(x, y, 1, 1);
                        }
                        if (particle.equals(Particle.WATER)) {
                            particleDraw.setFill(Color.AQUA);
                            particleDraw.fillRect(x, y, 1, 1);
                        }
                    }
                }

                //2.2 Execute keyboard commands unless gameover
                //
                // (gameover control is here so the particle simulation keeps running and other objects move
                // even though the player has been destroyed
                
                if (keysPressed.getOrDefault(KeyCode.LEFT, Boolean.FALSE) && !game.isGameOver()) {
                    playerShip.moveLeft(particles, game.isGameOver());
                }
                if (keysPressed.getOrDefault(KeyCode.RIGHT, Boolean.FALSE && !game.isGameOver())) {
                    playerShip.moveRight(particles, game.isGameOver());
                }
                if (keysPressed.getOrDefault(KeyCode.UP, Boolean.FALSE) && !game.isGameOver()) {
                    playerShip.moveUp();
                }
                if (keysPressed.getOrDefault(KeyCode.DOWN, Boolean.FALSE && !game.isGameOver())) {
                    playerShip.moveDown(particles, game.isGameOver());
                }
                if (keysPressed.getOrDefault(KeyCode.SPACE, Boolean.FALSE) && !game.isGameOver()) {
                    if (!playerHasFired && !game.isGameOver()) {
                        Pew pew = new Pew(playerShip.getX(), playerShip.getY() - 20, true);
                        pews.add(pew);
                        pane.getChildren().add(pew.getForm());
                        playerHasFired = true;
                    }
                }

                //2.3 update pews
                pews.forEach(pew -> {
                    pew.move();
                    List<Craft> hits = otherObjects.stream()
                            .filter(craft -> pew.collide(craft, game, enemycommand))
                            // pew.collide destroys pews that hit something
                            .collect(Collectors.toList());
                    hits.stream().forEach(hit -> {
                        otherObjects.get(otherObjects.indexOf(hit)).damage(game, enemycommand);
                        //damage method in Craft handles damage and destruction of Crafts
                    });
                    List<Craft> asteroidHits = asteroids.stream()
                            .filter(asteroid -> pew.collide(asteroid, game, enemycommand))
                            .collect(Collectors.toList());
                    asteroidHits.stream().forEach(hit -> {
                        asteroids.get(asteroids.indexOf(hit)).damage(game, enemycommand);
                    });
                    if (pew.collide(playerShip, game, enemycommand)) {
                        explosions.add(drawExplosion(playerShip, pane, explosions));
                        playerShip.damage(game, enemycommand);
                    }
                    if (pew.getForm().getTranslateY() > LENGTH) {
                        pew.destroy();
                        // don't actually know if this is necessary but oh well
                    }
                    if (!particles.getContent(pew.getX(),pew.getY()).equals(Particle.EMPTY)) {
                        pew.destroy();
                        particles.vaporize(pew.getX(), pew.getY());
                        // thought this would slow everything down a lot more tbh
                }
                });
                
                //2.4 update asteroids

                asteroids.forEach(asteroid -> {
                    asteroid.move(particles, playerShip, game.isGameOver());
                    List<Craft> hits = otherObjects.stream()
                            .filter(craft -> asteroid.collide(craft))
                            .collect(Collectors.toList());
                    hits.stream().forEach(hit -> {
                        otherObjects.get(otherObjects.indexOf(hit)).damage(game, enemycommand);
                    });
                    if (asteroid.collide(playerShip) && !playerShipBlownUp) {
                        explosions.add(drawExplosion(playerShip, pane, explosions));
                        playerShip.damage(game, enemycommand);
                        asteroid.damage(game, enemycommand);
                    }
                    if (particles.getContent(asteroid.getX(), asteroid.getY() - 5).equals(Particle.ICE)) {
                        asteroid.damage(game, enemycommand);
                    }

                    // blow up dead asteroids
                    if (!asteroid.isAlive()) {
                        explosions.add(drawExplosion(asteroid, pane, explosions));
                        int area = (int) asteroid.getForm().computeAreaInScreen();
                        int water = random.nextInt(area / 4);
                        particles.createDebris((int) asteroid.getForm().getTranslateX(), (int) asteroid.getForm().getTranslateY(), area - water, asteroid.getDebrisType());
                        particles.createDebris(asteroid.getX(), asteroid.getY(), water, Particle.WATER);
                    }
                    // delete off-screen asteroids, remembering to inform EnemyCommand
                    if (asteroid.getX() > WIDTH + 100) {
                        asteroid.setDead();
                        enemycommand.scratchAsteroid();
                    }
                });

                //2.5 update enemy ships
                
                otherObjects.forEach(thing -> {
                    thing.move(particles, playerShip, game.isGameOver());
                    // specifically, get and execute movement orders for each Craft
                    if (thing.collide(playerShip)) {
                        thing.damage(game, enemycommand);
                        playerShip.damage(game, enemycommand);
                        if (thing.isAlive() && playerShip.isAlive()) {
                            thing.setY(thing.getY() - 20);
                            playerShip.setY(playerShip.getY() + 20);
                        }
                    }
                    // enemy ships don't collide with each other - yet!

                    if (thing.getType().equals(CraftType.ENEMYINTERCEPTOR)) {
                        if (rightAboutNow - thing.getCreated() > SECOND && !game.isGameOver()) {
                            if (thing.getX() - playerShip.getX() < 20 && thing.getX() - playerShip.getX() > -20) {
                                // if the ship is an interceptor and the player's ship is close enough on the X axis
                                Pew pew = thing.fire();
                                pews.add(pew);
                                pane.getChildren().add(pew.getForm());
                                thing.setCreated(rightAboutNow);

                            }
                        }
                    }
                    if (thing.getType().equals(CraftType.ENEMYFIGHTER)) {
                        if (rightAboutNow - thing.getCreated() > 2 * SECOND && playerShipInZone(playerShip.getX(), thing.getZone()) && !game.isGameOver()) {
                            // fighters patrol their designated zone and fire if the player ship is in it
                            Pew pew = thing.fire();
                            pews.add(pew);
                            pane.getChildren().add(pew.getForm());
                            thing.setCreated(rightAboutNow);
                        }
                    }

                    //2.5.1 blow shit up
                    if (!thing.isAlive()) {
                        explosions.add(drawExplosion(thing, pane, explosions));
                        int area = (int) thing.getForm().computeAreaInScreen();
                        particles.createDebris((int) thing.getForm().getTranslateX(), (int) thing.getForm().getTranslateY(), area, thing.getDebrisType());
                    }
                });
                
                //2.6 blow up player ship if necessary

                if (!playerShip.isAlive() && !playerShipBlownUp) {
                    explosions.add(drawExplosion(playerShip, pane, explosions));
                    int area = (int) playerShip.getForm().computeAreaInScreen();
                    particles.createDebris(playerShip.getX(), playerShip.getY(), area, playerShip.getDebrisType());
                    pane.getChildren().remove(playerShip.getForm());
                    playerShipBlownUp = true;
                }

                //2.7 update score
                game.scoreTimer(rightAboutNow);

                //2.8 update display texts
                damageText.setText("Hull damage: " + (3 - playerShip.getDamage()) + "/3");
                titleText.setText("Points: " + game.getScore());

                //2.9 delete destroyed objects
                otherObjects.stream()
                        .filter(Craft -> !Craft.isAlive())
                        .forEach(Craft -> pane.getChildren().remove(Craft.getForm()));
                otherObjects.removeAll(otherObjects.stream()
                        .filter(Craft -> !Craft.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getForm()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                pews.stream()
                        .filter(Pew -> !Pew.isAlive())
                        .forEach(Pew -> pane.getChildren().remove(Pew.getForm()));
                pews.removeAll(pews.stream()
                        .filter(Pew -> !Pew.isAlive())
                        .collect(Collectors.toList()));
                
                //2.10 check for game over
                if (game.isGameOver()) {
                    gameOverText.setText("Game over!");

                }
                
                //2.11 get more enemies

                if (rightAboutNow - previousUpdate > SECOND * 2) {
                    otherObjects.addAll(enemycommand.update(rightAboutNow, particles.getHighestParticle()));
                    asteroids.addAll(enemycommand.updateAsteroids(rightAboutNow, particles.getHighestParticle()));
                }

            }
        }.start();

        //3 animate particles
        new AnimationTimer() {
            private long prev;

            @Override
            public void handle(long now) {
                if (now - prev < 1000000) {
                    return;
                }

                particles.update(now);
                this.prev = now;
            }
        }.start();

        //4.0 one-third-second maintenance: clean up explosions, allow player to fire again
        new AnimationTimer() {
            private long previousUpdate;

            @Override
            public void handle(long rightaboutnow) {
                if (rightaboutnow - previousUpdate < SECOND / 3) {
                    return;
                }

                if (!explosions.isEmpty()) {
                    explosions.stream()
                            .forEach(Explosion -> pane.getChildren().remove(Explosion));
                    explosions.clear();
                }

                this.previousUpdate = rightaboutnow;
                playerHasFired = false;
            }
        }.start();

        //5.0 Set scene and show
        window.setTitle("Starfighter!");
        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    //method for drawing an explosion, where the size of the explosion is relative to the size of the object exploding

    public Circle drawExplosion(Craft thing, Pane pane, ArrayList<Circle> explosions) {
        Point point = new Point(thing.getX(), thing.getY());
        if (point.equals(previousExplosion)) {
            return null;
        }
        Circle explosion = new Circle();
        int area = (int) thing.getForm().computeAreaInScreen();
        double radius = Math.sqrt(area / Math.PI);
        explosion.setCenterX(thing.getX());
        explosion.setCenterY(thing.getY());
        explosion.setRadius(radius * 1.5);
        explosion.setFill(Color.DARKORANGE);
        pane.getChildren().add(explosion);
        explosions.add(explosion);
        previousExplosion = point;

        return explosion;
    }
    
    //method for checking whether the player ship is in one of the three defined zones of the game area
    
    public boolean playerShipInZone(int playerShipX, int zone) {
        if (zone == 1) {
            if (playerShipX <= FIRSTZONE) {
                return true;
            }
        } else if (zone == 2) {
            if (playerShipX <= SECONDZONE && playerShipX > FIRSTZONE) {
                return true;
            }
        } else if (zone == 3) {
            if (playerShipX > SECONDZONE) {
                return true;
            }
        }
        return false;
    }
}
