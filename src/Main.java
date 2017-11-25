import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {
    private static boolean running = true;
    private static Process serverProcess;
    private static BufferedReader brNormal;
    private static BufferedReader brError;
    private static BufferedWriter bw;


    public static void main(String[] args) {
        scanConsole();
        start();
    }

    private static void scanConsole() {
        new Thread(() -> {
            try {
                String line;
                BufferedReader sys = new BufferedReader(new InputStreamReader(System.in));
                while (running && (line = sys.readLine()) != null) {
                    switch (line) {
                        case "startServer":
                            if (!serverProcess .isAlive()) {
                                start();
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod is already start");
                            }
                            break;
                        case "stopServer":
                            if (serverProcess .isAlive()) {
                                stop();
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod is already stop");
                            }
                            break;
                        case "stop":
                            if (serverProcess.isAlive()) {
                                stop();
                            }
                            running = false;
                            break;
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void stop() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod's process is going to stop");
            bw.write("exit\n");
            bw.flush();

            if (serverProcess.waitFor(5L, TimeUnit.MINUTES)) {
                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod's process is going to be destroy");
                serverProcess.destroy();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod's process is going to start");
            serverProcess = new ProcessBuilder().directory(new File("/home/jeu/Steam/Gmod"))
                    .command(Arrays.asList("./srcds_run",
                            "-game",
                            "garrysmod",
                            "+maxplayers",
                            "5"))
                    .start();

            brNormal = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
            brError = new BufferedReader(new InputStreamReader(serverProcess.getErrorStream()));
            bw = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));

            scanStream(brNormal);
            scanStream(brError);
            processExitDetector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void scanStream(BufferedReader br) {
        new Thread(() -> {
            String line;
            try {
                while (serverProcess.isAlive() && (line = br.readLine()) != null) {
                    System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "[Process] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void processExitDetector() {
        new Thread(() -> {
            if (serverProcess.isAlive()) {
                try {
                    serverProcess.waitFor();
                    System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]") + "Gmod has stoped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}


