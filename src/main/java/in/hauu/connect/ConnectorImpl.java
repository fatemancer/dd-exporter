package in.hauu.connect;

import in.hauu.diary.Diary;
import in.hauu.diary.Record;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;

@Slf4j
public class ConnectorImpl implements Connector {

    private static final String PAGE_TEMPLATE = "http://darkdiary.ru/users/%s?page=%s";
    private static final String RECORD_TEMPLATE = "http://darkdiary.ru/users/%s/%s/comment/";
    private static final String HOST = "http://darkdiary.ru";
    private static final String LOGIN_API = HOST + "/auth/login";

    protected static final Map<String, HttpClient> cachedHttpClient = new HashMap<>();
    private final CacheHandler cacheHandler;

    public ConnectorImpl(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    @Override
    public Diary retrieveDiary(String user, String password) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();

        HttpRequest build = HttpRequest.newBuilder()
                .POST(authParams(user, password))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Referer", "http://darkdiary.ru/")
                .uri(URI.create(LOGIN_API)).build();

        injectCookies(user, client, build);

        Parser parser = new Parser();
        HashMap<String, String> meta = parser.getMeta(retrievePages(user, 2).get(0));
        int lastIndex = Integer.parseInt(meta.getOrDefault("lastDiaryIndex", "0"));
        log.info("Last index: {}", lastIndex);
        return parser.parse(user, meta, retrievePages(user, lastIndex));
    }

    protected void injectCookies(
            String user,
            HttpClient client,
            HttpRequest build
    ) throws IOException, InterruptedException {
        client.send(build, this::firstHandle);
        cachedHttpClient.put(user, client);
    }

    private List<String> retrievePages(String user, int lastPage) {
        ProgressBar bar = new ProgressBar("Выкачиваем страницы", lastPage);
        var strings = new ArrayList<String>();
        for (int i = 1; i <= lastPage; i++) {
            bar.next();
            strings.add(wrapCache(user, i));
        }
        return strings;
    }

    private String wrapCache(Integer eid, String url, HttpClient client) {
        String seid = String.valueOf(eid);
        return cacheHandler.getPost(seid).orElseGet(() -> {
            String data = retry(this::tryConnect, client, url);
            cacheHandler.putPost(seid, data);
            return data;
        });
    }

    private String wrapCache(String user, int i) {
        HttpClient httpClient = cachedHttpClient.get(user);
        return cacheHandler.getPage(user, i).orElseGet(() -> {
            String url = String.format(PAGE_TEMPLATE, user, i);
            String data = retry(this::tryConnect, httpClient, url);
            cacheHandler.putPage(user, i, data);
            return data;
        });
    }

    protected String retry(
            BiFunction<HttpClient, String, HttpResponse<String>> function,
            HttpClient httpClient,
            String url
    ) {
        HttpResponse<String> response;
        int retries = 0;
        do {
            response = function.apply(httpClient, url);
            retries++;
        } while (response.statusCode() != 200 || retries != 3);
        return response.body();
    }

    @Override
    public void enrich(Diary diary) {
        var cachedClient = cachedHttpClient.get(diary.getLogin());
        long recsNeedEnrichment = diary.getFlatRecords()
                .filter(r -> r.getEnrichment() == Record.Enrichment.NEEDED)
                .count();
        ProgressBar bar = new ProgressBar("Выкачиваем комменты", (int) recsNeedEnrichment);
        diary.getFlatRecords()
                .filter(r -> r.getEnrichment() == Record.Enrichment.NEEDED)
                .forEach(r -> {
                            bar.next();
                            String recordUrl = String.format(RECORD_TEMPLATE, diary.getLogin(), r.getEid());
                            String commentsBlock = wrapCache(r.getEid(), recordUrl, cachedClient);
                            new Parser().injectComments(r, commentsBlock);
                        }
                );
    }

    private HttpResponse<String> tryConnect(HttpClient client, String url) {
        try {
            return client.send(getRequest(url), (r) -> handle(r, url));
        } catch (IOException | InterruptedException e) {
            log.error("Error, function caller will possibly retry: ", e);
            return null;
        }
    }

    private HttpRequest getRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url)).build();
    }

    private HttpResponse.BodySubscriber<String> firstHandle(HttpResponse.ResponseInfo responseInfo) {
        if (responseInfo.statusCode() != 200) {
            log.info(String.format(
                    "Code: %s, injected cookies: %s",
                    responseInfo.statusCode(),
                    responseInfo.headers().firstValue("set-cookie")
            ));
        }
        return HttpResponse.BodySubscribers.ofString(Charset.defaultCharset());
    }

    private HttpResponse.BodySubscriber<String> handle(
            HttpResponse.ResponseInfo responseInfo,
            String url
    ) {
        if (responseInfo.statusCode() != 200) {
            log.info(String.format("Code %s, page %s", responseInfo.statusCode(), url));
        }
        return HttpResponse.BodySubscribers.ofString(Charset.defaultCharset());
    }

    private HttpRequest.BodyPublisher authParams(String user, String password) {
        return HttpRequest.BodyPublishers.ofString(String.format("login=%s&password=%s", user, password));
    }
}
