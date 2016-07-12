package util.tools.inputOutput;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * An abstraction representing input/output device
 * Created by evgheni.s on July 11, 2016.
 */
public abstract class InputOutputDevice {

    public abstract InputOutputDevice printf(String fmt, Object... params) throws ConsoleException;

    public abstract String readLine() throws ConsoleException;

    public abstract char[] readPassword() throws ConsoleException;

    public abstract Reader reader() throws ConsoleException;

    public abstract PrintWriter writer() throws ConsoleException;
}
