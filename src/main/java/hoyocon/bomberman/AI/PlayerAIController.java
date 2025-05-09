package hoyocon.bomberman.AI;

import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Object.EnemyGroup.Doria;
import hoyocon.bomberman.Object.EnemyGroup.Enemy;
import hoyocon.bomberman.Object.EnemyGroup.Oneal;
import hoyocon.bomberman.Object.Player;
import javafx.scene.layout.Pane;

import java.util.*;

public class PlayerAIController {
    private Player player;
    private Entity playerEntity;
    private GMap map;
    private List<Enemy> enemies;
    private Pane gamePane;
    private Position lastPlayerPosition = null;
    private long lastTime = 0;

    // AI States
    private enum AIState {
        IDLE,
        FINDING_PATH,
        FOLLOWING_PATH,
        PLACING_BOMB,
        ESCAPING,
        AVOIDING_ENEMIES
    }
    
    private AIState currentState = AIState.IDLE;
    private final boolean DEBUG = true;
    private boolean bombPlaced = false;

    private List<Node> currentPath;
    private Node targetExit;

    private static final long BOMB_DELAY_NS = 3_500_000_000L; // 2 seconds
    private static final int TILE_SIZE = 48;
    private long bombPlacedTime;
    private int bombRow, bombCol;
    private Set<String> dangerZones = new HashSet<>();
    private List<Node> escapePath;

    private static final int ENEMY_DANGER_DISTANCE = 1;
    private static final int ENEMY_AWARE_DISTANCE = 5;
    private static final int MAX_PATH_FINDING_ATTEMPTS = 3;
    
    /**
     * Position class to track row/column coordinates
     */
    private static class Position {
        int row, col;
        
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public String toString() {
            return "[" + row + "," + col + "]";
        }
        
        boolean equals(Position other) {
            return this.row == other.row && this.col == other.col;
        }
    }
    
    /**
     * Node class for pathfinding
     */
    private static class Node {
        int row, col;
        int g = Integer.MAX_VALUE;
        int h = 0;
        int f = Integer.MAX_VALUE;
        Node parent;
        
        Node(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public String toString() {
            return "[" + row + "," + col + "]";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return row == node.row && col == node.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /**
     * Create a new PlayerAIController
     */
    public PlayerAIController(Player player, GMap map, List<Enemy> enemies, Pane gamePane) {
        this.player = player;
        this.playerEntity = player.getEntity();
        this.map = map;
        this.enemies = enemies;
        this.gamePane = gamePane;

        findExitPosition();
        log("AI Controller initialized");
        currentState = AIState.FINDING_PATH;
    }
    
    /**
     * Find the exit position in the map
     */
    private void findExitPosition() {
        for (int r = 0; r < map.height; r++) {
            for (int c = 0; c < map.width; c++) {
                if (map.getTileType(r, c) == GMap.EXIT) {
                    this.targetExit = new Node(r, c);
                    log("Found exit at " + targetExit);
                    return;
                }
            }
        }
        // Fallback if no exit found - use bottom right corner
        this.targetExit = new Node(map.height - 2, map.width - 2);
        log("No exit found, using fallback position " + targetExit);
    }

    /**
     * Update the AI controller each frame
     */
    public void update(long now) {
        if (lastTime == 0) {
            lastTime = now;
        }
        double dt = (now - lastTime) / 1_000_000_000.0;
        dt = Math.min(dt, 0.05);
        lastTime = now;

        Position playerPos = getPlayerPosition();
        
        log("AI State: " + currentState + ", Player at " + playerPos + "Player At" + playerEntity.getX() + "," + playerEntity.getY());

        switch (currentState) {
            case IDLE:
                makeDecision(playerPos);
                break;
                
            case FINDING_PATH:
                findNewPath(playerPos);
                break;
                
            case FOLLOWING_PATH:
                followCurrentPath(playerPos, now);
                break;
                
            case PLACING_BOMB:
                placeBombAndPrepareEscape(playerPos, now);
                break;
                
            case ESCAPING:
                escapeFromDanger(playerPos);
                break;
                
            case AVOIDING_ENEMIES:
                avoidEnemies(playerPos);
                break;
        }
    }
    
    /**
     * Get the player's current position in grid coordinates
     */
    private Position getPlayerPosition() {
        double left = playerEntity.getX();
        double top = playerEntity.getY();
        double right = left + 45;
        double bottom = top + 45;

        int leftCol = GMap.pixelToTile(left);
        int rightCol = GMap.pixelToTile(right+1);
        int topRow = GMap.pixelToTile(top);
        int bottomRow = GMap.pixelToTile(bottom+1);

        if (leftCol == rightCol && topRow == bottomRow) {
            lastPlayerPosition = new Position(topRow, leftCol);
            return lastPlayerPosition;
        }
        if(lastPlayerPosition == null) lastPlayerPosition = new Position(topRow, leftCol);

        if(lastPlayerPosition == null) return new Position((topRow + bottomRow)/2, (leftCol+rightCol)/2);
        return lastPlayerPosition;
    }
    
    /**
     * Make a decision about what to do next
     */
    private void makeDecision(Position playerPos) {
        if (isEnemyInDangerZone(playerPos)) {
            log("Enemy in danger zone detected, switching to AVOIDING_ENEMIES");
            currentState = AIState.AVOIDING_ENEMIES;
            return;
        }

        currentState = AIState.FINDING_PATH;
    }
    
    /**
     * Find a new path to a target
     */
    private void findNewPath(Position playerPos) {
        if (playerPos == null) {
            log("Player position is null, cannot find path. Waiting for next frame.");
            player.stop();
            currentState = AIState.IDLE;
            return;
        }
        log("Finding new path from " + playerPos);

        currentPath = findPathAStar(playerPos, targetExit);

        if (currentPath == null || currentPath.isEmpty()) {
            log("No path to exit found, looking for brick to destroy");
            Node nearestBrick = findBestBrickToDestroy(playerPos);
            
            if (nearestBrick != null) {
                log("Found brick target at " + nearestBrick);
                currentPath = findPathAStar(playerPos, nearestBrick);
            }
        } else {
            log("Path to exit found with " + currentPath.size() + " steps");
        }
        
        if (currentPath != null && !currentPath.isEmpty()) {
            currentState = AIState.FOLLOWING_PATH;
        } else {
            log("No viable path found. Waiting for map changes.");
            player.stop();
            currentState = AIState.IDLE;
        }
    }
    
    /**
     * Follow the current path to target
     */
    private void followCurrentPath(Position playerPos, long now) {
        if (currentPath == null || currentPath.isEmpty()) {
            currentState = AIState.FINDING_PATH;
            return;
        }
        
        Node nextStep = currentPath.get(0);

        if (map.getTileType(nextStep.row, nextStep.col) == GMap.BRICK) {
            log("Reached brick at " + nextStep + ", preparing to place bomb");
            currentState = AIState.PLACING_BOMB;
            return;
        }

        if (isEnemyInDangerZone(new Position(nextStep.row, nextStep.col))) {
            log("Enemy detected near path, finding alternate route");
            currentState = AIState.AVOIDING_ENEMIES;
            return;
        }

        log("Moving to " + nextStep);
        moveToNode(nextStep);

        if (arePositionsEqual(playerPos, new Position(nextStep.row, nextStep.col))) {
            log("Reached step " + nextStep + ", moving to next");
            currentPath.remove(0);
            
            if (currentPath.isEmpty()) {
                log("Path completed");
                currentState = AIState.IDLE;
            }
        }
    }
    
    /**
     * Place a bomb and prepare escape route
     */
    private void placeBombAndPrepareEscape(Position playerPos, long now) {
        bombPlaced = player.placeBomb(gamePane);
        
        if (bombPlaced) {
            log("Bomb placed at " + playerPos);
            bombRow = playerPos.row;
            bombCol = playerPos.col;
            bombPlacedTime = now;

            calculateBombDangerZones();

            escapePath = findEscapePath(playerPos);
            
            if (escapePath != null && !escapePath.isEmpty()) {
                log("Escape path found with " + escapePath.size() + " steps");
                currentState = AIState.ESCAPING;
            } else {
                log("No escape path found! Taking chances...");
                player.stop();
                // We'll still wait for explosion
                currentState = AIState.ESCAPING;
            }
        } else {
            log("Failed to place bomb, finding new path");
            currentState = AIState.FINDING_PATH;
        }
    }
    
    /**
     * Follow escape path away from bomb danger
     */
    private void escapeFromDanger(Position playerPos) {
        if (System.nanoTime() - bombPlacedTime >= BOMB_DELAY_NS) {
            log("Bomb should have exploded, returning to path finding");
            dangerZones.clear();
            bombPlaced = false;
            currentState = AIState.IDLE;
            return;
        }
        
        if (escapePath == null || escapePath.isEmpty()) {
            log("No escape path to follow, staying put and hoping");
            player.stop();
            return;
        }
        
        Node safeSpot = escapePath.get(0);
        log("Escaping to " + safeSpot);
        moveToNode(safeSpot);
        
        if (arePositionsEqual(playerPos, new Position(safeSpot.row, safeSpot.col))) {
            escapePath.remove(0);
            if (escapePath.isEmpty()) {
                if (isEnemyInDangerZone(playerPos)){
                    log("Safe from bomb but enemy nearby, switching to AVOIDING_ENEMIES");
                    currentState = AIState.AVOIDING_ENEMIES;
                } else {
                    log("Reached safe position, waiting for explosion");
                    player.stop();
                    if (isEnemyInDangerZone(playerPos)) {
                        log("Safe from bomb but enemy nearby, switching to AVOIDING_ENEMIES");
                        currentState = AIState.AVOIDING_ENEMIES;
                    }
                }
            }
        }
    }
    
    /**
     * Avoid nearby enemies
     */
    private void avoidEnemies(Position playerPos) {
        if (!isEnemyInDangerZone(playerPos)) {
            log("No enemy nearby, switching back to FINDING_PATH");
            currentState = AIState.FINDING_PATH;
            return;
        }
        if (bombPlaced && !isEnemyInDangerZone(playerPos) && dangerZones.contains(keyFromPosition(playerPos.row, playerPos.col))) {
            log("No enemy nearby, bomb placed and still in danger zone, switching back to ESCAPING");
            currentState = AIState.ESCAPING;
            return;
        }

        if (isEnemyNearAndChasing(playerPos)) {
            log("Enemy chasing! Placing bomb to block");
            boolean bombPlaced = player.placeBomb(gamePane);
            if (bombPlaced) {
                bombRow = playerPos.row;
                bombCol = playerPos.col;
                bombPlacedTime = System.nanoTime();
                calculateBombDangerZones();
                escapePath = findEscapePath(playerPos);
                currentState = AIState.ESCAPING;
                return;
            }
        }

        List<Node> safetyPath = findSafetyPath(playerPos);
        
        if (safetyPath != null && !safetyPath.isEmpty()) {
            log("Moving away from enemies");
            Node safeStep = safetyPath.get(0);
            moveToNode(safeStep);
            
            if (arePositionsEqual(playerPos, new Position(safeStep.row, safeStep.col))) {
                log("Reached safe spot away from enemies");
                currentState = AIState.FINDING_PATH;
            }
        } else {
            log("Could not find safe path from enemies!");
            if (isCornered(playerPos)) {
                log("Cornered! Placing defensive bomb");
                currentState = AIState.PLACING_BOMB;
            } else {
                currentState = AIState.FINDING_PATH;
            }
        }
    }

    private boolean isEnemyNearAndChasing(Position pos) {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            if (enemy instanceof Oneal && ((Oneal) enemy).isChasing) {
                int er = GMap.pixelToTile(enemy.getEntity().getY());
                int ec = GMap.pixelToTile(enemy.getEntity().getX());
                if (manhattanDistance(pos.row, pos.col, er, ec) <= ENEMY_DANGER_DISTANCE + 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if player is cornered with no escape routes
     */
    private boolean isCornered(Position pos) {
        int wallCount = 0;
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        
        for (int[] dir : directions) {
            int r = pos.row + dir[0];
            int c = pos.col + dir[1];
            
            if (!map.isWalkable(r, c)) {
                wallCount++;
            }
        }
        
        return wallCount >= 3;
    }
    
    /**
     * Find a path to safety, away from enemies
     */
    private List<Node> findSafetyPath(Position start) {
        int[][] dangerMap = createEnemyDangerMap();

        Node safestNode = null;
        int lowestDanger = Integer.MAX_VALUE;
        if(bombPlaced) calculateBombDangerZones();

        for (int r = 0; r < map.height; r++) {
            for (int c = 0; c < map.width; c++) {

                String key = keyFromPosition(r, c);
                if (!map.isWalkable(r, c)) continue;

                if (dangerMap[r][c] < lowestDanger) {
                    Node targetNode = new Node(r, c);
                    List<Node> path = findPathAStar(start, targetNode);
                    if (path != null && !path.isEmpty() && path.stream().noneMatch(n -> dangerZones.contains(keyFromPosition(n.row, n.col)))) {
                        lowestDanger = dangerMap[r][c];
                        safestNode = targetNode;
                    }

                }
            }
        }

        if (safestNode != null) {
            return findPathAStar(start, safestNode);
        }

        return null;
    }
    
    /**
     * Create a map of danger levels from enemies
     */
    private int[][] createEnemyDangerMap() {
        int[][] dangerMap = new int[map.height][map.width];

        for (int r = 0; r < map.height; r++) {
            for (int c = 0; c < map.width; c++) {
                dangerMap[r][c] = 0;
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            
            int er = GMap.pixelToTile(enemy.getEntity().getY());
            int ec = GMap.pixelToTile(enemy.getEntity().getX());

            for (int r = 0; r < map.height; r++) {
                for (int c = 0; c < map.width; c++) {
                    int distance = manhattanDistance(r, c, er, ec);
                    if (distance <= ENEMY_AWARE_DISTANCE) {
                        int dangerValue = ENEMY_AWARE_DISTANCE - distance + 1;

                        if (enemy instanceof Oneal) dangerValue *= 2;
                        if (enemy instanceof Doria) dangerValue *= 3;
                        
                        dangerMap[r][c] += dangerValue;
                    }
                }
            }
        }
        
        return dangerMap;
    }
    
    /**
     * Calculate bomb explosion danger zones
     */
    private void calculateBombDangerZones() {
        dangerZones.clear();
        int range = player.getFlameRange();

        dangerZones.add(keyFromPosition(bombRow, bombCol));

        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        
        for (int[] dir : directions) {
            for (int i = 1; i <= range; i++) {
                int r = bombRow + dir[0] * i;
                int c = bombCol + dir[1] * i;

                if (map.getTileType(r, c) == GMap.WALL) {
                    break;
                }
                dangerZones.add(keyFromPosition(r, c));

                if (map.getTileType(r, c) == GMap.BRICK) {
                    break;
                }
            }
        }
    }
    
    /**
     * Find path away from bomb danger zones
     */
    private List<Node> findEscapePath(Position start) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.f)
        );
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start.row, start.col);
        startNode.g = 0;
        startNode.f = 0;
        
        openSet.add(startNode);
        allNodes.put(keyFromPosition(start.row, start.col), startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = keyFromPosition(current.row, current.col);
            
            if (!dangerZones.contains(currentKey)) {
                return reconstructPath(current);
            }
            
            closedSet.add(currentKey);

            int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
            for (int[] dir : dirs) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                String neighborKey = keyFromPosition(newRow, newCol);

                if (newRow < 0 || newRow >= map.height || 
                    newCol < 0 || newCol >= map.width ||
                    !map.isWalkable(newRow, newCol) || 
                    closedSet.contains(neighborKey)) {
                    continue;
                }

                Node neighbor = allNodes.getOrDefault(
                    neighborKey, new Node(newRow, newCol)
                );

                int tentativeG = current.g + 1;

                if (dangerZones.contains(neighborKey)) {
                    tentativeG += 10;
                }

                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = tentativeG;
                    
                    allNodes.put(neighborKey, neighbor);
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }
    
    /**
     * Find the nearest brick that can be destroyed
     */
    private Node findBestBrickToDestroy(Position playerPos) {
        PriorityQueue<Node> candidates = new PriorityQueue<>(
            Comparator.comparingInt(n -> 
                manhattanDistance(playerPos.row, playerPos.col, n.row, n.col)
            )
        );

        for (int r = 0; r < map.height; r++) {
            for (int c = 0; c < map.width; c++) {
                if (map.getTileType(r, c) == GMap.BRICK) {
                    int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
                    for (int[] dir : dirs) {
                        int adjRow = r + dir[0];
                        int adjCol = c + dir[1];
                        
                        if (map.isWalkable(adjRow, adjCol)) {
                            candidates.add(new Node(adjRow, adjCol));
                            break;
                        }
                    }
                }
            }
        }

        while (!candidates.isEmpty()) {
            Node target = candidates.poll();
            List<Node> path = findPathAStar(playerPos, target);
            if (path != null && !path.isEmpty()) {
                return target;
            }
        }
        
        return null;
    }
    
    /**
     * A* algorithm to find optimal path between two points
     */
    private List<Node> findPathAStar(Position start, Node goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.f)
        );
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start.row, start.col);
        startNode.g = 0;
        startNode.h = manhattanDistance(start.row, start.col, goal.row, goal.col);
        startNode.f = startNode.g + startNode.h;
        
        openSet.add(startNode);
        allNodes.put(keyFromPosition(start.row, start.col), startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = keyFromPosition(current.row, current.col);
            
            if (current.row == goal.row && current.col == goal.col) {
                return reconstructPath(current);
            }
            
            closedSet.add(currentKey);
            int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
            for (int[] dir : dirs) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                String neighborKey = keyFromPosition(newRow, newCol);

                if (newRow < 0 || newRow >= map.height ||
                        newCol < 0 || newCol >= map.width ||
                        closedSet.contains(neighborKey)) {
                    continue;
                }

                int tileType = map.getTileType(newRow, newCol);

                if (tileType == GMap.WALL) continue;

                int extraCost = 0;
                if (tileType == GMap.BRICK) {
                    extraCost = 20;
                }

                if (isEnemyAtPosition(newRow, newCol)) continue;

                Node neighbor = allNodes.getOrDefault(
                        neighborKey, new Node(newRow, newCol)
                );

                int tentativeG = current.g + 1 + extraCost;

                if (isEnemyInDangerZone(new Position(newRow, newCol))) {
                    tentativeG += 5;
                }
                if (dangerZones.contains(neighborKey)) {
                    tentativeG += 10;
                }

                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.h = manhattanDistance(newRow, newCol, goal.row, goal.col);
                    neighbor.f = neighbor.g + neighbor.h;

                    allNodes.put(neighborKey, neighbor);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }
    
    /**
     * Reconstruct path from goal node to start
     */
    private List<Node> reconstructPath(Node end) {
        List<Node> path = new ArrayList<>();
        for (Node current = end; current != null && current.parent != null; current = current.parent) {
            path.add(0, current);
        }
        return path;
    }
    
    /**
     * Check if an enemy is at a specific position
     */
    private boolean isEnemyAtPosition(int row, int col) {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            
            int er = GMap.pixelToTile(enemy.getEntity().getY());
            int ec = GMap.pixelToTile(enemy.getEntity().getX());
            
            if (er == row && ec == col) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if there's an enemy in the danger zone around a position
     */
    private boolean isEnemyInDangerZone(Position pos) {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            
            int er = GMap.pixelToTile(enemy.getEntity().getY());
            int ec = GMap.pixelToTile(enemy.getEntity().getX());

            
            int distance = manhattanDistance(pos.row, pos.col, er, ec);

            int dangerDistance = ENEMY_DANGER_DISTANCE;

            if (enemy instanceof Oneal && ((Oneal) enemy).isChasing) dangerDistance = 4;
            if (enemy instanceof Doria) dangerDistance = 6;
            
            if (distance <= dangerDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Move player toward target node
     */
    private void moveToNode(Node target) {
        int targetPixelX = (int) GMap.tileToPixel(target.col);
        int targetPixelY = (int) GMap.tileToPixel(target.row);

        Position p = getPlayerPosition();

        double playerX = playerEntity.getX();
        double playerY = playerEntity.getY();
        double threshold = 1.0;

        if (Math.abs(playerX - targetPixelX) > threshold) {
            if (playerX < targetPixelX) {
                player.moveRight(0.016);
            } else {
                player.moveLeft(0.016);
            }
        } else if (Math.abs(playerY - targetPixelY) > threshold) {
            if (playerY < targetPixelY) {
                player.moveDown(0.016);
            } else {
                player.moveUp(0.016);
            }
        } else {
            player.stop();
        }
    }
    
    /**
     * Calculate Manhattan distance between two points
     */
    private int manhattanDistance(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }
    
    /**
     * Compare two positions for equality
     */
    private boolean arePositionsEqual(Position p1, Position p2) {
        if(p1 == null || p2 == null) return false;
        return p1.row == p2.row && p1.col == p2.col;
    }
    
    /**
     * Generate a unique key for a position
     */
    private String keyFromPosition(int row, int col) {
        return row + "," + col;
    }
    
    /**
     * Log debug messages
     */
    private void log(String message) {
        if (DEBUG) {
            System.out.println("[AI] " + message);
        }
    }
    public void resetAIState() {
        lastPlayerPosition = null;
        currentState = AIState.FINDING_PATH;
        escapePath = null;
        dangerZones.clear();
        currentPath = null;
        bombPlacedTime = 0;
        bombRow = -1;
        bombCol = -1;
        log("AI state reset after respawn");
    }

    public void resetTimer() {
        lastTime = 0;
    }
}