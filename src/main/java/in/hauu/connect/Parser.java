package in.hauu.connect;

import in.hauu.converter.Converter;
import in.hauu.diary.Comment;
import in.hauu.diary.Diary;
import in.hauu.diary.Page;
import in.hauu.diary.Record;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class Parser {

    private final Converter converter = new Converter();

    public Diary parse(String login, List<String> strings) {
        ArrayList<Page> pages = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            pages.add(converter.responseToPage(i, strings.get(i)));
        }
        return new Diary(login, pages);
    }

    public int getLastDiaryIndex(String page, String link) {
        try {
            return Integer.parseInt(Jsoup.parse(page)
                    .body()
                    .getElementsByClass("pager")
                    .first()
                    .getElementsByTag("a")
                    .last()
                    .text());
        } catch (Exception e) {
            log.error(String.format("Unable to get latest page from link, check page %s", link));
            throw new RuntimeException(e);
        }
    }

    public void injectComments(Record r, String commentsBlock) {
        List<Comment> mutableComments = r.getComments();
        mutableComments.addAll(converter.responseToComments(commentsBlock));
    }
}
