import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments required. Each argument must be a path "
                    + "to a jar file.");
        }
        List<Process> ps = new ArrayList<>();
        for (String path : args) {
            Process p = new ProcessBuilder("java", "-jar", path).inheritIO().start();
            ps.add(p);
            System.out.println("Startet " + path + " with pid " + p.pid());
        }
        ps.parallelStream().forEach(t -> {
            try {
                t.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
