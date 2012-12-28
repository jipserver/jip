package jip.jobs;

import com.google.inject.Inject;
import groovy.util.ConfigObject;
import jip.JipConfiguration;
import jip.JipEnvironment;
import jip.plugin.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map;

/**
 *
 * ID service that provides ids and stores the information about
 * the next id in a file. A Lock is acquired to ensure process
 * safety and the service can only be run on a file system
 * that allows to lock files.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
public class FileIdService implements IdService{
    /**
    * The logger
    */
    private static Logger log = LoggerFactory.getLogger(FileIdService.class);

    /**
     * The environment to be able to identify
     * configuration
     */
    private JipEnvironment environment;

    /**
     * Path to the id file
     */
    private File idfile;

    /**
     * Initialize a new service that uses the JIP environment to identify
     * the storage location
     *
     * @param environment the environment
     */
    @Inject
    public FileIdService(JipEnvironment environment) {
        if(environment == null){
            throw new NullPointerException("NULL environment not permitted");
        }
        this.environment = environment;
    }

    /**
     * Create an instance based on the given file.
     *
     * @param idfile the id file
     */
    public FileIdService(File idfile ) {
        this.idfile = idfile;
    }

    @Override
    public synchronized String next() {
        if (idfile == null) {
            Map<String, Object> cfg = environment.getConfiguration();
            Object path = JipConfiguration.get(cfg, "jobs", "idservice", "file");
            if (path == null) {
                log.error("ID service id file is not defined !");
                throw new RuntimeException("ID service id file is not defined !");
            }
            if(!path.toString().startsWith("/")){
                path = new File(environment.getJipHome(true), path.toString()).getAbsolutePath();
            }
            idfile = new File(path.toString());
            if (!idfile.getParentFile().exists()) {
                if (!idfile.getParentFile().mkdirs()) {
                    log.error("Unable to create storage file parent in {}", idfile.getParentFile().getAbsolutePath());
                    throw new RuntimeException("Unable to create storage file parent in " + idfile.getParentFile().getAbsolutePath());
                }
            }
        }
        FileLock lock = null;
        long nextId = 0;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            RandomAccessFile rw = new RandomAccessFile(idfile, "rw");
            channel = rw.getChannel();

            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.lock();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
            LongBuffer longBuffer = map.asLongBuffer();
            nextId = longBuffer.get(0);
            longBuffer.put(0, nextId+1);
        } catch (Exception e) {
            log.error("Error while reading next id {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // Release the lock
            if (lock != null) {
                try {lock.release();} catch (IOException ignore) {}
            }
            // Close the file
            if (channel != null) {
                try {channel.close();} catch (IOException ignore) {}
            }

        }
        return Long.toString(nextId);
    }

    private ConfigObject get(ConfigObject source, String id){
        return (ConfigObject) source.get(id);
    }
}
