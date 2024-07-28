package ltd.rymc.downloader;

import ltd.rymc.plugin.PluginDownloader;
import ltd.rymc.processor.Processor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    private static final Logger MAIN = LogManager.getLogger("Main");

    public static void main(String[] args) throws IOException, URISyntaxException {
        MAIN.info("Initializing...");
        String command = getCommand();
        if (command == null) {
            MAIN.error("This program needs npm to run!");
            return;
        }
        MAIN.info("Initialized!");

        String jarPath = getSelfPath();
        MAIN.info("Path: {}", jarPath);
        MAIN.info("Is running in jar file: {}", isRunningInJar());

        File downloader = getDownloaderPath();
        if (downloader == null) {
            MAIN.fatal("The runtime library is missing!");
            return;
        }

        ensureInstalled(downloader, command);

        PluginDownloader pluginDownloader = new PluginDownloader(Integer.parseInt(args[0]));
        Processor start = new Processor(downloader, command, "start", downloader.getParent(), "plugin-" + args[0] + ".jar", pluginDownloader.getDownloadURL());
        start.setEncoding(StandardCharsets.UTF_8.name());
        int result = start.runWithoutInput();
        MAIN.info("Exit: {}", result);
    }

    private static void ensureInstalled(File downloader, String command){
        if (!downloader.toPath().resolve("node_modules").toFile().exists()) {
            Processor install = new Processor(downloader, command, "install");
            install.setEncoding(StandardCharsets.UTF_8.name());
            install.runWithoutInput();
        }
    }

    private static String getCommand() {
        if (commandExist("npm")) return "npm";
        if (commandExist("npm.cmd")) return "npm.cmd";
        return null;
    }

    private static boolean commandExist(String... args) {
        Processor processor = new Processor(args);
        return processor.runWithNoStream() != -1;
    }

    private static String getSelfPath() throws URISyntaxException {
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        Pattern pattern = Pattern.compile("^/[A-Z]:/");
        return pattern.matcher(jarPath).find() ? jarPath.substring(1) : jarPath;
    }

    private static boolean isRunningInJar() {
        try {
            URL resource = Main.class.getResource("/" + Main.class.getName().replace('.', '/') + ".class");
            if (resource == null) return false;
            return resource.toString().startsWith("jar:");
        } catch (Exception e) {
            return false;
        }
    }

    private static File getDownloaderPath() throws URISyntaxException {
        String jarPath = getSelfPath();
        Path basePath = Paths.get(jarPath);
        String downloadFolder;
        if (isRunningInJar()) {
            downloadFolder = ".downloader";

            try {
                extractDownloaderFIle(basePath, basePath.getParent().resolve(downloadFolder));
            } catch (IOException e) {
                MAIN.log(Level.ERROR, e);
                return null;
            }

            // .../<baseDir>/self.jar -> .../<baseDir>
            basePath = basePath.getParent();


        } else {
            downloadFolder = "downloader";

            // .../<baseDir>/build/classes/java/main/ -> .../<baseDir>
            basePath = basePath.getParent().getParent().getParent().getParent();
        }

        File downloader = basePath.resolve(downloadFolder).toFile();
        MAIN.info("Downloader: {}", downloader.toString());
        if (!downloader.exists()) return null;
        return downloader;
    }

    private static void extractDownloaderFIle(Path path, Path outputFolder) throws IOException {

        ZipFile zipFile = new ZipFile(path.toFile());
        Enumeration<? extends ZipEntry> entrys = zipFile.entries();
        while (entrys.hasMoreElements()) {
            ZipEntry entry = entrys.nextElement();
            if (entry.isDirectory()) continue;

            String[] paths = entry.getName().split("/");
            if (!paths[0].equals("downloader")) {
                MAIN.debug("Skipping: {}", entry.getName());
                continue;
            }

            Path entryOutput = outputFolder;
            for (String filePath : paths) {
                entryOutput = outputFolder.resolve(filePath);
            }

            MAIN.info("Extracting: {}", entryOutput.toString());

            File dir = entryOutput.getParent().toFile();
            if (!dir.exists()) if (!dir.mkdirs()) MAIN.warn("Failed to create folder: {}", dir.toString());

            BufferedInputStream input = new BufferedInputStream(zipFile.getInputStream(entry));
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(entryOutput.toString()));

            int len;
            byte[] bytes = new byte[2048];
            while ((len = input.read(bytes)) != -1) {
                output.write(bytes, 0, len);
            }

            output.close();
            input.close();
        }

    }


}