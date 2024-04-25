// MainServer.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainServer {

    public static String name = "[MAIN SERVER]";
    public static int port = 20000;
    public static ArrayList<LanguageServerModel> languageServers = new ArrayList<>();

    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(port)) {

            log("Uruchomino na porcie: " + port);

            while (true) {
                var clientSocket = serverSocket.accept();
                log("Nowe polaczenie: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                runThreadForConnection(clientSocket);
            }

        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    public static void runThreadForConnection(Socket client) {
        new Thread(() -> {
            try {
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                var message = in.readLine().split(";");

                if (message[0].contains("a")) {
                    if (message.length >= 4) {
                        languageServers.add(new LanguageServerModel(message[1], message[2], message[3]));
                        log("Dodano server jezykowy: " + message[1] + " " + message[2] + ":" + message[3]);
                    } else {
                        log("Błędna wiadomość: niepoprawna liczba elementów w komunikacie 'a'");
                    }
                    client.close();
                } else if (message[0].contains("l")) {
                    if (message.length >= 1) {
                        var out = new PrintWriter(client.getOutputStream(), true);

                        String response = "";
                        for (var languageServer : languageServers) {
                            response += ";" + languageServer.Language;
                        }

                        out.println(response);
                        out.flush();
                    } else {
                        log("Błędna wiadomość: niepoprawna liczba elementów w komunikacie 'l'");
                    }
                } else if (message[0].contains("t")) {
                    if (message.length >= 4) {
                        var language = message[1];
                        var word = message[2];
                        var clientPort = message[3];
                        client.close();

                        var targetLanguageServer = languageServers.stream()
                                .filter(server -> server.Language.contains(language))
                                .findFirst().orElse(null);

                        if (targetLanguageServer != null) {
                            targetLanguageServer.Address = targetLanguageServer.Address.replace("/", "");

                            var socket = new Socket(targetLanguageServer.Address, Integer.parseInt(targetLanguageServer.Port));
                            var out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(word + ";" + client.getInetAddress().getHostAddress() + ";" + clientPort);
                            out.flush();
                            socket.close();
                        } else {
                            log("Nie znaleziono serwera dla języka: " + language);
                        }
                    } else {
                        log("Błędna wiadomość: niepoprawna liczba elementów w komunikacie 't'");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void log(String message) {
        System.out.println(name + " " + message);
    }
}
