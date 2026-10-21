package dev.pricewatch;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-process HTTP server standing in for a retailer. Tests set what each path returns. */
public class StubSite implements AutoCloseable {

    public record Response(int status, String body) {
    }

    private final HttpServer server;
    private final Map<String, Response> routes = new ConcurrentHashMap<>();

    public StubSite() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/", exchange -> {
            Response r = routes.getOrDefault(exchange.getRequestURI().getPath(), new Response(404, "not found"));
            byte[] body = r.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(r.status(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    public StubSite respond(String path, int status, String body) {
        routes.put(path, new Response(status, body));
        return this;
    }

    public String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
