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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConnectorImpl implements Connector {

    private static final String PAGE_TEMPLATE = "http://darkdiary.ru/users/%s?page=%s";
    private static final String RECORD_TEMPLATE = "http://darkdiary.ru/users/%s/%s/comment/";
    private static final String HOST = "http://darkdiary.ru";
    private static final String LOGIN_API = HOST + "/auth/login";

    private static final Map<String, HttpClient> cachedHttpClient = new HashMap<>();
    private static final Map<String, String> cachedPostData = new HashMap<>();

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

        // inject cookies
        HttpResponse<String> send = client.send(build, this::firstHandle);
        cachedHttpClient.put(user, client);

        Parser parser = new Parser();
        HashMap<String, String> meta = parser.getMeta(retrievePages(user, 2, client).get(0));
        int lastIndex = Integer.parseInt(meta.getOrDefault("lastDiaryIndex", "0"));
        lastIndex = 5;
        log.info("Last index: {}", lastIndex);
        return parser.parse(user, meta, retrievePages(user, lastIndex, client));
    }

    private List<String> retrievePages(String user, int lastPage, HttpClient client) {
        var strings = new ArrayList<String>();
        ProgressBar bar = new ProgressBar("Выкачиваем страницы", lastPage);
        for (int i = 1; i <= lastPage; i++) {
            bar.next();
            bar.show();
            String url = String.format(PAGE_TEMPLATE, user, i);
            int retries = 0;
            try {
                tryConnect(client, url, strings, i, retries);
            } catch (Exception e) {
                // hand-made craft retry policy
                retries++;
                if (retries > 3) {
                    log.error("Failed at page %s, trying to save what is left");
                    return strings;
                }
                tryConnect(client, url, strings, i, retries);
            }
        }
        return strings;
    }

    private String retrieveCommentsBlock(String url, Integer eid, HttpClient client) {
        var singletonList = new ArrayList<String>();
        int retries = 0;
        try {
            tryConnect(client, url, singletonList, -1, retries);
        } catch (Exception e) {
            // hand-made craft retry policy
            retries++;
            if (retries > 3) {
                log.error("Failed at page %s, trying to save what is left");
                return singletonList.get(0);
            }
            tryConnect(client, url, singletonList, -1, retries);
        }
        return singletonList.get(0);
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
                            bar.show();
                            String recordUrl = String.format(RECORD_TEMPLATE, diary.getLogin(), r.getEid());
                            String commentsBlock = retrieveCommentsBlock(recordUrl, r.getEid(), cachedClient);
                            new Parser().injectComments(r, commentsBlock);
                        }
                );
    }

    private void tryConnect(HttpClient client, String url, ArrayList<String> strings, int curIndex, int retry) {
        try {
            HttpResponse<String> send = client.send(getRequest(url), (r) -> handle(r, curIndex, retry));
            strings.add(send.body());
            Thread.sleep(800);
        } catch (IOException | InterruptedException e) {
            log.error("Error, will retry if possible: ", e);
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
            int curIndex,
            int retry
    ) {
        if (responseInfo.statusCode() != 200) {
            log.info(String.format("Code %s, page %s, iteration %s", responseInfo.statusCode(), curIndex, retry));
        }
        return HttpResponse.BodySubscribers.ofString(Charset.defaultCharset());
    }

    private HttpRequest.BodyPublisher authParams(String user, String password) {
        return HttpRequest.BodyPublishers.ofString(String.format("login=%s&password=%s", user, password));
    }

}
