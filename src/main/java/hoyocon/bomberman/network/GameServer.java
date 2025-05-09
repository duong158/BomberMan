package hoyocon.bomberman.network;

import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class GameServer {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
        server.bind(Network.TCP_PORT, Network.UDP_PORT);

        // Đăng ký lớp
        server.getKryo().register(Network.JoinRequest.class);
        server.getKryo().register(Network.JoinResponse.class);

        server.addListener(new com.esotericsoftware.kryonet.Listener() {
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                if (object instanceof Network.JoinRequest request) {
                    System.out.println("Người chơi yêu cầu tham gia: " + request.playerName);

                    Network.JoinResponse response = new Network.JoinResponse();
                    response.accepted = true;
                    response.message = "Chào " + request.playerName + ", đã tham gia server!";
                    connection.sendTCP(response);
                }
            }
        });

        System.out.println("🔧 Server đang chạy trên port " + Network.TCP_PORT);
    }
}
