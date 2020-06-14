package in.hauu.diary;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Data
public class Page {

    public final List<Record> records;
}
