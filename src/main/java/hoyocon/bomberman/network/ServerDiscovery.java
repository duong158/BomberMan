package hoyocon.bomberman.network;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerDiscovery {
    private static final int DISCOVERY_PORT = 7779;
    private static final String DISCOVERY_REQUEST = "BOMBERMAN_SERVER_DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE = "BOMBERMAN_SERVER_DISCOVERY_RESPONSE";
    private static final int TIMEOUT_MS = 3000;

    /**
     * Tìm tất cả các server có sẵn trong mạng LAN
     * @return Danh sách địa chỉ IP của các server tìm thấy
     */
    public static List<String> findServers() {
        List<String> serverAddresses = new ArrayList<>();
        AtomicBoolean searching = new AtomicBoolean(true);
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);
            
            // Gửi gói broadcast
            byte[] sendData = DISCOVERY_REQUEST.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, 
                    InetAddress.getByName("255.255.255.255"), 
                    DISCOVERY_PORT);
            
            System.out.println("Đang tìm kiếm server trong mạng LAN...");
            socket.send(sendPacket);
            
            // Thread nhận phản hồi
            Thread receiveThread = new Thread(() -> {
                try {
                    while (searching.get()) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        
                        try {
                            socket.receive(receivePacket);
                            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            
                            if (response.startsWith(DISCOVERY_RESPONSE)) {
                                String serverIP = receivePacket.getAddress().getHostAddress();
                                System.out.println("Đã tìm thấy server tại: " + serverIP);
                                
                                if (!serverAddresses.contains(serverIP)) {
                                    serverAddresses.add(serverIP);
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            // Timeout, kết thúc tìm kiếm
                            searching.set(false);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            receiveThread.start();
            
            // Đợi kết quả trong khoảng thời gian nhất định
            try {
                Thread.sleep(TIMEOUT_MS);
                searching.set(false);
                receiveThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return serverAddresses;
    }
}