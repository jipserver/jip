package jip.jobs;

import com.google.gson.Gson;
import com.google.inject.Inject;
import jip.JipEnvironment;
import jip.tools.DefaultExecuteEnvironment;
import jip.tools.ExecuteEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain text job store for pipeline jobs.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class FileJobStore implements JobStore{
    /**
    * The logger
    */
    private static Logger log = LoggerFactory.getLogger(FileJobStore.class);

    /**
     * The directory used to store files
     */
    private File storageDirectory;

    /**
     * Create a new instance of the job store.
     * The given directory must exist and must be writable
     * by the process running the job store. In addition,
     * the file system hosting the directory must support
     * file locks.
     *
     * @param storageDirectory the storage directory
     */
    public FileJobStore(File storageDirectory) {
        this.storageDirectory = storageDirectory;
        if(!this.storageDirectory.exists()){
            if(!this.storageDirectory.mkdirs()){
                log.error("Unable to create job store directory");
            }
        }
    }

    /**
     * Create a new job store and read the storage location
     * from the configuration. The key checked is
     * <pre>
     *     <code>
     *         jip.storage.directory
     *     </code>
     * </pre>
     * @param jipEnvironment
     */
    @Inject
    public FileJobStore(JipEnvironment jipEnvironment){
        this(new File((String) jipEnvironment.getConfiguration().flatten().get("jip.storage.directory")));
    }

    @Override
    public void save(PipelineJob pipelineJob) {
        FileLock lock = null;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            RandomAccessFile rw = new RandomAccessFile(new File(storageDirectory, pipelineJob.getId()+".job"), "rw");
            channel = rw.getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.lock();
            Map<String, Object> data = DefaultPipelineJob.toMap(pipelineJob);
            Gson gson = new Gson();
            rw.writeChars(gson.toJson(data));
        } catch (Exception e) {
            log.error("Error while writing job file", e.getMessage());
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

    }

    @Override
    public void delete(PipelineJob pipelineJob) {

    }

    @Override
    public void archive(PipelineJob pipelineJob) {

    }

    @Override
    public PipelineJob get(String id) {
        FileLock lock = null;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            File file = new File(storageDirectory, id + ".job");
            if(!file.exists()) return null;

            RandomAccessFile rw = new RandomAccessFile(file, "rw");
            channel = rw.getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.lock();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Gson gson = new Gson();
            String content = map.asCharBuffer().toString();
            HashMap jobMap = gson.fromJson(content, HashMap.class);
            return new DefaultPipelineJob(jobMap);
        } catch (Exception e) {
            log.error("Error while writing job file", e.getMessage());
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
    }

    @Override
    public Iterable<PipelineJob> list(boolean archived) {
        return null;
    }

}
