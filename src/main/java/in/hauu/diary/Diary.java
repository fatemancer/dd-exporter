package in.hauu.diary;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Data
public class Diary {

    final String login;
    final List<Page> pages;

    // todo: parse page for colors
    Color background = new Color("white");
    Color text = new Color("black");

    public Stream<Record> getFlatRecords() {
        return this.pages.stream().flatMap(p -> p.getRecords().stream());
    }
}
