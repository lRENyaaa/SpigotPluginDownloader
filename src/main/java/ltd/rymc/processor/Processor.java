package ltd.rymc.processor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Processor {

    private static final Logger SERVER_LOGGER = LogManager.getLogger("Output-IOStream");
    private static final Logger PROCESSOR = LogManager.getLogger("Processor");

    private static final String ENCODING = getConsoleEncoding();

    private final ProcessBuilder builder;
    private String encoding = ENCODING;

    public Processor(String... args) {
        builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
    }

    public Processor(File workDir, String... args) {
        this(args);
        builder.directory(workDir);
    }

    public void setEncoding(String encoding){
        this.encoding = encoding;
    }

    public int run() {
        try {
            Process process = builder.start();
            Thread output = initOutputThread(process, encoding);
            Thread input = initInputThread(process, encoding);

            int result = process.waitFor();

            input.interrupt();
            output.join();
            input.join();

            return result;
        } catch (IOException | InterruptedException e) {
            PROCESSOR.log(Level.ERROR, e);
            return -1;
        }

    }

    public int runWithoutInput() {
        try {
            Process process = builder.start();
            Thread output = initOutputThread(process, encoding);
            int result = process.waitFor();
            output.join();
            return result;
        } catch (IOException | InterruptedException e) {
            PROCESSOR.log(Level.ERROR, e);
            return -1;
        }
    }

    public int runWithNoStream() {
        try {
            return builder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            return -1;
        }
    }

    private static Thread initOutputThread(Process process, String encoding) throws UnsupportedEncodingException {
        BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), encoding));

        Thread thread = new Thread(() -> {
            String line;
            try {
                while ((line = processOutputReader.readLine()) != null) {
                    SERVER_LOGGER.info(line);
                }
            } catch (IOException e) {
                PROCESSOR.log(Level.WARN, e);
            }
        });

        thread.start();
        return thread;
    }

    private static Thread initInputThread(Process process, String encoding) throws UnsupportedEncodingException {
        ProcessorConsole console = new ProcessorConsole(process, encoding);
        Thread thread = new Thread(console::start);
        thread.start();
        return thread;
    }

    private static String getConsoleEncoding(){
        String consoleEncoding = System.getProperty("sun.jnu.encoding");
        return consoleEncoding == null ? Charset.defaultCharset().name() : consoleEncoding;
    }
}
