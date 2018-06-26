import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Main {

    private static File logDir = new File("/home/jeu/Steam/Gmod/garrysmod/log/");
    private static File logFile;
    private static File clientError = new File("/home/jeu/Steam/Gmod/garrysmod/clientside_errors.txt");
    private static File serverError = new File("/home/jeu/Steam/Gmod/garrysmod/lua_errors_server.txt");
    private static File commandFile = new File("/home/jeu/Steam/Gmod/garrysmod/addons/sixclonesjavaprocess/lua/autorun/server/sv_commande_ne_pas_modifer.lua\"");
    private static boolean running = true;
    private static Process serverProcess;

    public static void main(String[] args) {
        scanConsole();
        if (!commandFile.exists()) {
            try {
                commandFile.createNewFile();
                FileWriter fw = new FileWriter(commandFile);
                fw.write("-- NE PAS MODIFIER CE FICHIER"
                        + "\n-- fichier permettant a au programme java d'envoyer des commandes a gmod"
                        + "\nprint(\"module commande app java active\")");
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        start();
    }


    /**
     * Use to scan th data the user give to the program
     */
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
                                write("_restart");
                            } else {
                                System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod is already stop");
                            }
                            break;
                        case "stop":
                            if (serverProcess.isAlive()) {
                                write("_restart");
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

    /**
     * Use to send commande to the garry's mod server.
     *
     * @param data
     * The commande to send. Warming : with the method, some command will not be execute by the garry's mod server
     *
     */
    private static void write(String data) {
        try {
            FileWriter fw = new FileWriter(commandFile);
            fw.write("RunConsoleCommand(\"" + data + "\")\n");
            fw.flush();
            fw.close();

            Thread.sleep(5000);

            fw = new FileWriter(commandFile);
            fw.write("-- NE PAS MODIFIER CE FICHIER"
                    + "\n-- fichier permettant a au programme java d'envoyer des commandes a gmod"
                    + "\nprint(\"module commande app java active\")");
            fw.flush();
            fw.close();
						/*
-- NE PAS MODIFIER CE FICHIER
-- fichier permettant a au programme java d'envoyer des commandes a gmod
print("module commande app java active")
						 */
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use to start de garry's mod server
     */
    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to start");
            serverProcess = new ProcessBuilder().directory(new File("/home/jeu/Steam/Gmod"))
                    .command(Arrays.asList(
                            "./srcds_run -game garrysmod +maxplayers 5 -norestart".split(" ")
                    ))
                    .start();

            scanStream(new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));
            waitAndScan(clientError);
            waitAndScan(serverError);
            SimpleDateFormat format = new SimpleDateFormat("MMdd");
            logFile = new File(logDir.getAbsolutePath() + "L" + format.format(new Date(System.currentTimeMillis())) + "000.log");
            waitAndScan(logFile);

            processExitDetector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Use to get the output of a sub-process (the garry's mod server)
     *
     * @param br
     * The bufferedReader the function need to scan
     */
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

    /**
     * Wait to the file to be create and scan everything that write in
     *
     * @param f
     * The file to wait and scan
     */
    private static void waitAndScan(File f) {
        new Thread(() -> {
            while (running) {
                if (f.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "[Process] " + line);
                        }
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * The function wait until the process run and when the process stop delete clientError, serverError and logFile files
     */
    private static void processExitDetector() {
        new Thread(() -> {
            if (serverProcess.isAlive()) {
                try {
                    serverProcess.waitFor();
                    System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod has stoped");
                    if (clientError.exists()) {
                        clientError.delete();
                    }
                    if (serverError.exists()) {
                        serverError.delete();
                    }
                    if (logFile != null && logFile.exists()) {
                        logFile.delete();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}