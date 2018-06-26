import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static File logFile = new File("/home/jeu/Steam/Gmod/garrysmod/log/gmod.log");
    //private static File clientError = new File("/home/jeu/Steam/Gmod/garrysmod/clientside_errors.txt");
    //private static File serverError = new File("/home/jeu/Steam/Gmod/garrysmod/lua_errors_server.txt");
    private static File commandFile = new File("/home/jeu/Steam/Gmod/garrysmod/lua/autorun/server/sv_commande_ne_pas_modifer.lua");
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
        if (logFile.exists()) {
            logFile.delete();
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
    }

    /**
     * Use to send commande to the garry's mod server.
     *
     * @param data The commande to send. Warming : with the method, some command will not be execute by the garry's mod server
     */
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
    }

    /**
     * Use to start de garry's mod server
     */
    private static void start() {
        try {
            System.out.println(new SimpleDateFormat("[hh:mm:ss.SSS]").format(new Date(System.currentTimeMillis())) + "Gmod's process is going to start");
            serverProcess = new ProcessBuilder().directory(new File("/home/jeu/Steam/Gmod"))
                    .command(Arrays.asList(
                            "strace -o /home/jeu/Steam/Gmod/garrysmod/log/gmod.log -s 1024 -e write ./srcds_run -game garrysmod +maxplayers 15 +map rp_rockford_karnaka +host_workshop_collection 1382039356 -norestart".split(" ")
                    ))
                    .start();

            waitAndScan(logFile);

            processExitDetector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait to the file to be create and scan everything that write in
     *
     * @param f The file to wait and scan
     */
    private static void waitAndScan(File f) {
        new Thread(() -> {
            while (running) {
                if (f.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        String line;
                        while ((line = br.readLine()) != null) {
                            Pattern needToScan = Pattern.compile("^write\\([123],.*");
                            if (needToScan.matcher(line).matches()) {
                                Pattern getLength = Pattern.compile("(\\d+)$");
                                Matcher getLengthMatcher = getLength.matcher(line);
                                if (getLengthMatcher.find()) {
                                    int a = line.length();
                                    line = repacleSpecialCharacter(line);

                                    int aAjouter = count("\\", line) - ((a - line.length())/7) - count("\\\\", line);

                                    Pattern p = Pattern.compile("^write\\([123], \"(.{" + (Integer.parseInt(getLengthMatcher.group(1)) + aAjouter) + "})\", \\d+\\)\\s+= \\d+$");
                                    Matcher m = p.matcher(line);
                                    if (m.find()) {
                                        String log = m.group(1);
                                        for (String l : log.split("\\\\n")) {
                                            System.out.println(l.replace("\\t", "    ").replace("\\\"", "\"").replace("\"\\", "\"").replace("\\\\", "\\"));
                                        }
                                    }
                                }
                            }
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
     * replace all special character like \303\247 (meaning 'ç') or \303\251 (meaning 'é')
     *
     * @param line the string where you need to replace special character
     * @return the line where special character were replaced
     */
    private static String repacleSpecialCharacter(String line) {
        line = line.replaceAll(Pattern.quote("\\") + "303" + Pattern.quote("\\") + "247", "ç");
        line = line.replaceAll(Pattern.quote("\\") + "303" + Pattern.quote("\\") + "251", "é");
        return line;
    }

    /**
     * Search a string in a other string
     * @param s
     * the string to search
     * @param line
     * the other string
     * @return
     * return the number of apparition of s in line
     */
    private static int count(String s, String line) {
        int result = 0;
        for (int i = s.length(); i < line.length(); i++) {
            if (s.equalsIgnoreCase(line.substring(i - s.length(), i))) {
                result++;
            }
        }
        return result;
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