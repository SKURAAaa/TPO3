// LanguageServer.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


public class LanguageServer {

    public static String name = "[LANGUAGE SERVER]";
    public static int port = 1666;
    public static String translation = "ENG";
    public static Scanner scanner = new Scanner(System.in);

    public static HashMap<String, String> dictionary = new HashMap<>() {{
        put("pies", "dog");
        put("kot", "cat");
        put("ptak", "bird");
    }};

    public static void main(String args[]){
        translation = args[0];
        port = Integer.parseInt(args[1]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            registerServer(serverSocket.getInetAddress().getHostAddress(), port);

            while (true) {
                var clientSocket = serverSocket.accept();
                log("Polaczenie:" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                runThreadForConnection(clientSocket);
            }
        } catch (IOException e) {
            log("Blad serwera: " + e.getMessage());
        }
    }


    public static void runThreadForConnection(Socket client){
        new Thread(() -> {
            try {
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                var message = in.readLine().split(";");

                if (message.length >= 3) {
                    var word = message[0];
                    var address = message[1].replace("/", "");
                    var port = Integer.parseInt(message[2]);

                    var out = new PrintWriter(new Socket(address, port).getOutputStream(), true);

                    var translation = dictionary.get(word);

                    if (translation == null)
                        out.println("BRAK SLOWA");
                    else {
                        out.println(translation);
                    }
                } else {
                    log("Błędna wiadomość: niepoprawna liczba elementów w komunikacie 't'");
                }
            }
            catch (Exception ex){ ex.printStackTrace();}
        }).start();
    }

    public static void registerServer(String myAddress, int myPort){
        var address = "127.0.0.1";
        var port = 20000;

        try(Socket socket = new Socket(address, port)){
            var out = new PrintWriter(socket.getOutputStream());
            var message = "a;" + translation + ";" + myAddress + ";" + myPort;
            out.println(message);
            out.flush();
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void log(String message){
        System.out.println(name + " " + translation + "] " + message);
    }

}
