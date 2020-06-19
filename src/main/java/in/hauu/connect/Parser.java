package in.hauu.connect;

import in.hauu.converter.Converter;
import in.hauu.diary.Comment;
import in.hauu.diary.Diary;
import in.hauu.diary.Page;
import in.hauu.diary.Record;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class Parser {

    private final Converter converter = new Converter();

    public Diary parse(String login, HashMap<String, String> meta, List<String> strings) {
        ArrayList<Page> pages = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            pages.add(converter.responseToPage(i, strings.get(i)));
        }
        return new Diary(login, meta, pages);
    }

    public HashMap<String, String> getMeta(String page) {
        HashMap<String, String> meta = new HashMap<>();
        Document document = Jsoup.parse(page);
        meta.put("lastDiaryIndex", getLastDiaryIndex(document));
        meta.put("colorText", getTextColor(document));
        meta.put("colorBack", getBackColor(document));
        return meta;
    }

    // todo: parse css (download separately?)
    private String getBackColor(Document document) {
        return "#FFFFFF";
    }

    private String getTextColor(Document document) {
        return "#000000";
    }

    private String getLastDiaryIndex(Document document) {
        return document
                .body()
                .getElementsByClass("pager")
                .first()
                .getElementsByTag("a")
                .last()
                .text();
    }

    void injectComments(Record r, String commentsBlock) {
        List<Comment> mutableComments = r.getComments();
        mutableComments.addAll(converter.responseToComments(commentsBlock));
    }
}
