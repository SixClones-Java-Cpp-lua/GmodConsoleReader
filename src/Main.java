import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static File commandFile = new File("/home/jeu/Steam/Gmod/garrysmod/lua/autorun/server/sv_commande_ne_pas_modifer.lua");
    private static Process serverProcess;

    public static void main(String[] args) {
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


    /*
     * Use to scan th data the user give to the program

    private static void scanConsole() {
        new Thread(() -> {
            try {
                String line;
                BufferedReader sys = new BufferedReader(new InputStreamReader(System.in));
                while (running && (line = sys.readLine()) != null) {
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
                            running = false;
                            break;
                        default:
                            if (line.startsWith("write ")) {
                                write(line.replaceFirst("write ", "").replaceAll(" ", "\", \""));
                                System.out.println("> RunConsoleCommande(\"" + line.replaceFirst("write ", "").replaceAll(" ", "\", \"") + "\")");
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
    }*/

    /*
     * Use to send commande to the garry's mod server.
     *
     * @param data The commande to send. Warming : with the method, some command will not be execute by the garry's mod server

    private static void write(String data) {
        try {
            FileWriter fw = new FileWriter(commandFile);
            fw.write("RunConsoleCommand(\"" + data + "\")\n");
            fw.flush();
            fw.close();

            Thread.sleep(3000);

            fw = new FileWriter(commandFile);
            fw.write("-- NE PAS MODIFIER CE FICHIER"
                    + "\n-- fichier permettant a au programme java d'envoyer des commandes a gmod"
                    + "\nprint(\"module commande app java active\")");
            fw.flush();
            fw.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Use to start de garry's mod server
     */
    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to start");
            serverProcess = new ProcessBuilder("./srcds_run -game garrysmod +maxplayers 15 +map rp_rockford_karnaka +host_workshop_collection 1382039356 -norestart".split(" ")).redirectInput(ProcessBuilder.Redirect.INHERIT).directory(new File("/home/jeu/Steam/Gmod")).start();

            scan(new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));

            processExitDetector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void scan(BufferedReader br) throws IOException {
        while (serverProcess.isAlive()) {
            String line;
            if ((line= br.readLine()) != null) {
                System.out.println(line);
            }
        }
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}