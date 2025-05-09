package hoyocon.bomberman.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameServer {
    private Server server;
    private Map<Integer, String> connectedPlayers = new HashMap<>();
    private boolean isRunning = false;

    public GameServer() {
        server = new Server();
        Network.register(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("📡 New client connected: " + connection.getID());
            }

            @Override
            public void disconnected(Connection connection) {
                String playerName = connectedPlayers.remove(connection.getID());
                System.out.println("❌ Client disconnected: " + connection.getID() +
                        (playerName != null ? " (" + playerName + ")" : ""));
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Network.JoinRequest) {
                    Network.JoinRequest request = (Network.JoinRequest) object;
                    System.out.println("📥 Received join request from: " + request.playerName);

                    // Lưu thông tin người chơi
                    connectedPlayers.put(connection.getID(), request.playerName);

                    // Gửi phản hồi
                    Network.JoinResponse response = new Network.JoinResponse();
                    response.message = "Xin chào " + request.playerName + "! Bạn đã kết nối thành công.";
                    response.accepted = true;
                    connection.sendTCP(response);
                }
                else if (object instanceof Network.InviteRequest) {
                    Network.InviteRequest invite = (Network.InviteRequest) object;
                    System.out.println("📨 Invite from " + invite.fromName + " to " + invite.targetName);

                    // Tìm connection của người nhận lời mời
                    for (Map.Entry<Integer, String> entry : connectedPlayers.entrySet()) {
                        if (entry.getValue().equals(invite.targetName)) {
                            server.sendToTCP(entry.getKey(), invite);
                            break;
                        }
                    }
                }
                else if (object instanceof Network.StartGameRequest) {
                    System.out.println("🎮 Received start game request");
                    // Broadcast start game signal
                    Network.StartGameSignal signal = new Network.StartGameSignal();
                    server.sendToAllTCP(signal);
                }
            }
        });
    }

    public void start() {
        if (!isRunning) {
            try {
                server.start();
                server.bind(Network.TCP_PORT, Network.UDP_PORT);
                isRunning = true;
                System.out.println("🚀 Server started on TCP port " + Network.TCP_PORT +
                        " and UDP port " + Network.UDP_PORT);
            } catch (IOException e) {
                System.err.println("❌ Could not start server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (isRunning) {
            server.stop();
            isRunning = false;
            System.out.println("⏹️ Server stopped");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}