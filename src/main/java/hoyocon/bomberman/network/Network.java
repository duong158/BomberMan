package hoyocon.bomberman.network;

import com.esotericsoftware.kryo.Kryo;

public class Network {
    public static final int TCP_PORT = 7777;
    public static final int UDP_PORT = 7778;

    public static void register(Kryo kryo) {
        kryo.register(JoinRequest.class);
        kryo.register(JoinResponse.class);
        kryo.register(InviteRequest.class);
        kryo.register(String.class); // nếu cần gửi chuỗi
        kryo.register(StartGameRequest.class);
        kryo.register(StartGameSignal.class);
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
}