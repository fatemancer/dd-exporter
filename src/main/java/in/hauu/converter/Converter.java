package in.hauu.converter;

import in.hauu.diary.Comment;
import in.hauu.diary.Page;
import in.hauu.diary.Record;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Converter {

    public Page responseToPage(int pageNumber, String response) {
        List<Record> records = new ArrayList<>();

        Document document = Jsoup.parse(response);
        Elements recordsForPage = document.body()
                .getElementsByClass("layout-wrapper")
                .get(0)
                .getElementsByClass("layout-main")
                .get(0)
                .getElementsByTag("article");

        recordsForPage.forEach(r -> records.add(responseToRecord(r)));
        return new Page(records);
    }

    private Record responseToRecord(Element element) {
        Elements meta =
                element.getElementsByTag("header").first().getElementsByClass("meta").first().getElementsByTag(
                        "span"
                );
        String time = meta.first().getElementsByTag("time").first().attr("datetime");
        String privacy = meta.first().getElementsByTag("span").last().text();
        Elements titleAndLink = element.getElementsByTag("header").first().getElementsByClass("title");
        String header = extractTitle(titleAndLink);
        String text = element.getElementsByTag("section").html();
        String parsedText = element.getElementsByTag("section").text();
        String music = extractExtra(element, "Музыка");
        String mood = extractExtra(element, "Состояние");
        String tags = extractExtra(element, "Теги");
        String eid = extractEid(element);
        String commentNumber = extractComments(element);
        var dto = new RecordDto(
                eid, time, header, text, privacy, music, mood, commentNumber, tags
        );
        return new Record(dto);
    }

    private String extractEid(Element element) {
        return element.getElementsByTag("footer").last().getElementsByClass("controls").first().children().get(1).attr(
                "href").split("/")[3];
    }

    private String extractComments(Element element) {
        String text = element.getElementsByTag("footer")
                .last()
                .getElementsByClass("controls")
                .first()
                .children()
                .get(1)
                .text();
        if (text.equals("К записи") || text.equals("Комментировать")) {
            return "0";
        } else {
            return text.split(" ")[0];
        }
    }

    private String extractExtra(Element root, String key) {
        Elements maybeMusic = root.getElementsByTag("footer").last().getElementsByTag("div");
        for (var div : maybeMusic) {
            if (div.html().contains("<strong>" + key)) {
                // get child(1) gives array out of bounds, bug ?
                return div.html().split("/strong>")[1];
            }
        }
        return null;
    }

    private String extractTitle(Elements titleAndLink) {
        if (titleAndLink.first() == null) {
            return "[Запись без заголовка]";
        } else {
            return titleAndLink.first().text();
        }
    }

    public Collection<Comment> responseToComments(String commentsBlock) {
        Document document = Jsoup.parse(commentsBlock);
        Elements htmlComments = document.body()
                .getElementsByClass("layout-wrapper")
                .get(0)
                .getElementsByClass("layout-main")
                .get(0)
                .getElementsByClass("commentsSection")
                .get(0)
                .getElementsByClass("comment");

        List<Comment> comments = htmlComments.stream().map(this::parseComment).collect(Collectors.toList());
        return comments;
    }

    private Comment parseComment(Element hc) {
        String author = hc.getElementsByClass("author").text();
        String content = hc.getElementsByClass("content").text();
        boolean isPersonal = hc.getElementsByAttributeValue("title", "Такой коммент видят только автор записи и автор коммента").size() == 1;
        return new Comment(author, content, isPersonal);
    }
}
