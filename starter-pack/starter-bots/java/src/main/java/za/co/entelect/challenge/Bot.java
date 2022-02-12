package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;
    //private GameState gameState;
    //private Car opponent;
    //private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_RIGHT);
        directionList.add(TURN_LEFT);
    }

    /* public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    } */

    public Command run(GameState gameState) {
        Car myCar = gameState.player;

        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState); // beberapa blok ke depan
        List<Object> nextBlocks = blocks.subList(0,1); // mendapatkan satu blok ke depan (masih dalam bentuk list)


        /* if (myCar.damage >= 5) {
            return new FixCommand();
        }
        if (blocks.contains(Terrain.MUD)) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        } */

        //Fix first if too damaged to move
        if(myCar.damage == 5) {
            return FIX;
        }

        if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        //Basic fix logic
        if(myCar.damage >= 5) {
            return FIX;
        }

        // Belok hanya untuk menghindari obstacle, selebihnya mengikuti current lane

        // Menghindari obstacle
        if (blocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
                if (myCar.position.lane == 1) {
                    return TURN_LEFT;
                } else if (myCar.position.lane == 4) {
                    return TURN_RIGHT;
                } else {
                    // blok di kanan current lane
                    List<Object> blocksInRightSideLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                    //blok di kiri current lane
                    List<Object> blocksInLeftSideLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);

                    int obsAtXLeft = 0;
                    int obsAtXRight = 0;
                    int i = 0;
                    int j = 0;
                    boolean foundLeft = false;
                    boolean foundRight = false;

                    // menghindari dgn belok ke lane yang memiliki powerups terdekat
                    while (i < blocksInLeftSideLane.size() && !foundLeft) {
                        if (blocksInLeftSideLane.contains(Terrain.MUD) || blocksInLeftSideLane.contains(Terrain.WALL)) {
                            obsAtXLeft = i;
                            foundLeft = true;
                        } else {
                            i ++;
                        }
                    }
                    while (j < blocksInRightSideLane.size() && !foundRight) {
                        if (blocksInRightSideLane.contains(Terrain.MUD) || blocksInLeftSideLane.contains(Terrain.WALL)) {
                            obsAtXRight = j;
                            foundRight = true;
                        } else {
                            j ++;
                        }
                    }
                    if (foundRight == false) {
                        return TURN_RIGHT;
                    } else if (foundLeft == false) {
                        return TURN_LEFT;
                    } else if (obsAtXLeft <= obsAtXRight) {
                        return TURN_RIGHT;
                    } else if (obsAtXLeft > obsAtXRight) {
                        return TURN_LEFT;
                    }
                }
            }
        }

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        return ACCELERATE;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

}
