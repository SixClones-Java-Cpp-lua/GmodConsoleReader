import com.pty4j.PtyProcessBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static Process serverProcess;
    private static BufferedWriter printer;

    public static void main(String[] args) {
        scanConsole();
        start(args);
    }

    /**
     * Scan the cmd for user input
     */
    private static void scanConsole() {
        new Thread(() -> {
            try {
                String line;
                BufferedReader sys = new BufferedReader(new InputStreamReader(System.in));
                while ((line = sys.readLine()) != null) {
                    if (serverProcess.isAlive()) {
                        write(line);
                    } else {
                        log("The garry's mod server is off, stopping the java process");
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Send a command to the garry's mod server.
     *
     * @param data The command.
     */
    private static void write(String data) {
        try {
            if (printer != null) {
                printer.write(data + "\n");
                printer.flush();
                log(">" + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use to start de garry's mod server
     *
     * @param cmd the arg use to start the process
     */
    private static void start(String[] cmd) {
        try {
            log("Gmod process is going to start");

            serverProcess = new PtyProcessBuilder(cmd)
                    //.setDirectory("/home/jeu/Steam/Gmod/") // use the dir where the jar is
                    .start();

            printer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
            scan(new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));

            processExitDetector();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scan the given buffer in a new thread
     *
     * @param br the buffer
     */
    private static void scan(BufferedReader br) {
        new Thread(() -> {
            try {
                while (serverProcess.isAlive()) {
                    String line;
                    if ((line = br.readLine()) != null) {
                        log(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * The function wait until the process stop
     */
    private static void processExitDetector() {
        new Thread(() -> {
            if (serverProcess.isAlive()) {
                try {
                    serverProcess.waitFor();
                    log("Gmod has stopped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    static SimpleDateFormat date = new SimpleDateFormat("[hh:mm:ss.SSS]");

    /**
     * log a message
     *
     * @param s the message
     */
    public static void log(String s) {
        System.out.println(date.format(new Date(System.currentTimeMillis())) + s);
    }

}