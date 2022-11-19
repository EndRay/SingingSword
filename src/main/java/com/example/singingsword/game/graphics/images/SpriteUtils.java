package com.example.singingsword.game.graphics.images;

import com.example.singingsword.GameController;
import com.example.singingsword.game.DamageCause;
import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.EnemyType;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class SpriteUtils {
    public static final ImageDrawer swordSprite = new TracedImageDrawer(getImageDrawer(128, 128, "sword"), 1/6f);
    public static final ImageDrawer backgroundSprite = getImageDrawer(960, 720, "background");
    public static final ImageDrawer floorSprite = getImageDrawer(960, 720, "floor");


    public static ImageDrawer getEnemySprite(Enemy enemy) {
        String name = enemy.getType() == EnemyType.REGULAR ? "enemy" : "infected_enemy";

        return switch (enemy.getType()) {
            case REGULAR, INFECTED -> switch (enemy.getArmorType()) {
                case NONE -> getImageDrawer(128, 128, name);
                case TOP -> getImageDrawer(128, 128, name, "hat");
                case BOTTOM -> getImageDrawer(128, 128, name, "bottom_hat");
                case BOTH -> getImageDrawer(128, 128, name, "bottom_hat", "hat");
                case STRONG_TOP -> getImageDrawer(128, 128, name, "strong_hat");
                case STRONG_BOTTOM -> getImageDrawer(128, 128, name, "strong_bottom_hat");
                case STRONG_BOTH -> getImageDrawer(128, 128, name, "strong_bottom_hat", "strong_hat");
            };
            case HEALING -> new TracedImageDrawer(getImageDrawer(128, 128, "healing_boy"), 0.5f, 0.25f);
        };
    }

    public static ImageDrawer getFilledHeartSprite() {
        return new SimpleImageDrawer(getImageProvider("hp", 128, 128));
    }

    public static ImageDrawer getLostHeartSprite(DamageCause cause) {
        return switch (cause) {
            case INFECTION ->
                (Math.random() < 0.5) ?
                    getImageDrawer(128, 128, "damaged_hp1/beating", "damaged_hp1/eye") :
                    getImageDrawer(128, 128, "damaged_hp2/heart", "damaged_hp2/eye1", "damaged_hp2/eye2", "damaged_hp2/eye3");
            case DAMAGED_SWORD -> getImageDrawer(128, 128, "broken_heart/segment1", "broken_heart/segment2", "broken_heart/segment3");
        };
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
        try (var info = GameController.class.getResourceAsStream("images/" + name + "/info.properties")){
            var properties = new Properties();
            properties.load(info);
            float period = Float.parseFloat(properties.getProperty("period"));
            List<Image> trueFrames = new ArrayList<>();
            for (int j = 0; j < frames.size(); j++) {
                int count = Integer.parseInt(properties.getProperty("frame" + j, String.valueOf(1)));
                for (int k = 0; k < count; k++) {
                    trueFrames.add(frames.get(j));
                }
            }
            return new AnimatedImageProvider(trueFrames, period);
        } catch (IllegalArgumentException e) {
            throw new NoSuchFileException("No animated sprite \"" + name + "\"");
        } catch (NullPointerException e) {
            throw new NoSuchFileException("No info.properties for animated sprite \"" + name + "\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static ImageDrawer getImageDrawer(int width, int height, String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("Cannot create ImageDrawer with no sprites");
        }
        if (names.length == 1) {
            return new SimpleImageDrawer(getImageProvider(names[0], width, height));
        }
        List<ImageProvider> providers = new ArrayList<>();
        for (String name : names) {
            providers.add(getImageProvider(name, width, height));
        }
        return new CombinedImageDrawer(providers);
    }
}
