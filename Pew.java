package fallinginvaders;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 *
 * @author Michael
 */
public class Pew {

    private Polygon form;
    private boolean isFriendly;
    private Point2D motion;
    private boolean isAlive;

    public Pew(int x, int y, boolean isFriendly) {
        this.isFriendly = isFriendly;
        this.isAlive = true;
        this.form = new Polygon(2, -4, 2, 4, -2, 4, -2, -4);
        if (isFriendly) {
            this.motion = new Point2D(0, -2);
            this.form.setFill(javafx.scene.paint.Color.LIGHTGREEN);
            this.form.setTranslateX(x);
            this.form.setTranslateY(y);
        } else {
            this.motion = new Point2D(0, 2);
            this.form.setFill(javafx.scene.paint.Color.MAGENTA);
            this.form.setTranslateX(x);
            this.form.setTranslateY(y);
        }
    }

    public Polygon getForm() {
        return this.form;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void destroy() {
        isAlive = false;
    }
    
    public int getX() {
        return (int) this.form.getTranslateX();
    }
    
    public int getY() {
        return (int) this.form.getTranslateY();
    }

    public void move() {
        if (isFriendly) {
            this.form.setTranslateY(this.form.getTranslateY() + this.motion.getY());
        } else {
            this.form.setTranslateY(this.form.getTranslateY() + this.motion.getY());
        }
    }

    public boolean collide(Craft other, GameState game, EnemyCommand command) {
        boolean hit = false;
        Shape collisionZone = Shape.intersect(this.form, other.getForm());
        hit = collisionZone.getBoundsInLocal().getWidth() != -1;
        if (hit) {
            this.destroy();
            if (other.getType().equals(CraftType.ENEMYFIGHTER) && this.isFriendly) {
                game.score(20);
            } else if (other.getType().equals(CraftType.ENEMYINTERCEPTOR) && this.isFriendly) {
                game.score(40);
            } else if (other.getType().equals(CraftType.ASTEROID) && this.isFriendly) {
                game.score(10);
            }
        }
        return hit;
    }

}
