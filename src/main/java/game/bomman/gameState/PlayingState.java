package game.bomman.gameState;

import game.bomman.entity.character.Bomber;
import game.bomman.inputHandler.MovingController;
import game.bomman.map.Map;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.FileNotFoundException;

public class PlayingState extends GameState {
    private Canvas characterCanvas;
    private Canvas bombCanvas;
    private Canvas itemCanvas;
    private Map gameMap;

    public PlayingState() {
//        root.getChildren().add(itemCanvas);
//        root.getChildren().add(bombCanvas);
        scene = new Scene(root);
        gameMap = new Map();
    }

    public Scene getScene() { return scene; }

    public void setUp() throws FileNotFoundException {
        /// Set up the game map.
        root.getChildren().add(gameMap.setUp());

        /// Set up the characters.
        characterCanvas = new Canvas(gameMap.getWidth(), gameMap.getHeight());
        root.getChildren().add(characterCanvas);
        characterCanvas.requestFocus();

        double[] bomberPosition  = gameMap.getCell(1, 1).getLoadingPosition();

        Bomber bomber = new Bomber(characterCanvas.getGraphicsContext2D(),
                                   bomberPosition[0], 
                                   bomberPosition[1]);
        MovingController controller = new MovingController(characterCanvas, bomber);
    }

    public void run() {
        MovingController.setUpForBomber();
//        new AnimationTimer() {
//            @Override
//            public void handle(long ) {
//
//            }
//        }.start();
    }
}
