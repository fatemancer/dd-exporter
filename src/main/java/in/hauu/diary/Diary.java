package in.hauu.diary;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
@Data
public class Diary {

    final String login;
    final List<Page> pages;

    // todo: parse page for colors
    Color background = new Color("white");
    Color text = new Color("black");
}
