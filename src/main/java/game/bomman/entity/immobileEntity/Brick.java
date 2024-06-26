package game.bomman.entity.immobileEntity;

import game.bomman.component.InteractionHandler;
import game.bomman.entity.Entity;
import game.bomman.entity.item.Item;
import game.bomman.map.Cell;
import game.bomman.map.Map;
import javafx.scene.image.Image;

import java.io.FileNotFoundException;

public class Brick extends ImmobileEntity {
    private static final Image image;
    private static final Image breakingImage;
    private static final Image sparkyImage;
    private double sparkyTimer = 0;
    private int sparkyFrameIndex = 1;
    private boolean sparked = false;
    private boolean isBreaking = false;
    private boolean broken = false;
    private static final double SPRITE_DURATION = 0.08f;
    private static final double SPARKING_DURATION = 0.5f;
    private static final int N_SPRITES = 7;

    static {
        try {
            image = loadImage(IMAGES_PATH + "/map/brick.png");
            breakingImage = loadImage(IMAGES_PATH + "/map/brick_explosion@7.png");
            sparkyImage = loadImage(IMAGES_PATH + "/map/brick_sparky@2.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Brick(Map map, double loadingPosX, double loadingPosY) {
        this.map = map;
        initHitBox(loadingPosX, loadingPosY, SIDE, SIDE);
        gc.drawImage(image, loadingPosX, loadingPosY, SIDE, SIDE);
    }

    private void sparking(double elapsedTime) {
        sparkyTimer += elapsedTime;
        if (sparkyTimer >= SPARKING_DURATION) {
            sparkyTimer = 0;
            sparkyFrameIndex = 1 - sparkyFrameIndex;
        }
    }

    private void beingBroken(double elapsedTime) {
        timer += elapsedTime;
        if (timer >= SPRITE_DURATION) {
            timer = 0;
            ++frameIndex;
            if (frameIndex == N_SPRITES) {
                isBreaking = false;
                broken = true;
            }
        }
    }

    public void spark() { sparked = true; }

    @Override
    public void explode() {
        isBreaking = true;
    }

    @Override
    public void interactWith(Entity other) {

    }

    @Override
    public void update(double elapsedTime) {
        int positionOnMapX = getPosOnMapX();
        int positionOnMapY = getPosOnMapY();

        if (isBreaking == true) {
            beingBroken(elapsedTime);
        }
        if (broken == true) {
            /// Remove the brick from the game.
            InteractionHandler.removeImmobileEntity(this);

            /// Change the shader grass below the brick into grass.
            Cell belowCell = map.getCell(positionOnMapX, positionOnMapY + 1);
            if (belowCell.getRawConfig() != '#') {
                belowCell.reloadGrass();
            }

            Cell thisCell = map.getCell(positionOnMapX, positionOnMapY);
            /// Reveal the portal if it is hidden under the broken brick.
            if (thisCell.getRawConfig() == 'x') {
                Portal portal = InteractionHandler.getPortal();

                /// Actually put the portal into the game.
                portal.appear();
                InteractionHandler.addImmobileEntity(portal);

                if (sparked == true) {
                    portal.activate();
                }
            }

            /// Reveal the item hidden under the broken brick if any.
            char rawConfig = thisCell.getRawConfig();
            if (rawConfig == 'f'
                    || rawConfig == 's'
                    || rawConfig == 'b'
                    || rawConfig == 'l'
                    || rawConfig == 'i'
                    || rawConfig == 'j'
                    || rawConfig == 'd') {
                Item.startCountdownTimer(InteractionHandler.getItem(thisCell));
            }
        }
        if (sparked == true) {
            sparking(elapsedTime);
        }
    }

    @Override
    public void draw() {
        double loadingPosX = hitBox.getMinX();
        double loadingPosY = hitBox.getMinY();

        if (isBreaking == false && broken == false) {
            if (sparked == true) {
                gc.drawImage(
                        sparkyImage, SIDE * sparkyFrameIndex, 0, SIDE, SIDE,
                        loadingPosX, loadingPosY, SIDE, SIDE
                );
            } else {
                gc.drawImage(image, loadingPosX, loadingPosY, SIDE, SIDE);
            }
            return;
        }
        if (isBreaking == true && broken == false) {
            gc.drawImage(breakingImage, SIDE * frameIndex, 0, SIDE, SIDE, loadingPosX, loadingPosY, SIDE, SIDE);
        }
    }
}
