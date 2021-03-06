package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;
import static java.lang.Math.max;

public class Bot {
    private List<Command> directionList = new ArrayList<>();

    private GameState gameState;
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    /* Konstruktor */
    public Bot(GameState gameState) {
        directionList.add(TURN_RIGHT);
        directionList.add(TURN_LEFT);
        this.gameState = gameState;
        List<Lane[]> map = gameState.lanes;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
    }

    /* Fungsi run yang dijalankan tiap ronde */
    public Command run() {
        int currentSpeed = myCar.speed;

        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        // list of block yang dapat dilihat (20 block pada map terlepas dari finish lane, dsb)

        // Mobil akan diperbaiki terlebih dahulu jika damagenya lebih besar dari 3
        if(myCar.damage >= 3) {
            return FIX;
        }

        int nearestObstacle = getNearestObstacle(blocks); // lokasi obstacle terdekat pada lane mobil berjalan

        // pakai boost jika tidak ada obstacle yg menghalangi di depannya
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && (nearestObstacle > 15 || nearestObstacle == 0)) {
            if (myCar.damage != 0){
                return FIX;
            }
            return BOOST;
        }

        /* sebelum speed dinaikkan ke state selanjutnya, akan dicek obstacle terdekat
        pada lane saat itu agar tidak mengenai obstacle */
        switch (currentSpeed) {
            case 0:
                return ACCELERATE;
            case 3:
                if (nearestObstacle > 5 || nearestObstacle == 0) {
                    return ACCELERATE;
                }
            case 5:
                if (nearestObstacle > 6 || nearestObstacle == 0) {
                    return ACCELERATE;
                }
            case 6:
                if (nearestObstacle > 8 || nearestObstacle == 0) {
                    return ACCELERATE;
                }
            case 8:
                if (nearestObstacle > 9 || nearestObstacle == 0) {
                    return ACCELERATE;
                }
        }

        // Belok hanya untuk menghindari obstacle, selebihnya mengikuti current lane

        // Menghindari obstacle
        if (getNearestObstacle(blocks) != 0) {
            Object landingBlock; // block tempat mendarat setelah memakai lizard
            if(currentSpeed < blocks.size()){
                landingBlock = blocks.get(currentSpeed);
            } else {
                landingBlock = Terrain.EMPTY;
            }

            boolean landingInObstacle = landingBlock == Terrain.MUD || landingBlock == Terrain.WALL;

            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && !landingInObstacle) {
                // cek agar saat landing setelah menggunakan lizard tdk mengenai obstacle
                return LIZARD;
            }

            /* Pada percabangan di bawah ini, variabel blocksInleftSideLane dan blocksInRightLane
               dideklarasikan lebih dari sekali agar tidak terjadi out of bounds saat pengaksesan elemen
             */

            if (currentSpeed >= getNearestObstacle(blocks)) { // jika akan menabrak obstacle
                if (myCar.position.lane == 4) {
                    // blok di kiri lane saat ini
                    List<Object> blocksInLeftSideLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);

                    // jika obstacle di lane saat ini lebih jauh daripada obstacle di lane kirinya
                    if (getNearestObstacle(blocks) >= getNearestObstacle(blocksInLeftSideLane) && (getNearestObstacle(blocksInLeftSideLane) != 0)){
                        return ACCELERATE;
                    }
                    return TURN_LEFT;
                } else if (myCar.position.lane == 1) {
                    // blok di kanan lane saat ini
                    List<Object> blocksInRightSideLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);

                    // jika obstacle di lane saat ini lebih jauh daripada obstacle di lane kanannya
                    if (getNearestObstacle(blocks) >= getNearestObstacle(blocksInRightSideLane) && (getNearestObstacle(blocksInRightSideLane) != 0)){
                        return ACCELERATE;
                    }
                    return TURN_RIGHT;
                } else { // jika di 2 lane tengah (lane 2 dan 3)
                    // blok di kanan current lane
                    List<Object> blocksInRightSideLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                    // blok di kiri current lane
                    List<Object> blocksInLeftSideLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);

                    // menghindari dgn belok ke lane yang memiliki obstaclel terjauh

                    // cek obstacle terdekat di lane kiri
                    int left = getNearestObstacle(blocksInLeftSideLane);

                    // cek obstacle terdekat di lane kanan
                    int right = getNearestObstacle(blocksInRightSideLane);

                    if (right == 0) { // tidak ada obstacle di lane kanan
                        return TURN_RIGHT;
                    } else if (left == 0) { // tidak ada obstavle di lane kiri
                        return TURN_LEFT;
                    } else if (left <= right) { // obstacle di lane kanan lebih jauh
                        return TURN_RIGHT;
                    } else { // obstacle di lane kiri lebih jauh
                        return TURN_LEFT;
                    }
                }
            }
        }

        // setelah coba menghindari obstacle, pakai powerups yg ada

        // ageresif (penggunaan powerups untuk menyarang lawan)

        int opponentLane = opponent.position.lane;
        int opponentBlock = opponent.position.block;
        Command TWEET = new TweetCommand(opponentLane, opponentBlock + opponent.speed + 4);

        // tweet
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups) && myCar.position.lane != opponent.position.lane) {
            // cek agar cybertruck tdk di-spawn di lane saat ini
            return TWEET;
        }

        // oil dan emp
        if (myCar.position.lane == opponent.position.lane) { // jika satu lane dengan lawan
            if (hasPowerUp(PowerUps.OIL, myCar.powerups) && myCar.position.block > opponent.position.block) {
                // jika posisi mobil lebih jauh dari lawan
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups) && myCar.position.block < opponent.position.block) {
                // jika posisi lawan lebih jauh
                return EMP;
            }
        }

        // default ACCELERATE
        return ACCELERATE;
    }

    /* Menerima masukan list of blocks dan mengembalikan indeks
       list tempat obstacle pertama kali ditemukan yang mengindikasikan obstacle terdekat
     */

    private int getNearestObstacle(List<Object> blocks) {
        int i = 0;
        boolean found = false;
        while (i < blocks.size() && !found) {
            if (blocks.get(i) == Terrain.MUD || blocks.get(i) == Terrain.WALL) {
                found = true;
            } else {
                i ++;
            }
        }
        if (!found) {
            return 0;
        } else {
            return i + 1;
        }
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed up to 20 blocks.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + 20; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    /* Menerima masukan powerup dan list of powerup yang tersedia,
       mengembalikan true jika powerup tersedia, false jika tidak
     */
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }
}