package in.hauu.diary;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Data
@RequiredArgsConstructor
public class Diary {

    final String login;
    // let's add a touch of dynamic typing...
    final HashMap<String, String> meta;
    final List<Page> pages;

    public Stream<Record> getFlatRecords() {
        return Stream.concat(
                yieldDesciptionPage(),
                this.pages.stream().flatMap(p -> p.getRecords().stream())
        );
    }

    Stream<Record> yieldDesciptionPage() {
        return Stream.of(new Record(
                        0,
                        LocalDateTime.now(),
                        "Автоматический экспорт",
                        "Это автоматический экспорт вашего дневника на Darkdiary. <br>Он не требует интернета (только для подгрузки внешних ресурсов типа картинок или видео), размещён на одной странице и работает " +
                                "в любом современном браузере. Эти ресурсы будут показываться только в том случае, если до сих пор существуют.<br><br>" +
                                "<button onclick=\"Array.from(document.getElementsByTagName('details')).forEach" +
                                "(function(x) { if (!x.getAttribute('open')) { x.setAttribute('open','true') } else { x.removeAttribute('open')}})\">Раскрыть/скрыть все комментарии</button>\n" +
                                "<br><br> Поддержка и дополнительная информация о проекте - на странице <a href='https://github.com/fatemancer/dd-exporter'>репозитория</a>",
                        null,
                        null,
                        null
                ).setEnrichment(Record.Enrichment.NOT_NEEDED)
        );
    }
}

