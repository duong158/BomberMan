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
        server.getKryo().register(Network.StartGameRequest.class);
        server.getKryo().register(Network.StartGameSignal.class);

        server.addListener(new com.esotericsoftware.kryonet.Listener() {
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                if (object instanceof Network.JoinRequest request) {
                    System.out.println("Người chơi yêu cầu tham gia: " + request.playerName);

                    Network.JoinResponse response = new Network.JoinResponse();
                    response.accepted = true;
                    response.message = "Chào " + request.playerName + ", đã tham gia server!";
                    connection.sendTCP(response);
                } else if (object instanceof Network.StartGameRequest) {
                    System.out.println("Nhận StartGameRequest, broadcast StartGameSignal cho tất cả client");
                    // Gửi tín hiệu bắt đầu game cho tất cả client
                    for (com.esotericsoftware.kryonet.Connection conn : server.getConnections()) {
                        conn.sendTCP(new Network.StartGameSignal());
                    }
                }
            }
        });

        System.out.println("🔧 Server đang chạy trên port " + Network.TCP_PORT);
    }
}
