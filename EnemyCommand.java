package fallinginvaders;

import static fallinginvaders.FallingInvaders.FIRSTZONE;
import static fallinginvaders.FallingInvaders.WIDTH;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import java.util.Random;

/**
 *
 * @author Michael
 */
public class EnemyCommand {

    private Pane pane;
    private MoveLogic movelogic;
    private int FirstZoneFighters;
    private int SecondZoneFighters;
    private int ThirdZoneFighters;
    private int interceptors;
    private int asteroids;
    private Random rng;
    private long lastspawn;
    private long lastasteroid;

    public EnemyCommand(Pane pane, MoveLogic movelogic) {
        this.pane = pane;
        this.movelogic = movelogic;
        this.rng = new Random();
        this.lastspawn = 0;
        this.FirstZoneFighters = 0;
        this.SecondZoneFighters = 0;
        this.ThirdZoneFighters = 0;
        this.interceptors = 0;
        this.lastasteroid = 0;
    }

    public ArrayList<Craft> initialize() {
        ArrayList<Craft> objects = new ArrayList<>();
        objects.add(spawnInterceptor(200));
        objects.add(spawnInterceptor(FallingInvaders.FIRSTZONE));
        objects.add(spawnInterceptor(FallingInvaders.SECONDZONE));
        objects.add(spawnFighter(1));
        objects.add(spawnFighter(2));
        objects.add(spawnFighter(3));
        return objects;
    }

    public ArrayList<Craft> initializeAsteroids() {
        ArrayList<Craft> objects = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            objects.add(spawnAsteroid(FallingInvaders.LENGTH));
        }
        return objects;
    }

    public ArrayList<Craft> update(long rightaboutnow, int highestParticle) {
        ArrayList<Craft> objects = new ArrayList<>();
        if (interceptors < 2) {
            if (rng.nextBoolean()) {
                objects.add(spawnInterceptor(FallingInvaders.FIRSTZONE));
            } else {
                objects.add(spawnInterceptor(FallingInvaders.SECONDZONE));
            }
        }
        if (FirstZoneFighters < 1) {
            objects.add(spawnFighter(1));
        }
        if (SecondZoneFighters < 1) {
            objects.add(spawnFighter(2));
        }
        if (ThirdZoneFighters < 1) {
            objects.add(spawnFighter(3));
        }

        //if (objects.isEmpty() && rightaboutnow - lastspawn > FallingInvaders.SECOND * 2) {
        //    if (!rng.nextBoolean()) {
        //        objects.add(spawnInterceptor(FallingInvaders.FIRSTZONE));
        //    } else {
        //        objects.add(spawnInterceptor(FallingInvaders.SECONDZONE));
        //    }
        //    lastspawn = rightaboutnow;
        //}
        return objects;
    }

    public ArrayList<Craft> updateAsteroids(long rightaboutnow, int high) {
        ArrayList<Craft> objects = new ArrayList<>();
        if (asteroids < 20 && rightaboutnow - lastasteroid > FallingInvaders.SECOND && high > 200) {
            objects.add(spawnAsteroid(high));
            lastasteroid = rightaboutnow;
        }
        return objects;
    }

    public void scratchFighter(int zone) {
        if (zone == 1) {
            FirstZoneFighters--;
        } else if (zone == 2) {
            SecondZoneFighters--;
        } else if (zone == 3) {
            ThirdZoneFighters--;
        }
    }

    public void scratchInterceptor() {
        interceptors--;
    }

    public void scratchAsteroid() {
        asteroids--;
    }

    private Craft spawnInterceptor(int width) {
        Craft enemyInterceptor = Craft.createEnemyShip(width, 10, CraftType.ENEMYINTERCEPTOR, movelogic, 0);
        pane.getChildren().add(enemyInterceptor.getForm());
        enemyInterceptor.enemyShipDeploy();
        interceptors++;
        return enemyInterceptor;
    }

    private Craft spawnFighter(int zone) {
        Craft enemyFighter = Craft.createEnemyShip(WIDTH / 2 - 50, 10, CraftType.ENEMYFIGHTER, movelogic, zone);
        pane.getChildren().add(enemyFighter.getForm());
        if (zone == 1) {
            FirstZoneFighters++;
        }
        if (zone == 2) {
            SecondZoneFighters++;
        }
        if (zone == 3) {
            ThirdZoneFighters++;
        }
        return enemyFighter;
    }

    private Craft spawnAsteroid(int high) {
        int y = 200;
        if (high > 200) {
            y = rng.nextInt(high - 220) + 200;
            }
        Craft asteroid = Craft.createAsteroid(1, y, movelogic);
        pane.getChildren().add(asteroid.getForm());
        asteroids++;
        return asteroid;
    }

}
