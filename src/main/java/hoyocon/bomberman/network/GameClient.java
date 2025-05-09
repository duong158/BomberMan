package hoyocon.bomberman.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Scanner;

public class GameClient {
    private static boolean running = true;
    private Client client;
    private String playerName;

    // Constructor cho khi sử dụng như một lớp độc lập
    public GameClient() {
        client = new Client();
        client.start();
        Network.register(client.getKryo());
    }

    // Phương thức kết nối với xử lý timeout
    public boolean connect(String host, String playerName) {
        this.playerName = playerName;
        boolean success = false;

        // Tạo thread riêng cho việc kết nối
        Thread connectThread = new Thread(() -> {
            try {
                client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT);
            } catch (IOException e) {
                System.err.println("Lỗi kết nối: " + e.getMessage());
            }
        });

        connectThread.start();

        try {
            // Đợi thread kết nối hoàn thành trong thời gian xác định
            connectThread.join(7000); // Đợi tối đa 7 giây
            if (client.isConnected()) {
                success = true;
                System.out.println("Đã kết nối thành công đến " + host);
            } else {
                System.err.println("Kết nối thất bại - timeout");
            }
        } catch (InterruptedException e) {
            System.err.println("Kết nối bị gián đoạn: " + e.getMessage());
        }

        return success;
    }

    // Gửi yêu cầu tham gia
    public void sendJoinRequest(String playerName) {
        if (client.isConnected()) {
            Network.JoinRequest request = new Network.JoinRequest();
            request.playerName = playerName;
            client.sendTCP(request);
        }
    }

    // Gửi yêu cầu bắt đầu game
    public void sendStartGameRequest() {
        if (client.isConnected()) {
            client.sendTCP(new Network.StartGameRequest());
        }
    }

    // Thêm listener
    public void addListener(Listener listener) {
        client.addListener(listener);
    }

    // Kiểm tra kết nối
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    // Đóng kết nối
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    // Lấy client gốc
    public Client getClient() {
        return client;
    }

    // Lấy tên người chơi
    public String getPlayerName() {
        return playerName;
    }

    // Main method cho ứng dụng console
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameClient gameClient = new GameClient();

        System.out.print("Nhập địa chỉ IP server (hoặc Enter để dùng 192.168.110.32): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "192.168.110.32";
        }

        System.out.print("Nhập tên người chơi (hoặc Enter để dùng Player1): ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) {
            playerName = "Player1";
        }

        System.out.println("Đang kết nối đến server " + host + "...");
        boolean connected = gameClient.connect(host, playerName);

        if (connected) {
            // Thêm listener
            gameClient.addListener(new Listener() {
                public void received(Connection connection, Object object) {
                    if (object instanceof Network.JoinResponse response) {
                        System.out.println("📩 Server phản hồi: " + response.message);
                    } else if (object instanceof Network.StartGameSignal) {
                        System.out.println("🎮 Nhận tín hiệu bắt đầu game!");
                        // Ở đây bạn có thể chuyển đến màn hình game
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    System.out.println("❌ Mất kết nối với server!");
                    running = false;
                }
            });

            // Gửi yêu cầu tham gia
            gameClient.sendJoinRequest(playerName);

            // Giữ chương trình chạy và xử lý lệnh người dùng - GIỐNG CODE HIỆN TẠI CỦA BẠN
            System.out.println("Nhập 'start' để bắt đầu game hoặc 'exit' để thoát:");

            while (running) {
                if (scanner.hasNextLine()) {
                    String command = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(command)) {
                        running = false;
                    } else if ("start".equalsIgnoreCase(command)) {
                        System.out.println("Gửi yêu cầu bắt đầu game...");
                        gameClient.sendStartGameRequest();
                    }
                }

                // Ngủ một chút để không sử dụng 100% CPU - GIỐNG CODE HIỆN TẠI CỦA BẠN
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Đóng kết nối...");
            gameClient.close();
        } else {
            System.out.println("❌ Không thể kết nối đến server.");
        }

        scanner.close();
    }
}