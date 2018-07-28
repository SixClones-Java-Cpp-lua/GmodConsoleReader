import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static Process serverProcess;
    private static BufferedWriter printer;

    public static void main(String[] args) {
        scanConsole();
        start();
    }

    /**
     * Use to scan the data the user give to the program
     */
    private static void scanConsole() {
        new Thread(() -> {
            try {
                String line;
                BufferedReader sys = new BufferedReader(new InputStreamReader(System.in));
                while ((line = sys.readLine()) != null) {
                    switch (line) {
                        case "startServer":
                            if (!serverProcess.isAlive()) {
                                start();
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod is already start");
                            }
                            break;
                        case "stopServer":
                            if (serverProcess.isAlive()) {
                                write("_restart");
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod is already stop");
                            }
                            break;
                        case "stop":
                            if (serverProcess.isAlive()) {
                                write("_restart");
                            }
                            System.exit(0);
                            break;
                        default:
                            if (line.startsWith("write ")) {
                                write(line.replaceFirst("write ", ""));
                                System.out.println("> " + line.replaceFirst("write ", ""));
                            } else {
                                System.out.println("stopServer | startServer | stop | write [string]");
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Use to send commande to the garry's mod server.
     *
     * @param data The commande to send.
        */
    private static void write(String data) {
        try {
            if (printer != null) {
                printer.write(data + "\n");
                printer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use to start de garry's mod server
     */
    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to start");

            //String[] cmd = ("./srcds_run -game garrysmod +maxplayers 15 +map rp_rockford_karnaka +host_workshop_collection 1382039356 -norestart").split(" ");

            String[] cmd = new String[4];
            cmd[0] = "su";
            cmd[1] = "sixclones";
            cmd[2] = "-c";
            cmd[3] = "./main.exe -game garrysmod +maxplayers 15 +map rp_rockford_karnaka +host_workshop_collection 1382039356 -norestart";

            serverProcess = new ProcessBuilder(cmd).directory(new File("/home/jeu/Steam/Gmod/")).start();

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
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * The function wait until the process run
     */
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