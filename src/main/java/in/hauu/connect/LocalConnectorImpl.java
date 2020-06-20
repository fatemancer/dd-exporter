package in.hauu.connect;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public class LocalConnectorImpl extends ConnectorImpl {

    private static final Map<String, String> mockUrls = Map.of(
            "http://darkdiary.ru/users/Makenshi?page=1", "src/main/resources/private/101.html",
            "http://darkdiary.ru/users/Makenshi?page=2", "src/main/resources/private/102.html",
            "http://darkdiary.ru/users/Makenshi?page=3", "src/main/resources/private/103.html",
            "comment", "src/main/resources/private/comment_example.html"
    );

    public LocalConnectorImpl(CacheHandler cacheHandler) {
        super(cacheHandler);
    }

    @SneakyThrows
    @Override
    protected String retry(
            BiFunction<HttpClient, String, HttpResponse<String>> function,
            HttpClient httpClient,
            String url
    ) {
        if (url.contains("/comment")) {
            return new String(Files.readAllBytes(Path.of(mockUrls.get("comment"))));
        } else {
            return new String(Files.readAllBytes(Path.of(mockUrls.get(url))));
        }
    }

    @Override
    protected void injectCookies(
            String user,
            HttpClient client,
            HttpRequest build
    ) throws IOException, InterruptedException {
        cachedHttpClient.put(user, client);
    }
}
