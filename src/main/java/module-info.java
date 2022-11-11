module com.example.singingsword {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.singingsword to javafx.fxml;
    exports com.example.singingsword;
}