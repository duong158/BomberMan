module org.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens org.example.bomberman to javafx.fxml;
    exports org.example.bomberman;
}