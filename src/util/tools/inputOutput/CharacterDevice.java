package util.tools.inputOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Character stream wrapper implementation
 * Created by evgheni.s on July 11, 2016.
 */

public class CharacterDevice extends InputOutputDevice {

    private final BufferedReader reader;
    private final PrintWriter writer;

    public CharacterDevice(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public CharacterDevice printf(String fmt, Object... params)
            throws ConsoleException {
        writer.printf(fmt, params);
        return this;
    }

    @Override
    public String readLine() throws ConsoleException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public char[] readPassword() throws ConsoleException {
        return readLine().toCharArray();
    }

    @Override
    public Reader reader() throws ConsoleException {
        return reader;
    }

    @Override
    public PrintWriter writer() throws ConsoleException {
        return writer;
    }
}