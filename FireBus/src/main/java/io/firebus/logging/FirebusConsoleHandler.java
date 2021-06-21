package io.firebus.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FirebusConsoleHandler extends ConsoleHandler {

	
    public void publish(LogRecord record)
    {
        try {
            String message = getFormatter().format(record);
            if (record.getLevel().intValue() >= Level.WARNING.intValue())
            {
                System.err.write(message.getBytes());                       
            }
            else
            {
                System.out.write(message.getBytes());
            }
        } catch (Exception exception) {
            
        }

    }
}
