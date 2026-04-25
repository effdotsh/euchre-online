package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.Players.Player;
import org.example.Players.RemotePlayer;
import org.example.Players.StrategyAIPlayer;
import org.example.Strategies.StrategyFactory;
import org.example.Strategies.StrategyType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HostedGame hostedGame = createGame();

        Thread gameThread = new Thread(hostedGame.game()::playGame, "euchre-web-game");
        gameThread.setDaemon(true);
        gameThread.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/game", exchange -> writeJson(exchange, hostedGame.game().snapshot()));
        server.createContext("/api/action", exchange -> handleAction(exchange, hostedGame.remotePlayer()));
        server.createContext("/app.js", exchange -> writeAsset(exchange, "web/app.js", "application/javascript; charset=utf-8"));
        server.createContext("/styles.css", exchange -> writeAsset(exchange, "web/styles.css", "text/css; charset=utf-8"));
        server.createContext("/", exchange -> writeAsset(exchange, "web/index.html", "text/html; charset=utf-8"));
        server.start();

        System.out.println("Euchre web UI available at http://localhost:" + port);
    }

    private static HostedGame createGame() {
        RemotePlayer remotePlayer = new RemotePlayer("Ephram");
        Player[] players = {
                new StrategyAIPlayer("Lance", StrategyFactory.create(StrategyType.NEUTRAL)),
                new StrategyAIPlayer("Laura", StrategyFactory.create(StrategyType.NEUTRAL)),
                remotePlayer,
                new StrategyAIPlayer("Olivia", StrategyFactory.create(StrategyType.NEUTRAL))
        };
        return new HostedGame(new Euchre(players, 1200), remotePlayer);
    }

    private static void handleAction(HttpExchange exchange, RemotePlayer remotePlayer) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            Map<String, String> params = readFormBody(exchange);
            String value = params.get("value");
            if (value == null || value.isBlank()) {
                writeText(exchange, 400, "Missing action value");
                return;
            }

            remotePlayer.submit(value);
            writeText(exchange, 204, "");
        } catch (IllegalArgumentException | IllegalStateException e) {
            writeText(exchange, 400, e.getMessage());
        }
    }

    private static Map<String, String> readFormBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        if (body.isBlank()) {
            return params;
        }
        for (String pair : body.split("&")) {
            String[] pieces = pair.split("=", 2);
            String key = decode(pieces[0]);
            String value = pieces.length > 1 ? decode(pieces[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpExchange exchange, String body) throws IOException {
        writeResponse(exchange, 200, body, "application/json; charset=utf-8");
    }

    private static void writeText(HttpExchange exchange, int status, String body) throws IOException {
        writeResponse(exchange, status, body, "text/plain; charset=utf-8");
    }

    private static void writeResponse(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().putAll(Map.of(
                "Content-Type", List.of(contentType),
                "Cache-Control", List.of("no-store")
        ));
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static void writeAsset(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try (InputStream inputStream = GameServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = inputStream.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        }
    }

    private record HostedGame(Euchre game, RemotePlayer remotePlayer) {
    }
}
