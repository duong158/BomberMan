module hoyocon.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens hoyocon.bomberman to javafx.fxml;
    opens hoyocon.bomberman.Object to javafx.fxml;
    exports hoyocon.bomberman;
}