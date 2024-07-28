package ltd.rymc.processor;

import net.minecrell.terminalconsole.SimpleTerminalConsole;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class ProcessorConsole extends SimpleTerminalConsole {

    private final PrintWriter processInputWriter;
    private final Process process;

    public ProcessorConsole(Process process, String encoding) throws UnsupportedEncodingException {
        this.process = process;
        this.processInputWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), encoding), true);

    }

    @Override
    protected boolean isRunning() {
        return process.isAlive();
    }

    @Override
    protected void runCommand(String command) {
        processInputWriter.println(command);
    }

    @Override
    protected void shutdown() {
        process.destroy();
    }
}
