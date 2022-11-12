module com.example.singingsword {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.singingsword to javafx.fxml;
    exports com.example.singingsword;
    exports com.example.singingsword.sound;
    opens com.example.singingsword.sound to javafx.fxml;
    exports com.example.singingsword.game;
    opens com.example.singingsword.game to javafx.fxml;
    exports com.example.singingsword.game.engine;
    opens com.example.singingsword.game.engine to javafx.fxml;
}