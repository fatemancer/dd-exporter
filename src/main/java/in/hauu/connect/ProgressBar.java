package in.hauu.connect;

import java.util.List;

public class ProgressBar {

    private final int max;
    private int current;
    private String name;
    private List<String> bar = List.of("/","-","\\");

    public ProgressBar(String name, int max) {
        this.name = name;
        this.max = max;
        this.current = 0;
    }

    public void next() {
        current++;
        System.err.print(String.format("%s: %s/%s %s%s", name, current, max, bar.get(current % 3), "\r"));
    }
}
