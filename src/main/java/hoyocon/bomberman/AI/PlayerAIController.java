package hoyocon.bomberman.AI;

import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.EntitiesState.State;
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
    private Node targetExit;

    private List<Node> currentPath;
    private boolean waitingForBomb;
    private long bombPlacedTime;
    private int bombRow, bombCol;
    private Set<String> dangerZones;
    private List<Node> escapePath;

    private static final long BOMB_DELAY_NS = 2_000_000_000L;
    private static final int TILE_SIZE = 48;

    public PlayerAIController(Player player, GMap map, List<Enemy> enemies, Pane gamePane) {
        this.player = player;
        this.playerEntity = player.getEntity();
        this.map = map;
        this.enemies = enemies;
        this.gamePane = gamePane;
        int exitRow = 1;
        int exitCol = 1;
        this.targetExit = new Node(exitRow, exitCol);
        this.currentPath = new ArrayList<>();
        this.waitingForBomb = false;
        this.dangerZones = new HashSet<>();
    }

    private static class Node {
        int row, col;
        int g = Integer.MAX_VALUE, h, f;
        Node parent;
        Node(int row, int col) { this.row = row; this.col = col; }
    }

    public void update(long now) {
        int playerRow,playerCol;
        playerRow = GMap.pixelToTile(playerEntity.getY());
        playerCol = GMap.pixelToTile(playerEntity.getX());


        // Debug output
        System.out.println("AI Update: Player at [" + playerRow + "," + playerCol + "]");
        System.out.println("Player state: " + player.getState());
        System.out.println("Player position updated: [" + playerRow + ", " + playerCol + "]");

        if (waitingForBomb) {
            if (now - bombPlacedTime >= BOMB_DELAY_NS) {
                waitingForBomb = false;
                currentPath = null;
                System.out.println("Bomb exploded, resuming normal path");
            } else {
                System.out.println("Escaping from bomb");
                escapeFromBomb();
                return;
            }
        }

        if (currentPath == null || currentPath.isEmpty()) {
            System.out.println("Finding path to exit");
            currentPath = findPath(targetExit);
            if (currentPath == null) {
                System.out.println("No direct path to exit, finding brick to destroy");
                Node brick = findNearestBrickAccessPoint();
                if (brick != null) {
                    System.out.println("Found brick at [" + brick.row + "," + brick.col + "], finding path to it");
                    currentPath = findPath(brick);
                }
            } else {
                System.out.println("Path to exit found with " + currentPath.size() + " steps");
            }
        }

        if (currentPath == null || currentPath.isEmpty()) {
            System.out.println("No path found, stopping");
            player.stop();
            return;
        }

        Node next = currentPath.get(0);
        if (isEnemyNearby(next.row, next.col)) {
            currentPath = null;
            return;
        }

        if (map.getTileType(next.row, next.col) == GMap.BRICK) {
            // đặt bom và đợi nổ
            player.placeBomb(gamePane);
            waitingForBomb = true;
            bombPlacedTime = now;
            bombRow = next.row;
            bombCol = next.col;
            buildDangerZones();
            escapePath = findEscapePath(playerRow, playerCol);
            return;
        }

        moveTo(next);
        if (playerRow == next.row && playerCol == next.col) {
            currentPath.remove(0);
        }
    }

    private void buildDangerZones() {
        dangerZones.clear();
        int range = player.getFlameRange();
        dangerZones.add(key(bombRow, bombCol));
        for (int d = 1; d <= range; d++) {
            for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int r = bombRow + dir[0] * d;
                int c = bombCol + dir[1] * d;
                if (!map.isWalkable(r, c)) break;
                dangerZones.add(key(r, c));
            }
        }
    }

    private List<Node> findEscapePath(int startRow, int startCol) {
        boolean[][] visited = new boolean[map.height][map.width];
        Queue<Node> q = new LinkedList<>();
        Map<String, Node> parent = new HashMap<>();
        Node start = new Node(startRow, startCol);
        q.add(start);
        visited[startRow][startCol] = true;
        parent.put(key(startRow, startCol), null);

        while (!q.isEmpty()) {
            Node u = q.poll();
            if (!dangerZones.contains(key(u.row, u.col))) {
                return buildPath(u, parent);
            }
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int r = u.row + d[0], c = u.col + d[1];
                String k = key(r, c);
                if (map.isWalkable(r, c) && !visited[r][c] && !dangerZones.contains(k)) {
                    visited[r][c] = true;
                    parent.put(k, u);
                    q.add(new Node(r, c));
                }
            }
        }
        return null;
    }

    private List<Node> findPath(Node goal) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, Node> all = new HashMap<>();
        int sr = GMap.pixelToTile(playerEntity.getY());
        int sc = GMap.pixelToTile(playerEntity.getX());
        Node start = new Node(sr, sc);
        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.h;
        open.add(start);
        all.put(key(sr, sc), start);

        while (!open.isEmpty()) {
            Node u = open.poll();
            if (u.row == goal.row && u.col == goal.col) {
                return buildPath(u);
            }
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int r = u.row + d[0], c = u.col + d[1];
                String k = key(r, c);
                if (!map.isWalkable(r, c) || isEnemyNearby(r, c) || dangerZones.contains(k)) continue;
                int cost = (map.getTileType(r, c) == GMap.BRICK ? 10 : 1);
                Node v = all.getOrDefault(k, new Node(r, c));
                int ng = u.g + cost;
                if (ng < v.g) {
                    v.g = ng;
                    v.h = heuristic(v, goal);
                    v.f = v.g + v.h;
                    v.parent = u;
                    all.put(k, v);
                    open.add(v);
                }
            }
        }
        return null;
    }

    private Node findNearestBrickAccessPoint() {
        Node best = null;
        int minDistance = Integer.MAX_VALUE;
        int playerRow = GMap.pixelToTile(playerEntity.getY());
        int playerCol = GMap.pixelToTile(playerEntity.getX());

        for (int r = 0; r < map.height; r++) {
            for (int c = 0; c < map.width; c++) {
                if (map.getTileType(r, c) == GMap.BRICK) {
                    for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                        int nr = r + dir[0], nc = c + dir[1];
                        if (map.isWalkable(nr, nc)) {
                            int distance = Math.abs(playerRow - nr) + Math.abs(playerCol - nc);
                            if (distance < minDistance) {
                                minDistance = distance;
                                best = new Node(nr, nc);
                            }
                        }
                    }
                }
            }
        }
        return best;
    }


    private List<Node> buildPath(Node end) {
        List<Node> path = new ArrayList<>();
        for (Node cur = end; cur != null && cur.parent != null; cur = cur.parent) {
            path.add(0, cur);
        }
        return path;
    }

    private List<Node> buildPath(Node end, Map<String, Node> parent) {
        List<Node> path = new ArrayList<>();
        Node cur = end;
        while (cur != null) {
            path.add(0, cur);
            cur = parent.get(key(cur.row, cur.col));
        }
        return path;
    }

    private boolean isEnemyNearby(int row, int col) {
        for (Enemy e : enemies) {
            int er = GMap.pixelToTile(e.getEntity().getY());
            int ec = GMap.pixelToTile(e.getEntity().getX());
            int dist = Math.abs(row - er) + Math.abs(col - ec);
            if (e instanceof Oneal && dist <= 5) return true;
            if (e instanceof Doria && dist <= 7) return true;
            if (!(e instanceof Oneal) && dist == 0) return true;
        }
        return false;
    }

    private void moveTo(Node next) {
        double tx = next.col;
        double ty = next.row;
        int px,py;
        px = (int) Math.round(playerEntity.getX() / TILE_SIZE);
        py = (int) Math.round(playerEntity.getY() / TILE_SIZE);
        
        if (px!=tx) {
            if (px < tx) {
                System.out.println("AI moving RIGHT to get to column " + next.col);
                player.moveRight(0.016);
            } else {
                System.out.println("AI moving LEFT to get to column " + next.col);
                player.moveLeft(0.016);
            }
        } else if (py!=ty) {
            if (py < ty) {
                System.out.println("AI moving DOWN to get to row " + next.row);
                player.moveDown(0.016);
            } else {
                System.out.println("AI moving UP to get to row " + next.row);
                player.moveUp(0.016);
            }
        } else {
            System.out.println("AI reached target position [" + next.row + "," + next.col + "], stopping");
            player.stop();
        }
    }

    private void escapeFromBomb() {
        if (escapePath == null || escapePath.isEmpty()) return;
        Node next = escapePath.get(0);
        int playerRow = GMap.pixelToTile(playerEntity.getY());
        int playerCol = GMap.pixelToTile(playerEntity.getX());
        if (playerRow == next.row && playerCol == next.col) {
            escapePath.remove(0);
        } else {
            moveTo(next);
        }
    }

    private int heuristic(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private String key(int r, int c) { return r + "," + c; }
}