package in.hauu.writer;

import in.hauu.diary.Comment;
import in.hauu.diary.Diary;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Formatter {

    private static String HEAD;
    private static String TAIL;
    private static String TEMPLATE_ARTICLE;
    private static String FOOTER;
    private static String COMMENTS;
    private static String COMMENT_ITEM;

    static {
        HEAD = UserFileSystem.getResourceAsString("head.html");
        TAIL = UserFileSystem.getResourceAsString("tail.html");
        TEMPLATE_ARTICLE = UserFileSystem.getResourceAsString("template.html");
        FOOTER = UserFileSystem.getResourceAsString("footer_block.html");
        COMMENTS = UserFileSystem.getResourceAsString("comment_block.html");
        COMMENT_ITEM = UserFileSystem.getResourceAsString("comment_item_block.html");
    }

    public static void checkIfExists() {
        // static must fire and successfully load resources
    }

    public Stream<String> format(Diary diary) {
        return diary.getFlatRecords().map(r ->
                MessageFormat.format(
                        TEMPLATE_ARTICLE,
                        r.getDateTime(),
                        r.getHeader(),
                        r.getText(),
                        injectComments(r.getComments()),
                        injectFooter(r.getMusic(), r.getMood(), r.getTags())
                ));
    }

    private Object injectFooter(String music, String mood, String tags) {
        String total = "";
        if (music != null) {
            total += wrap(music, "Музыка: ");
        }
        if (mood != null) {
            total += wrap(mood, "Состояние: ");
        }
        if (tags != null) {
            total += wrap(tags, "Теги: ");
        }
        return total;
    }

    private String injectComments(List<Comment> comments) {
        if (comments.isEmpty()) {
            return "";
        } else {
            return String.format(COMMENTS,
                    comments.size(),
                    comments.stream().map(this::wrapComment).collect(Collectors.joining())
            );
        }
    }

    private String wrapComment(Comment c) {
        String privateLine = "<span style=\"color:E50000;size:50%;align:center\"> private! </span>";
        if (c.isPersonal()) {
            return String.format(COMMENT_ITEM, c.getAuthor(), privateLine, c.getContent());
        } else {
            return String.format(COMMENT_ITEM, c.getAuthor(), "", c.getContent());
        }
    }

    private String wrap(String content, String name) {
        return String.format(FOOTER, name, content);
    }

    String getHead() {
        return HEAD;
    }

    String getTail() {
        return TAIL;
    }
}
