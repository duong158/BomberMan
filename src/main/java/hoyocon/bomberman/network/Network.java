package hoyocon.bomberman.network;

import com.esotericsoftware.kryo.Kryo;

public class Network {
    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;

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
    }

    // ✅ Response gửi từ Server -> Client
    public static class JoinResponse {
        public String message;
        public boolean accepted;
    }

    public static class InviteRequest {
        public String fromName;
        public String targetName;
    }

    public static class StartGameRequest {}
    public static class StartGameSignal {}
}