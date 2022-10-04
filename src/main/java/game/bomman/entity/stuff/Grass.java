package game.bomman.entity.stuff;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileNotFoundException;

public class Grass extends Stuff {
    private static final Image image;

    static {
        try {
            image = loadImage(IMAGES_PATH + "/map/grass@2.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Grass(int row, int col) {
        super(row, col);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(image, 0, 0, side, side, positionX, positionY, side, side);
    }
}