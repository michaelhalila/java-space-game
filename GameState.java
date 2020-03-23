package fallinginvaders;

/**
 *
 * @author Michael
 */
public class GameState {
    
    private boolean gameOver;
    private int score;
    private long scoringtimer;
    
    public GameState() {
        this.gameOver = false;
        this.score = 0;
    }
    
    public boolean isGameOver() {
        return this.gameOver;
    }
    
    public void gameOver() {
        this.gameOver = true;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public void score(int score) {
        this.score = this.score + score;
    }
    
    public void scoreTimer(long rightaboutnow) {
        if (rightaboutnow - this.scoringtimer > FallingInvaders.SECOND && !gameOver) {
            score(1);
            setScoringTimer(rightaboutnow);
        }
    }
    
    private void setScoringTimer(long rightaboutnow) {
        this.scoringtimer = rightaboutnow;
    }
    
}
