package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.GameSceneBuilder;
import hoyocon.bomberman.Map.GMap;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

/**
 * Quái vật Pass - chết spawn hai balloon.
 */
public class Pass extends Enemy {
    private static final double PASS_SPEED = 80;
    private Pane gamePane;
    private Group gameWorld;

    public Pass(int row, int col, GMap gameMap, Pane gamePane, Group gameWorld) {
        super(
                (int)(row * GMap.TILE_SIZE),
                (int)(col * GMap.TILE_SIZE),
                PASS_SPEED,
                "/assets/textures/enemy3.png"
        );
        this.gameMap = gameMap;
        this.gamePane = gamePane;
        this.gameWorld = gameWorld;
    }

    @Override
    public void die() {
        if (!isDead()) {
            super.die();
            int row = (int) (getY() / GMap.TILE_SIZE);
            int col = (int) (getX() / GMap.TILE_SIZE);
            if (gameMap != null && gamePane != null && gameWorld != null) {
                GameSceneBuilder.spawnBalloonAt(row, col, gameMap, gamePane, gameWorld);
                GameSceneBuilder.spawnBalloonAt(row, col, gameMap, gamePane, gameWorld);
            }
        }
    }
}
