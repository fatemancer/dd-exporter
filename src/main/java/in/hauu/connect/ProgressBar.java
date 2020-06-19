package in.hauu.connect;

public class ProgressBar {

    private String bar;

    private int max;
    private int current;
    private int step;
    private String name;

    public ProgressBar(String name, int max) {
        this.name = name;
        this.max = max;
        this.current = 0;
        this.step = 0;
        bar = String.format("%s, %s/%s: [%s]", name, current, max, ".".repeat(78));
    }

    public void next() {
        current++;
        if (current / ((double) max) / (1.0 / 78) > step) {
            step++;
            bar = String.format("%s, %s/%s: [%s%s]", name, current, max, "X".repeat(step), ".".repeat(78-step));
        }
    }

    public void show() {
        System.err.print(bar + "\r");
    }
}
