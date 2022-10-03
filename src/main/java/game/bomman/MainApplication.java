package game.bomman;

import game.bomman.entity.Entity;
import game.bomman.entity.character.Bomber;
import game.bomman.entity.stuff.Brick;
import game.bomman.entity.stuff.Grass;
import game.bomman.entity.stuff.Stuff;
import game.bomman.entity.stuff.Wall;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static game.bomman.entity.character.Character.NOT_MOVING;

public class MainApplication extends Application {
    public void loadStaticMapSample(Stage stage) throws FileNotFoundException {
        FileInputStream maps = new FileInputStream("src/main/resources/game/bomman/assets/maps/map1.txt");
        Scanner mapScanner = new Scanner(maps);

        int height = mapScanner.nextInt();
        int width = mapScanner.nextInt();

        mapScanner.useDelimiter("\n");
        mapScanner.next();

        Stuff[][] entities = new Stuff[height][width];

        for (int row = 0; row < height; ++row) {
            String thisRow = mapScanner.next();
            for (int col = 0; col < width; ++col) {
                switch (thisRow.charAt(col)) {
                    case '#' -> entities[row][col] = new Wall(row, col);
                    case ' ' -> entities[row][col] = new Grass(row, col);
                    case '*' -> entities[row][col] = new Brick(row, col);
                }
            }
        }

        Canvas canvas = new Canvas(width * Stuff.side, height * Stuff.side);
        Group root = new Group(canvas);
        Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight());
        stage.setScene(scene);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // currently allow only one pressed key which is
        // about moving direction to be handled at a time.
        // wrap keyPressed around a single element array
        // to pass it in a lambda function
        final String[] keyPressed = {NOT_MOVING};
        KeyCode[] allowedKeys = {
                KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT
        };

        scene.setOnKeyPressed(event -> {
            if (Arrays.asList(allowedKeys).contains(event.getCode())
                    && keyPressed[0].equals(NOT_MOVING)) {
                keyPressed[0] = event.getCode().toString();
            }
        });

        scene.setOnKeyReleased(event -> {
            if (Arrays.asList(allowedKeys).contains(event.getCode())
                    && keyPressed[0].equals(event.getCode().toString())) {
                keyPressed[0] = NOT_MOVING;
            }
        });

        Bomber b = new Bomber(1, 1);

        final long startTime = System.nanoTime();
        // wrapped in a single-element array to
        // be able to pass into a lambda function
        final long[] lastNanoTime = {startTime};

        double speed = 100;

        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                double elapsedTime = (currentNanoTime - lastNanoTime[0]) / 1000000000.0;
                lastNanoTime[0] = currentNanoTime;
                double timeSinceStart = (currentNanoTime - startTime) / 1000000000.0;

                b.setVelocity(0,0);
                switch (keyPressed[0]) {
                    case "UP" -> b.addVelocity(0, -speed);
                    case "DOWN" -> b.addVelocity(0, speed);
                    case "LEFT" -> b.addVelocity(-speed, 0);
                    case "RIGHT" -> b.addVelocity(speed, 0);
                }
                b.update(elapsedTime, timeSinceStart, keyPressed[0], entities);

                // clears the canvas
                gc.clearRect(0 , 0, canvas.getWidth(), canvas.getHeight());

                for (Stuff[] entityRow: entities) {
                    for (Stuff entity: entityRow) {
                        entity.render(gc);
                    }
                }

                b.render(gc);
            }
        }.start();
    }

    void loadFullMap() {
        // Todo:
        // 1. create bounding box for each entities: checked
        // 2. add collision detection functionality: checked
        // 3. change setPosition condition
    }

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        stage.setTitle("Bomberman");
        stage.setResizable(false);

        loadStaticMapSample(stage);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}