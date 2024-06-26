package game.bomman;

import game.bomman.component.DashboardHandler;
import game.bomman.component.GamePlayComponent;
import game.bomman.component.InteractionHandler;
import game.bomman.component.SoundPlayer;
import game.bomman.gameState.EndingState;
import game.bomman.gameState.PlayingState;
import game.bomman.gameState.scores.HighScore;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

public class Game {
    private static Stage stage;
    private static PlayingState playingState;
    public static final String[] levels;
    public static int currentMap = 0;
    // Thay đổi số map ở đây khi thêm file map mới:
    private static final int NUMBER_OF_MAPS = 2;
    private static boolean started = false;
    private static boolean paused = false;

    static {
        levels = new String[NUMBER_OF_MAPS];
        for (int i = 0; i < levels.length; ++i) {
            levels[i] = String.format("src/main/resources/game/bomman/assets/map%d.txt", i + 1);
        }
    }

    public static void init(Stage stage_) throws FileNotFoundException {
        stage = stage_;
        playingState = new PlayingState();
    }

    public static void levelUp() throws FileNotFoundException {
        ++currentMap;
        HighScore.addScore((long) playingState.getGameTimer());
        if (currentMap >= levels.length) {
            gameWin();
            return;
        }
        playingState.loadNextLevelMap();
        stage.setScene(playingState.getScene());
        stage.sizeToScene();
        setPosition(stage);
        SoundPlayer.playStageStartSound();
        playingState.resetGameTimer();
        DashboardHandler.clearDashboard();
    }

    public static void gameLost() {
        EndingState endingState = new EndingState(false);
        Game.endGame();
        stage.setScene(endingState.getScene());
        stage.sizeToScene();
        Game.setPosition(stage);
        SoundPlayer.playGameOverSound();
    }

    public static void gameWin() {
        EndingState endingState = new EndingState(true);
        endGame();
        stage.setScene(endingState.getScene());
        stage.sizeToScene();
        setPosition(stage);
        SoundPlayer.playWinSound();
    }

    public static void setPosition(Stage stage) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        double stageMinX = primaryScreenBounds.getMinX() + (primaryScreenBounds.getWidth() - stage.getWidth()) / 2;
        double stageMinY = primaryScreenBounds.getMinY() + (primaryScreenBounds.getHeight() - stage.getHeight()) / 2;

        stage.setX(stageMinX);
        stage.setY(stageMinY);
    }

    public static void run() throws FileNotFoundException {
        playingState.setUp();
        playingState.run();
        started = true;
        paused = false;

        stage.setTitle("Bomberman");
        stage.setResizable(false);
        stage.setScene(playingState.getScene());
        stage.sizeToScene();
        setPosition(stage);
        SoundPlayer.playStageStartSound();
        stage.show();
    }

    public static boolean hasStarted() {
        return started;
    }

    // Stop the game and clean up so that
    // a new game could be played if needed
    public static void endGame() {
        started = false;
        playingState.getPlayingStateTimer().stop();
        GamePlayComponent.clearEnemyList();
        InteractionHandler.clearEntityList();
        GamePlayComponent.clearBomber();
        HighScore.resetScore();
    }

    public static boolean hasPaused() { return paused; }

    public static void pause() {
        if (started && !paused) {
            paused = true;
            MainApplication.pausedStage.showAndWait();
        }
    }

    public static void load() {
        if (started && paused) {
            paused = false;
            MainApplication.pausedStage.close();
        }
    }

    public static PlayingState getPlayingState() {
        return playingState;
    }
}
