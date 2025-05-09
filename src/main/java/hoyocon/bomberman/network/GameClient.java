package hoyocon.bomberman.network;

import java.io.IOException;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Client;

public class GameClient {
    private static boolean running = true;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.start();

        try {
            System.out.println("Đang kết nối đến server...");
            client.connect(5000, "192.168.110.32", Network.TCP_PORT, Network.UDP_PORT);
            System.out.println("Đã kết nối thành công!");

            // Đăng ký các class
            client.getKryo().register(Network.JoinRequest.class);
            client.getKryo().register(Network.JoinResponse.class);
            client.getKryo().register(Network.StartGameRequest.class);
            client.getKryo().register(Network.StartGameSignal.class);

            client.addListener(new com.esotericsoftware.kryonet.Listener() {
                public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                    if (object instanceof Network.JoinResponse response) {
                        System.out.println("📩 Server phản hồi: " + response.message);
                    } else if (object instanceof Network.StartGameSignal) {
                        System.out.println("🎮 Nhận tín hiệu bắt đầu game!");
                        // Ở đây bạn có thể chuyển đến màn hình game
                    }
                }

                @Override
                public void disconnected(com.esotericsoftware.kryonet.Connection connection) {
                    System.out.println("❌ Mất kết nối với server!");
                    running = false;
                }
            });

            // Gửi yêu cầu tham gia
            Network.JoinRequest request = new Network.JoinRequest();
            request.playerName = "Player1";
            client.sendTCP(request);

            // Giữ chương trình chạy và xử lý lệnh người dùng
            Scanner scanner = new Scanner(System.in);
            System.out.println("Nhập 'start' để bắt đầu game hoặc 'exit' để thoát:");

            while (running) {
                if (scanner.hasNextLine()) {
                    String command = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(command)) {
                        running = false;
                    } else if ("start".equalsIgnoreCase(command)) {
                        System.out.println("Gửi yêu cầu bắt đầu game...");
                        client.sendTCP(new Network.StartGameRequest());
                    }
                }

                // Ngủ một chút để không sử dụng 100% CPU
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Đóng kết nối...");
            client.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("❌ Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
