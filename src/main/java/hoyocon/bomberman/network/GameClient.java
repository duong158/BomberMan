package hoyocon.bomberman.network;

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;

public class GameClient {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.start();
        client.connect(5000, "192.168.110.32", Network.TCP_PORT, Network.UDP_PORT); // 👈 đổi "localhost" nếu cần

        client.getKryo().register(Network.JoinRequest.class);
        client.getKryo().register(Network.JoinResponse.class);

        client.addListener(new com.esotericsoftware.kryonet.Listener() {
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                if (object instanceof Network.JoinResponse response) {
                    System.out.println("📩 Server phản hồi: " + response.message);
                }
            }
        });

        Network.JoinRequest request = new Network.JoinRequest();
        request.playerName = "Player1";
        client.sendTCP(request);
    }
}
