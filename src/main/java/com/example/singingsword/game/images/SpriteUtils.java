package com.example.singingsword.game.images;

import com.example.singingsword.GameController;
import javafx.scene.image.Image;

import java.util.Arrays;

public class SpriteUtils {
    public static final ImageDrawer swordSprite = new SimpleImageDrawer(new StaticImageProvider(getImage("images/sword.png", 128, 128)));
    public static final ImageDrawer backgroundSprite = new SimpleImageDrawer(new StaticImageProvider(getImage("images/background.png", 960, 720)));
    public static final ImageDrawer floorSprite = new SimpleImageDrawer(new StaticImageProvider(getImage("images/floor.png", 960, 720)));


    public static ImageDrawer getEnemySprite(){
        return new SimpleImageDrawer(getAnimateImageProvided(128, 128, 0.5f, "images/enemy1.png", "images/enemy2.png", "images/enemy3.png"));
    }

    public static Image getImage(String name, int width, int height){
        return new Image(GameController.class.getResource(name).toString(), width, height, true, false);
    }

    public static AnimatedImageProvider getAnimateImageProvided(int width, int height, float period, String... names){
        return new AnimatedImageProvider(Arrays.stream(names).map(name -> getImage(name, width, height)).toList(), period);
    }

}
