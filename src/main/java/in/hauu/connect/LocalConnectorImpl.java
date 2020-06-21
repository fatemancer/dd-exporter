package in.hauu.connect;

import in.hauu.writer.UserFileSystem;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public class LocalConnectorImpl extends ConnectorImpl {

    private static final Map<String, String> mockUrls = Map.of(
            "http://darkdiary.ru/users/Makenshi?page=1", "private/101.html",
            "http://darkdiary.ru/users/Makenshi?page=2", "private/102.html",
            "http://darkdiary.ru/users/Makenshi?page=3", "private/103.html",
            "comment", "private/comment_example.html"
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
            return UserFileSystem.getResourceAsString(mockUrls.get("comment"));
        } else {
            return UserFileSystem.getResourceAsString(mockUrls.get(url));
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
