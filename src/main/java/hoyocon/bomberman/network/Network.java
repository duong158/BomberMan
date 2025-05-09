package hoyocon.bomberman.network;

import com.esotericsoftware.kryo.Kryo;

public class Network {
    public static final int TCP_PORT = 7777;
    public static final int UDP_PORT = 7778;

    public static void register(Kryo kryo) {
        kryo.register(JoinRequest.class);
        kryo.register(JoinResponse.class);
        kryo.register(InviteRequest.class);
        kryo.register(String.class);
        kryo.register(StartGameRequest.class);
        kryo.register(StartGameSignal.class);
        kryo.register(PlayerJoined.class);
        kryo.register(PlayerDisconnected.class);
        kryo.register(PlayerMove.class);
        kryo.register(PlaceBomb.class);
        kryo.register(BombExplode.class);
    }

    // ✅ Request gửi từ Client -> Server
    public static class JoinRequest {
        public String playerName;
        
        // Thêm constructor không tham số
        public JoinRequest() {}
    }

    // ✅ Response gửi từ Server -> Client
    public static class JoinResponse {
        public String message;
        public boolean accepted;
        
        // Thêm constructor không tham số
        public JoinResponse() {}
    }

    public static class InviteRequest {
        public String fromName;
        public String targetName;
    }

    public static class StartGameRequest {
        // Thêm constructor không tham số
        public StartGameRequest() {}
    }

    public static class StartGameSignal {
        // Thêm constructor không tham số
        public StartGameSignal() {}
    }

    public static class PlayerJoined {
        public String playerName;
    }

    // Tin nhắn khi người chơi ngắt kết nối
    public static class PlayerDisconnected {
        public String playerName;
    }

    // Tin nhắn di chuyển người chơi
    public static class PlayerMove {
        public String playerName;
        public int direction; // 0: UP, 1: RIGHT, 2: DOWN, 3: LEFT
    }

    // Tin nhắn khi người chơi đặt bom
    public static class PlaceBomb {
        public String playerName;
        public int row;
        public int col;
    }

    // Tin nhắn khi bom nổ
    public static class BombExplode {
        public int row;
        public int col;
        public int range;
    }
}