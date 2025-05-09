module hoyocon.bomberman {
    // Third-party UI libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;

// Spring Boot & related modules
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.data.jpa;
    requires spring.security.crypto;

// JPA API
    requires jakarta.persistence;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires javafx.media;
    requires kryonet;
    requires kryo;

// Export packages for external use (if necessary)
    exports hoyocon.bomberman.network;
    exports hoyocon.bomberman;
    exports hoyocon.bomberman.api.controller;
    exports hoyocon.bomberman.api.entity;
    exports hoyocon.bomberman.Object;
    exports hoyocon.bomberman.Object.EnemyGroup;

// Open packages for reflection (JavaFX & Spring)
    opens hoyocon.bomberman to javafx.fxml;
    opens hoyocon.bomberman.Object to javafx.fxml;
    opens hoyocon.bomberman.Object.EnemyGroup to javafx.fxml;
    opens hoyocon.bomberman.api.controller to spring.beans, spring.context, spring.web;
    opens hoyocon.bomberman.api.entity to spring.core;
    opens hoyocon.bomberman.network to kryo;
}