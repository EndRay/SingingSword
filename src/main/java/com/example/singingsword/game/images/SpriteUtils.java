package com.example.singingsword.game.images;

import com.example.singingsword.GameController;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class SpriteUtils {
    public static final ImageDrawer swordSprite = new SimpleImageDrawer(getImageProvider("sword", 128, 128));
    public static final ImageDrawer backgroundSprite = new SimpleImageDrawer(getImageProvider("background", 960, 720));
    public static final ImageDrawer floorSprite = new SimpleImageDrawer(getImageProvider("floor", 960, 720));


    public static ImageDrawer getEnemySprite() {
        return new SimpleImageDrawer(getImageProvider("enemy", 128, 128));
    }

    public static ImageDrawer getFilledHeartSprite() {
        return new SimpleImageDrawer(getImageProvider("hp", 128, 128));
    }

    public static ImageDrawer getLostHeartSprite() {
        return new CombinedImageDrawer(List.of(
                getImageProvider("damaged_hp1/beating", 128, 128),
                getImageProvider("damaged_hp1/eye", 128, 128)
        ));
    }

    public static Image getImage(String name, int width, int height) {
        try {
            return new Image(Objects.requireNonNull(GameController.class.getResource("images/" + name)).toString(), width, height, true, false);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Image not found: " + name);
        }
    }

    private static StaticImageProvider getStaticImageProvider(String name, int width, int height) throws NoSuchFileException {
        try {
            return new StaticImageProvider(getImage(name + ".png", width, height));
        } catch (IllegalArgumentException e) {
            throw new NoSuchFileException("No static sprite \"" + name + "\"");
        }
    }

    private static AnimatedImageProvider getAnimatedImageProvider(String name, int width, int height) throws NoSuchFileException {
        List<Image> frames = new ArrayList<>();
        int i = 0;
        while (true) {
            try {
                frames.add(getImage(name + (name.endsWith("/") ? "" : "/") + i + ".png", width, height));
                i++;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        try (var info = new FileInputStream(Objects.requireNonNull(GameController.class.getResource("images/" + name + "/info.properties")).getFile())) {
            var properties = new Properties();
            properties.load(info);
            float period = Float.parseFloat(properties.getProperty("period"));
            List<Image> trueFrames = new ArrayList<>();
            for(int j = 0; j < frames.size(); j++){
                int count = Integer.parseInt(properties.getProperty("frame" + j, String.valueOf(1)));
                for(int k = 0; k < count; k++){
                    trueFrames.add(frames.get(j));
                }
            }
            return new AnimatedImageProvider(trueFrames, period);
        } catch (IllegalArgumentException e) {
            throw new NoSuchFileException("No animated sprite \"" + name + "\"");
        } catch (NullPointerException e) {
            throw new NoSuchFileException("No info.properties for animated sprite \"" + name + "\"");
        } catch (IOException e) {
            throw new RuntimeException("Error while reading info.properties for animated sprite \"" + name + "\"");
        }
    }

    public static ImageProvider getImageProvider(String name, int width, int height) {
        try {
            return getAnimatedImageProvider(name, width, height);
        } catch (NoSuchFileException e) {
            try {
                return getStaticImageProvider(name, width, height);
            } catch (NoSuchFileException e2) {
                throw new IllegalArgumentException("No such sprite \"" + name + "\"");
            }
        }
    }
}
