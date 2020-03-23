package fallinginvaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author Michael
 */
public class ParticleSimulation {

    private Particle[][] table;
    private Random rng;
    private int highestParticle;
    private long lastFreeze;
    boolean freeze;

    public ParticleSimulation() {
        this.rng = new Random();
        this.freeze = false;
        this.highestParticle = FallingInvaders.LENGTH - 20;
        this.table = new Particle[FallingInvaders.WIDTH][FallingInvaders.LENGTH];
        for (int x = 0; x < FallingInvaders.WIDTH; x++) {
            for (int y = 0; y < FallingInvaders.LENGTH; y++) {
                table[x][y] = Particle.EMPTY;
            }
        }
    }

    public void update(long rightAboutNow) {

        if (rightAboutNow - lastFreeze > FallingInvaders.SECOND * 2) {
            freeze = true;
            lastFreeze = rightAboutNow;
        }

        for (int x = 0; x < this.table.length; x++) {
            for (int y = this.table[x].length - 1; y > 0; y--) {
                if (this.table[x][y].equals(Particle.EMPTY)) {
                    continue;
                }
                if (this.table[x][y].equals(Particle.EXPLOSION)) {
                    setContent(x,y,Particle.BURNT);
                }
                if (this.table[x][y].equals(Particle.LIGHT)) {
                    updateSolid(x, y, Particle.LIGHT);
                    continue;
                }
                if (this.table[x][y].equals(Particle.HEAVY)) {
                    updateSolid(x, y, Particle.HEAVY);
                    continue;
                }
                if (this.table[x][y].equals(Particle.PLAYER)) {
                    updateSolid(x, y, Particle.PLAYER);
                    continue;
                }
                if (this.table[x][y].equals(Particle.BURNT)) {
                    updateSolid(x,y, Particle.BURNT);
                    continue;
                }
                if (this.table[x][y].equals(Particle.ICE)) {
                    updateSolid(x, y, Particle.ICE);
                    continue;
                }
                if (this.table[x][y].equals(Particle.WATER)) {
                    updateLiquid(x, y, Particle.WATER);
                    if (freeze) {
                        freezeWater(x, y);
                    }
                }
            }
        }
        freeze = false;
    }

    public Particle getContent(int x, int y) {
        if (x < 0 || y < 0) {
            return Particle.METAL;
        }
        if (x >= table.length || y >= table[x].length) {
            return Particle.METAL;
        }
        return table[x][y];
    }

    public void setContent(int x, int y, Particle particle) {
        if (isBounds(x, y)) {
            this.table[x][y] = particle;
        }
    }

    public int getHighestParticle() {
        return this.highestParticle;
    }

    private boolean isBounds(int x, int y) {
        if (x < 0 || y < 0) {
            return false;
        }
        if (x >= table.length || y >= table[x].length) {
            return false;
        }
        return true;
    }

    private void updateSolid(int x, int y, Particle particle) {
        if (!isBounds(x, y + 1)) {
            return;
        }
        ArrayList<Point> emptiesBelow = findParticlesBelow(x, y, Particle.EMPTY);
        if (!Fall(x, y, emptiesBelow, particle, Particle.EMPTY)) {
            if (!particle.equals(Particle.ICE)) {
                Fall(x, y, findParticlesBelow(x, y, Particle.WATER), particle, Particle.WATER);

            }
        }

        if (emptiesBelow.isEmpty()) {
            // update highestParticle for asteroid generation
            if (highestParticle > y) {
                highestParticle = y;
            }
        }
    }

    private void updateLiquid(int x, int y, Particle particle) {
        if (!isBounds(x, y + 1)) {
            setContent(x, y, Particle.ICE);
            return;
        }
        ArrayList<Point> empties = findParticlesBelow(x, y, Particle.EMPTY);

        if (Fall(x, y, empties, particle, Particle.EMPTY)) {
            return;
        }
        ArrayList<Point> adjacents = findAdjacent(x, y, Particle.EMPTY);
        if (!adjacents.isEmpty()) {
            Collections.shuffle(adjacents);
            setContent(adjacents.get(0).getX(), adjacents.get(0).getY(), particle);
            setContent(x, y, Particle.EMPTY);
            return;
        }
    }

    private void freezeWater(int x, int y) {
        if (findAdjacent(x, y, Particle.EMPTY).isEmpty() && findParticlesBelow(x, y, Particle.EMPTY).isEmpty() && findParticlesBelow(x, y, Particle.WATER).isEmpty()) {
            setContent(x, y, Particle.ICE);
        }
    }
    
    public void vaporize(int x, int y) {
        // method for pews hitting solid particles
        Particle target = getContent(x,y);
        if (target.equals(Particle.ICE)) {
            setContent(x,y,Particle.WATER);
            return;
        }
        if (target.equals(Particle.LIGHT)) {
            setContent(x,y,Particle.EXPLOSION);
            return;
        }
        if (target.equals(Particle.HEAVY)) {
            setContent(x,y,Particle.EXPLOSION);
        }
    }

    public void createDebris(int x, int y, int area, Particle particle) {
        int square = (int) Math.sqrt(area);
        for (int across = x - (square / 2); across < x + (square / 2); across++) {
            for (int vert = y - (square / 2); vert < y + (square / 2); vert++) {
                setContent(across, vert, particle);
            }
        }
    }

    private ArrayList<Point> findParticlesBelow(int x, int y, Particle particle) {
        ArrayList<Point> pointsBelow = new ArrayList<>();

        if (getContent(x, y + 1).equals(particle) && isBounds(x, y + 1)) {
            pointsBelow.add(new Point(x, y + 1));
        }
        if (getContent(x - 1, y + 1).equals(particle) && isBounds(x - 1, y + 1)) {
            pointsBelow.add(new Point(x - 1, y + 1));
        }
        if (getContent(x + 1, y + 1).equals(particle) && isBounds(x + 1, y + 1)) {
            pointsBelow.add(new Point(x + 1, y + 1));
        }

        return pointsBelow;
    }

    private ArrayList<Point> findAdjacent(int x, int y, Particle particle) {
        //method for liquids to move horizontally
        ArrayList<Point> pointsAdjacent = new ArrayList<>();
        if (getContent(x + 1, y).equals(particle) && isBounds(x + 1, y)) {
            pointsAdjacent.add(new Point(x + 1, y));
        }
        if (getContent(x - 1, y).equals(particle) && isBounds(x - 1, y)) {
            pointsAdjacent.add(new Point(x - 1, y));
        }
        return pointsAdjacent;
    }

    private boolean Fall(int x, int y, ArrayList<Point> points, Particle falling, Particle newTop) {
        if (!points.isEmpty()) {
            Collections.shuffle(points);
            setContent(points.get(0).getX(), points.get(0).getY(), falling);
            setContent(x, y, newTop);
            return true;
        }
        return false;
    }

}
