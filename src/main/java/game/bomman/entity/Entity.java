package game.bomman.entity;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class Entity {
   public static final String IMAGES_PATH = "src/main/resources/game/bomman/assets/sprites";
   public static final double SIDE = 48;

   // Notes that X's coordinate here is the horizontal one
   // while Y's coordinate is the vertical one.
   protected double positionX;
   protected double positionY;
   protected BoundingBox hitBox;

   // create a load Image method to encapsulate
   // the functionality of loading images so that
   // we can change the particular implementation
   // later on if necessary
   public static Image loadImage(String path) throws FileNotFoundException {
      FileInputStream inputStream = new FileInputStream(path);
      Image image = new Image(inputStream);
      try {
         inputStream.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return image;
   }

   public double[] getPosition() {
      double x = hitBox.getCenterX();
      double y = hitBox.getCenterY();
      return new double[] {x, y};
   }

   public double[] getLoadingPosition() {
      return new double[] {hitBox.getMinX(), hitBox.getMinY()};
   }

   public boolean gotInto(Entity other) {
      return other.hitBox.contains(getPosition()[0], getPosition()[1]);
   }
}
