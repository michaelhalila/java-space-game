package fallinginvaders;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Michael
 */
public class MoveLogic {

    private Random rng;
    
    public MoveLogic() {
        this.rng = new Random();
    }

    public ArrayList<MoveOrder> getMoveList(CraftType craft, int x, int y, int playerShipX, int playerShipY, int zone) {
        ArrayList<MoveOrder> moves = new ArrayList<>();

        if (craft.equals(CraftType.ENEMYINTERCEPTOR)) {
            if (playerShipY - y > 0) {
                moves.add(MoveOrder.DOWN);                
            }
            if (playerShipY - y < 0) {
                moves.add(MoveOrder.UP);
            }
            
            if (playerShipX - x > 0) {
                moves.add(MoveOrder.RIGHT);
            }
            if (playerShipX - x < 0) {
                moves.add(MoveOrder.LEFT);
            }
        }
        
        if (craft.equals(CraftType.ENEMYFIGHTER)) {
            
            int thisZoneRightBound = -1;
            int thisZoneLeftBound = -1;
            
            if (zone == 1) {
                thisZoneRightBound = FallingInvaders.FIRSTZONE;
                thisZoneLeftBound = 0;
            } else if (zone == 2) {
                thisZoneRightBound = FallingInvaders.SECONDZONE;
                thisZoneLeftBound = FallingInvaders.FIRSTZONE;
            } else if (zone == 3) {
                thisZoneRightBound = FallingInvaders.WIDTH;
                thisZoneLeftBound = FallingInvaders.SECONDZONE;
            }
            
            if (y < 100) {
                for (int i = 0; i < 50; i++) {
                    moves.add(MoveOrder.DOWN);
                    return moves;
                }
            }
            
            if (x > thisZoneRightBound - 50) {
                for (int i = 0; i < 50; i++) {
                moves.add(MoveOrder.LEFT);
                }
            } else if (x < thisZoneLeftBound - 50) {
                for (int i = 0; i < 50; i++) {
                moves.add(MoveOrder.RIGHT);                }
            } else {
                if(!rng.nextBoolean()) {
                    for (int i = 0; i < 30; i++) {
                    moves.add(MoveOrder.LEFT);                        
                    }
                } else {
                    for (int i = 0; i < 30; i++) {
                    moves.add(MoveOrder.RIGHT);                        
                    }
                }
            }
            
        }
        
        return moves;
    }

}
