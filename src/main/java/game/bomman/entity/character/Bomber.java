package game.bomman.entity.character;

import game.bomman.Game;
import game.bomman.component.DashboardHandler;
import game.bomman.component.InteractionHandler;
import game.bomman.component.SoundPlayer;
import game.bomman.entity.Entity;
import game.bomman.entity.character.enemy.Enemy;
import game.bomman.entity.immobileEntity.Bomb;
import game.bomman.entity.immobileEntity.Flame;
import game.bomman.entity.immobileEntity.Portal;
import game.bomman.map.Cell;
import game.bomman.map.Map;
import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.util.Stack;

public class Bomber extends Character {
    public static final double WIDTH = 42;
    public static final double HEIGHT = 48;
    private static final int N_SPRITES_PER_DIRECTION = 4;
    private static final double LEVEL_UP_SPRITE_DURATION = 0.4f;
    private static final int N_LEVEL_UP_SPRITES = 4;
    private static final double DYING_SPRITE_DURATION = 0.12;
    private static final int N_DYING_SPRITES = 11;
    private double levelUpTimer = 0;
    private int levelUpFrameIndex = 0;
    private double dyingTimer = 0;
    private int dyingFrameIndex = 0;
    private int padding = 0;
    private double respawnWaitTimer = 4.0;
    private double detonatorTimer = 30.0;
    // indicates if bomber is waiting to respawn
    private boolean waiting = true;
    private boolean detonator = false;
    private boolean isMoving = false;
    private boolean insidePortal = false;
    private static final Image bomberWalking;
    private static final Image bomberStanding;
    private static final Image effectedWalking;
    private static final Image effectedStanding;
    private static final Image bomberDying;
    private static final Image bomberLevelUp;
    private static final Image detonatorStanding;
    private static final Image detonatorWalking;
    private int numOfBombs;
    private int flameLength;
    private final Stack<String> commandStack = new Stack<>();

    public Bomber(Map map) {
        this.map = map;
        this.speed = 180;
        this.numOfLives = 3;
        this.numOfBombs = 1;
        this.flameLength = 1;
        gc.drawImage(bomberStanding, 0, 0, WIDTH, HEIGHT, SIDE + 3.0f, SIDE, WIDTH, HEIGHT);
        initHitBox(SIDE + 3.0f, SIDE, WIDTH, HEIGHT);
    }

    static {
        try {
            bomberStanding = loadImage(IMAGES_PATH + "/player/idle.png") ;
            bomberWalking = loadImage(IMAGES_PATH + "/player/walking.png");
            effectedStanding = loadImage(IMAGES_PATH + "/player/idle_tran.png") ;
            effectedWalking = loadImage(IMAGES_PATH + "/player/walking_tran.png");
            bomberDying = loadImage(IMAGES_PATH + "/player/die@11.png");
            bomberLevelUp = loadImage(IMAGES_PATH + "/player/white@4.png");
            detonatorStanding = loadImage(IMAGES_PATH + "/player/detonatorStanding.png");
            detonatorWalking = loadImage(IMAGES_PATH + "/player/detonatorWalking.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isWaiting() { return waiting; }

    public boolean isDetonator() { return detonator; }

    public void setDetonator() {
        detonator = true;
        detonatorTimer = 30.0;
    }

    public int getFlameLength() { return flameLength; }

    public void increaseFlameLength() { ++flameLength; }

    public void increaseNumOfBombs() { ++numOfBombs; }

    public void increaseSpeed() { speed += 60; }

    public void increaseNumOfLives() { ++numOfLives; }

    public void getIntoPortal() { insidePortal = true; }

    @Override
    public void update(double elapsedTime) throws FileNotFoundException {

        if (!isAlive) {
            dyingTimer += elapsedTime;
            dying();
            return;
        }

        if (brickPassingTimer > 0) {
            brickPassingTimer -= elapsedTime;
        } else {
            brickPassingTimer = 0;
            brickPassing = false;
        }

        if (respawnWaitTimer > 0) {
            respawnWaitTimer -= elapsedTime;
        } else {
            respawnWaitTimer = 0;
            waiting = false;
        }

        if (detonatorTimer > 0) {
            detonatorTimer -= elapsedTime;
        } else {
            detonatorTimer = 0;
            detonator = false;
        }

        Cell thisCell = map.getCell(getPosOnMapX(), getPosOnMapY());
        /// Handle interaction between Bomber and other entities.
        InteractionHandler.handleInteraction(this, thisCell);

        if (insidePortal) {
            levelUpTimer += elapsedTime;
            levelUp();
            return;
        }

        if (commandStack.empty()) {
            isMoving = false;
            return;
        }
        isMoving = true;

        /// Update walking sprite.
        timer += elapsedTime;
        if (timer >= speed / 1200) {
            timer = 0;
            ++frameIndex;
            if (frameIndex == N_SPRITES_PER_DIRECTION) {
                frameIndex = 0;
            }
        }

        /// Update bomber's position.
        updatePosition(elapsedTime);
    }

    private void updatePosition(double elapsedTime) {
        int positionOnMapX = getPosOnMapX();
        int positionOnMapY = getPosOnMapY();

        Cell currentCell = map.getCell(positionOnMapX, positionOnMapY);
        double cellMinX = currentCell.getHitBox().getMinX();
        double cellMinY = currentCell.getHitBox().getMinY();

        double currentX = hitBox.getMinX();
        double currentY = hitBox.getMinY();

        String command = commandStack.peek();
        boolean isBuffering = (command.charAt(0) == '1');

        switch (command.charAt(1)) {
            case 'u' -> {
                facingDirectionIndex = 0;
                padding = 0;

                Cell aheadCell = getAheadCell();
                boolean isBlocked = aheadCell.isBlocking(this);

                if (!isBlocked && !isBuffering) {
                    if (map.getCell(positionOnMapX + 1, positionOnMapY - 1).isBlocking(this)
                            && currentX > cellMinX + 3) {
                        commandStack.add("1left");
                        //System.out.println("Left buffered.");
                        break;
                    }

                    if (map.getCell(positionOnMapX - 1, positionOnMapY - 1).isBlocking(this)
                            && currentX < cellMinX + 3) {
                        commandStack.add("1right");
                        //System.out.println("Right buffered.");
                        break;
                    }
                }

                hitBox.setMinY(currentY - speed * elapsedTime);

                /// Character blocked.
                if (isBlocked || isBuffering || currentY > cellMinY) {
                    if (hitBox.getMinY() < cellMinY) {
                        hitBox.setMinY(cellMinY);
                    }
                }
                if (isBuffering && currentY <= cellMinY) {
                    //System.out.println("Up buffer removed.");
                    commandStack.pop();
                }
            }
            case 'd' -> {
                facingDirectionIndex = 2;
                padding = N_SPRITES_PER_DIRECTION * 2;

                Cell aheadCell = getAheadCell();
                boolean isBlocked = aheadCell.isBlocking(this);

                if (!isBlocked && !isBuffering) {
                    if (map.getCell(positionOnMapX + 1, positionOnMapY + 1).isBlocking(this)
                            && currentX > cellMinX + 3) {
                        commandStack.add("1left");
                        //System.out.println("Left buffered.");
                        break;
                    }
                    if (map.getCell(positionOnMapX - 1, positionOnMapY + 1).isBlocking(this)
                            && currentX < cellMinX + 3) {
                        commandStack.add("1right");
                        //System.out.println("Right buffered.");
                        break;
                    }
                }

                hitBox.setMinY(currentY + speed * elapsedTime);

                /// Character blocked.
                if (isBlocked || isBuffering || currentY < cellMinY) {
                    if (hitBox.getMinY() > cellMinY) {
                        hitBox.setMinY(cellMinY);
                    }
                }
                if (isBuffering && currentY >= cellMinY) {
                    //System.out.println("Down buffer removed.");
                    commandStack.pop();
                }
            }
            case 'l' -> {
                facingDirectionIndex = 3;
                padding = N_SPRITES_PER_DIRECTION * 3;

                Cell aheadCell = getAheadCell();
                boolean isBlocked = aheadCell.isBlocking(this);

                if (!isBlocked && !isBuffering) {
                    if (map.getCell(positionOnMapX - 1, positionOnMapY - 1).isBlocking(this)
                            && currentY < cellMinY) {
                        commandStack.add("1down");
                        //System.out.println("Down buffered.");
                        break;
                    }
                    if (map.getCell(positionOnMapX - 1, positionOnMapY + 1).isBlocking(this)
                            && currentY > cellMinY) {
                        commandStack.add("1up");
                        break;
                    }
                }

                hitBox.setMinX(currentX - speed * elapsedTime);

                /// Character blocked.
                if (isBlocked || isBuffering || currentX > cellMinX + 3) {
                    //System.out.println(aheadCell.getRawConfig());
                    if (hitBox.getMinX() < cellMinX + 3) {
                        hitBox.setMinX(cellMinX + 3);
                    }
                }
                if (isBuffering && currentX <= cellMinX + 3) {
                    //System.out.println("Left buffer removed.");
                    commandStack.pop();
                }
            }
            case 'r' -> {
                facingDirectionIndex = 1;
                padding = N_SPRITES_PER_DIRECTION;

                Cell aheadCell = getAheadCell();
                boolean isBlocked = aheadCell.isBlocking(this);

                if (!isBlocked && !isBuffering) {
                    if (map.getCell(positionOnMapX + 1, positionOnMapY - 1).isBlocking(this)
                            && currentY < cellMinY) {
                        commandStack.add("1down");
                        //System.out.println("Down buffered.");
                        break;
                    }
                    if (map.getCell(positionOnMapX + 1, positionOnMapY + 1).isBlocking(this)
                            && currentY > cellMinY) {
                        commandStack.add("1up");
                        //System.out.println("Up buffered.");
                        break;
                    }
                }

                hitBox.setMinX(currentX + speed * elapsedTime);

                /// Character blocked.
                if (isBlocked || isBuffering || currentX < cellMinX + 3) {
                    //System.out.println(aheadCell.getRawConfig());
                    if (hitBox.getMinX() > cellMinX + 3) {
                        hitBox.setMinX(cellMinX + 3);
                    }
                }
                if (isBuffering && currentX >= cellMinX + 3) {
                    //System.out.println("Right buffer removed.");
                    commandStack.pop();
                }
            }
        }
    }

    @Override
    public void draw() {

        if (!isAlive) {
            gc.drawImage(bomberDying,
                    53 * dyingFrameIndex, 0, 53, HEIGHT,
                    hitBox.getMinX(), hitBox.getMinY(), 53, HEIGHT);
            return;
        }

        if (insidePortal) {
            gc.drawImage(bomberLevelUp,
                    WIDTH * levelUpFrameIndex, 0, WIDTH, HEIGHT,
                    hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
            return;
        }

        if (isMoving) {
            if (brickPassing) {
                gc.drawImage(effectedWalking,
                        (frameIndex + padding) * WIDTH, 0, WIDTH, HEIGHT,
                        hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
                return;
            }
            if (detonator) {
                gc.drawImage(detonatorWalking,
                        (frameIndex + padding) * WIDTH, 0, WIDTH, HEIGHT,
                        hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
                return;
            }
            gc.drawImage(bomberWalking,
                    (frameIndex + padding) * WIDTH, 0, WIDTH, HEIGHT,
                    hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);

        } else {
            if (brickPassing) {
                gc.drawImage(effectedStanding,
                        WIDTH * facingDirectionIndex, 0, WIDTH, HEIGHT,
                        hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
                return;
            }
            if (detonator) {
                gc.drawImage(detonatorStanding,
                        WIDTH * facingDirectionIndex, 0, WIDTH, HEIGHT,
                        hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
                return;
            }
            gc.drawImage(bomberStanding,
                    WIDTH * facingDirectionIndex, 0, WIDTH, HEIGHT,
                    hitBox.getMinX(), hitBox.getMinY(), WIDTH, HEIGHT);
        }
    }

    private void levelUp() throws FileNotFoundException {
        if (levelUpTimer >= LEVEL_UP_SPRITE_DURATION) {
            levelUpTimer = 0;
            ++levelUpFrameIndex;
            if (levelUpFrameIndex == N_LEVEL_UP_SPRITES) {
                insidePortal = false;
                levelUpFrameIndex = 0;
                Game.levelUp();
            }
        }
    }

    private void dying() {
        if (dyingTimer >= DYING_SPRITE_DURATION) {
            dyingTimer = 0;
            ++dyingFrameIndex;
            if (dyingFrameIndex == N_DYING_SPRITES) {
                respawn();
            }
        }
    }

    @Override
    public void die() {
        super.die();
        isMoving = false;
        SoundPlayer.playDyingSound();
        while (!commandStack.isEmpty()) { commandStack.pop(); }
    }

    private void respawn() {
        --numOfLives;
        DashboardHandler.updateLifeCount();
        if (numOfLives <= 0) {
            Game.gameLost();
            return;
        }
        isAlive = true;
        dyingFrameIndex = 0;
        waiting = true;
        respawnWaitTimer = 4.0;
        brickPassing = false;
        detonator = false;
        resetPosition();
    }

    public void resetPosition() {
        hitBox.setMinX(SIDE);
        hitBox.setMinY(SIDE);
        isMoving = false;
        facingDirectionIndex = 2;
    }

    @Override
    public void interactWith(Entity other) {
        if (other instanceof Portal) {
            Cell thisCell = map.getCell(getPosOnMapX(), getPosOnMapY());
            if (Math.abs(hitBox.getCenterX() - thisCell.getHitBox().getCenterX()) <= 0.3
                    && Math.abs(hitBox.getCenterY() - thisCell.getHitBox().getCenterY()) <= 0.3) {
                Portal portal = (Portal) other;
                if (portal.isActivated()) {
                    this.getIntoPortal();
                }
            }
            return;
        }
        if (other instanceof Enemy || other instanceof Flame) {
            die();
        }
    }

    private void layNewBomb() {
        if (numOfBombs == 0) {
            return;
        }

        Cell thisCell = map.getCell(getPosOnMapX(), getPosOnMapY());

        Portal portal = InteractionHandler.getPortal();
        if (portal != null) {
            if (portal.getLoadingPositionX() == thisCell.getLoadingPositionX()
                    && portal.getLoadingPositionY() == thisCell.getLoadingPositionY()) {
                return;
            }
        }

        if (thisCell.getImmobileEntity() != null) {
            return;
        }

        --numOfBombs;

        Bomb newBomb = new Bomb(
                map, this,
                thisCell.getLoadingPositionX(),
                thisCell.getLoadingPositionY()
        );
        InteractionHandler.addImmobileEntity(newBomb);
    }

    @Override
    public void layingBomb() {
        layNewBomb();
    }

    public void detonate() {
        if (detonator) {
            Bomb firstBomb = InteractionHandler.getFirstBomb();
            if (firstBomb != null) {
                firstBomb.explode();
            }
        }
    }

    public void retakeBomb() {
        ++numOfBombs;
    }

    @Override
    public void moveDown() {
        if (commandStack.empty() || commandStack.peek().charAt(1) != 'd') {
            commandStack.push("0down");
        }
    }

    @Override
    public void moveLeft() {
        if (commandStack.empty() || commandStack.peek().charAt(1) != 'l') {
            commandStack.push("0left");
        }
    }

    @Override
    public void moveRight() {
        if (commandStack.empty() || commandStack.peek().charAt(1) != 'r') {
            commandStack.push("0right");
        }
    }

    @Override
    public void moveUp() {
        if (commandStack.empty() || commandStack.peek().charAt(1) != 'u') {
            commandStack.push("0up");
        }
    }

    @Override
    public void removeDown() {
        for (int i = commandStack.size() - 1; i >= 0; --i) {
            if (commandStack.get(i).equals("0down")) {
                commandStack.remove(i);
                break;
            }
        }
    }

    @Override
    public void removeLeft() {
        for (int i = commandStack.size() - 1; i >= 0; --i) {
            if (commandStack.get(i).equals("0left")) {
                commandStack.remove(i);
                break;
            }
        }
    }

    @Override
    public void removeRight() {
        for (int i = commandStack.size() - 1; i >= 0; --i) {
            if (commandStack.get(i).equals("0right")) {
                commandStack.remove(i);
                break;
            }
        }
    }

    @Override
    public void removeUp() {
        for (int i = commandStack.size() - 1; i >= 0; --i) {
            if (commandStack.get(i).equals("0up")) {
                commandStack.remove(i);
                break;
            }
        }
    }

    public int getNumOfLives() {
        return numOfLives;
    }
}
