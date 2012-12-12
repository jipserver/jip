package jip.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;

/**
 * Read from input stream and delegate to output
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class InputStreamGlobber implements Callable<Void>{
    private InputStream inputStream;
    private OutputStream outputStream;

    public InputStreamGlobber(InputStream inputStream) {
        this(inputStream, null);
    }

    public InputStreamGlobber(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream == null ? System.out : outputStream;
    }

    @Override
    public Void call() throws Exception {
        byte[] buffer = new byte[4096];
        int l = 0;
        while((l = inputStream.read(buffer)) >= 0){
            outputStream.write(buffer, 0, l);
        }
        if(outputStream != System.out && outputStream != System.err){
            outputStream.close();
        }
        return null;
    }
}
