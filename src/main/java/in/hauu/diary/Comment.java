package in.hauu.diary;

import lombok.Value;

@Value
public class Comment {

    String author;
    String content;
    boolean personal;
}
