package hethong;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class LoginServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(12345);
        System.out.println("Server đang chạy trên cổng 12345...");

        // Mock database
        HashMap<String, String> users = new HashMap<>();
        users.put("admin", "123");
        users.put("shork", "456");

        while (true) {
            Socket client = server.accept();
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    String request = in.readLine(); // username,password
                    String[] parts = request.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    if (users.containsKey(username) && users.get(username).equals(password)) {
                        out.println("OK");
                    } else {
                        out.println("FAIL");
                    }

                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
