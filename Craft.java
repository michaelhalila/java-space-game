package fallinginvaders;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 *
 * @author Michael Halila
 */
public class Craft {

    private Polygon form;
    private Point2D motion;
    private boolean alive;
    private int damageCapacity;
    private CraftType type;
    private long created;
    private Particle debrisType;
    private ArrayList<MoveOrder> moves;
    private boolean hasMoveOrders;
    private MoveLogic moveLogic;
    private int zone;

    public Craft() {
        this.alive = true;
        this.created = -1;
        this.hasMoveOrders = false;
    }

    public Craft(int x, int y, int damageCapacity) {
    }

    public static Craft createPlayerShip() {
        Craft playerShip = new Craft();
        playerShip.form = new Polygon(0, -10, 5, 10, -5, 10);
        playerShip.form.setTranslateX(FallingInvaders.WIDTH / 2);
        playerShip.form.setTranslateY(FallingInvaders.LENGTH - 200);
        playerShip.damageCapacity = 3;
        playerShip.motion = new Point2D(2, 2);
        playerShip.type = CraftType.PLAYERSHIP;
        playerShip.debrisType = Particle.PLAYER;
        playerShip.zone = -1;
        return playerShip;
    }

    public static Craft createEnemyShip(int x, int y, CraftType type, MoveLogic movelogic, int zone) {
        Craft enemyShip = new Craft();
        if (type.equals(CraftType.ENEMYFIGHTER)) {
            createEnemyFighter(enemyShip, zone);
            enemyShip.getForm().setFill(javafx.scene.paint.Color.ORANGERED);
        } else if (type.equals(CraftType.ENEMYINTERCEPTOR)) {
            createEnemyInterceptor(enemyShip);
            enemyShip.getForm().setFill(javafx.scene.paint.Color.CRIMSON);
        }
        enemyShip.form.setTranslateX(x);
        enemyShip.form.setTranslateY(y);
        enemyShip.moveLogic = movelogic;
        enemyShip.moves = new ArrayList<>();
        return enemyShip;
    }

    private static Craft createEnemyInterceptor(Craft interceptor) {
        interceptor.form = new Polygon(0, 10, 5, -10, -5, -10);
        interceptor.damageCapacity = 1;
        interceptor.type = CraftType.ENEMYINTERCEPTOR;
        interceptor.debrisType = Particle.HEAVY;
        interceptor.motion = new Point2D(1, 1);
        interceptor.zone = -1;
        return interceptor;
    }

    private static Craft createEnemyFighter(Craft fighter, int zone) {
        fighter.form = new Polygon(0, 10, 5, -10, -5, -10);
        fighter.damageCapacity = 1;
        fighter.type = CraftType.ENEMYFIGHTER;
        fighter.debrisType = Particle.HEAVY;
        fighter.motion = new Point2D(0.5, 1);
        fighter.zone = zone;
        return fighter;
    }

    public static Craft createAsteroid(int x, int y, MoveLogic movelogic) {
        Craft asteroid = new Craft();
        asteroid.form = new Polygon(0, 0, 25, 0, 30, 10, 25, 40, 0, 20);
        asteroid.getForm().setFill(javafx.scene.paint.Color.LIGHTGRAY);
        asteroid.type = CraftType.ASTEROID;
        asteroid.damageCapacity = 1;
        asteroid.alive = true;
        asteroid.debrisType = Particle.LIGHT;
        asteroid.motion = new Point2D(1,0);
        asteroid.setX(x);
        asteroid.setY(y);
        asteroid.getForm().setRotate(1.0);
        asteroid.moveLogic = movelogic;
        return asteroid;
    }

    public Polygon getForm() {
        return this.form;
    }

    public int getDamage() {
        return this.damageCapacity;
    }

    public int getZone() {
        return this.zone;
    }

    public int getX() {
        return (int) this.form.getTranslateX();
    }

    public int getY() {
        return (int) this.form.getTranslateY();
    }

    public void setX(int x) {
        this.form.setTranslateX(x);
    }

    public void setY(int y) {
        this.form.setTranslateY(y);
    }

    public boolean isAlive() {
        return this.alive;
    }
    
    public void setDead() {
        this.alive = false;
    }

    public CraftType getType() {
        return this.type;
    }

    public Particle getDebrisType() {
        return this.debrisType;
    }

    public boolean collide(Craft other) {
        Shape collisionZone = Shape.intersect(this.form, other.getForm());
        return collisionZone.getBoundsInLocal().getWidth() != -1;
    }

    public void damage(GameState game, EnemyCommand command) {
        this.damageCapacity--;
        if (this.damageCapacity < 1) {
            this.alive = false;
            if (this.type.equals(CraftType.PLAYERSHIP)) {
                game.gameOver();
            } else {
                scratchThis(command);
            }
        }
    }

    private void scratchThis(EnemyCommand command) {
        if (this.type.equals(CraftType.ENEMYFIGHTER)) {
            command.scratchFighter(this.zone);
            return;
        }
        if (this.type.equals(CraftType.ENEMYINTERCEPTOR)) {
            command.scratchInterceptor();
            return;
        }
        if (this.type.equals(CraftType.ASTEROID)) {
            command.scratchAsteroid();
            return;
        }

    }

    public void move(ParticleSimulation particles, Craft playerShip, boolean gameover) {
        
        if (this.type.equals(CraftType.ASTEROID)) {
            this.form.setTranslateX(this.form.getTranslateX() + this.motion.getX());
            this.form.setRotate(this.form.getRotate() + 5);
            return;
        }
        
        if (gameover) {
            moveUp();
            return;
        }

        if (!this.hasMoveOrders && !gameover) {
            this.moves = this.moveLogic.getMoveList(this.type, this.getX(), this.getY(), playerShip.getX(), playerShip.getY(), this.zone);
            this.hasMoveOrders = true;
        }
        
        if (this.moves == null) {
            return;
        }

        int moveOrdersExecuted = 0;

        while (moveOrdersExecuted < 2 && moveOrdersExecuted < moves.size()) {

            if (this.moves.get(moveOrdersExecuted).equals(MoveOrder.RIGHT)) {
                moveRight(particles, gameover);
                moveOrdersExecuted++;
            } else if (this.moves.get(moveOrdersExecuted).equals(MoveOrder.LEFT)) {
                moveLeft(particles, gameover);
                moveOrdersExecuted++;
            } else if (this.moves.get(moveOrdersExecuted).equals(MoveOrder.DOWN)) {
                moveDown(particles, gameover);
                moveOrdersExecuted++;
            } else if (this.moves.get(moveOrdersExecuted).equals(MoveOrder.UP)) {
                moveUp();
                moveOrdersExecuted++;
            }
        }

        while (moveOrdersExecuted > 0) {
            if (!moves.isEmpty()) {
                moves.remove(0);
            }
            moveOrdersExecuted--;
        }

        if (moves.isEmpty()) {
            this.hasMoveOrders = false;
        }
    }

    public void moveLeft(ParticleSimulation particles, boolean gameover) {
        if (particles.getContent(this.getX() - 20, this.getY()).equals(Particle.EMPTY)) {
            this.form.setTranslateX(this.form.getTranslateX() - this.motion.getX());
        }
    }

    public void moveRight(ParticleSimulation particles, boolean gameover) {
        if (particles.getContent(this.getX() + 20, this.getY()).equals(Particle.EMPTY)) {
            this.form.setTranslateX(this.form.getTranslateX() + this.motion.getX());
        }
    }

    public void moveUp() {
            this.form.setTranslateY(this.form.getTranslateY() - this.motion.getY());
        
    }

    public void moveDown(ParticleSimulation particles, boolean gameover) {
        if (particles.getContent(this.getX(), this.getY() + 20).equals(Particle.EMPTY)) {
            this.form.setTranslateY(this.form.getTranslateY() + this.motion.getY());
        }
    }

    public void enemyShipDeploy() {
        setMoveOrder(100,MoveOrder.DOWN);
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
        return this.created;
    }

    public Pew fire() {
        Pew pew = new Pew(this.getX(), this.getY() + 20, false);
        return pew;
    }
    
    public void setMoveOrder(int times, MoveOrder moveorder) {
        for (int i = 0; i < times; i++) {
            this.moves.add(moveorder);
        }
        this.hasMoveOrders = true;
    }

}
