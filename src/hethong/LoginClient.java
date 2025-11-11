package hethong;

import java.io.*;
import java.net.*;

public class LoginClient {
    public static boolean checkLogin(String username, String password) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(username + "," + password); // gửi user,pass
            String response = in.readLine();
            return "OK".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false; // nếu không kết nối được → coi như login fail
        }
    }
}
