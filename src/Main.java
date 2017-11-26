import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Main {
    private static boolean running = true;
    private static Process serverProcess;
    private static BufferedWriter bw;


    public static void main(String[] args) {
        scanConsole();
        start();
        new Thread(() -> {
            while (running) {
                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "isAlive :" + serverProcess.isAlive());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod is already start");
                            }
                            break;
                        case "stopServer":
                            if (serverProcess .isAlive()) {
                                stop();
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod is already stop");
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
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to stop");
            bw.write("exit\n") ;
            bw.flush();
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "write done");
            if (!serverProcess.waitFor(20, TimeUnit.SECONDS)) {
                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to be destroy");
                serverProcess.destroy();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to start");
            serverProcess = new ProcessBuilder().directory(new File("/home/jeu/Steam/Gmod"))
                    .command(Arrays.asList(
                            "./srcds_run-backup -game garrysmod +maxplayers 5 +map rp_frenchtownsend_v4 +host_workshop_collection 1206592668".split(" ")
                    ))
                    .start();

            bw = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));

            scanStream(new BufferedReader(new InputStreamReader(serverProcess.getErrorStream())));
            scanStream(new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));
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
                    System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "[Process] " + line);
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
                    System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod has stoped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}