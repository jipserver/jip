package jip.jobs;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Inject;
import jip.JipConfiguration;
import jip.JipEnvironment;
import jip.plugin.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

/**
 * Plain text job store for pipeline jobs.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
@Extension
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
     * the archive directory
     */
    private File archiveDirectory;

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
        this.archiveDirectory = new File(storageDirectory.getParentFile(), "archive");
        if(!this.storageDirectory.exists()){
            if(!this.storageDirectory.mkdirs()){
                log.error("Unable to create job store directory");
            }
        }
        if(!this.archiveDirectory.exists()){
            if(!this.archiveDirectory.mkdirs()){
                log.error("Unable to create job store archive directory");
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
        this(new File(getDirectory(jipEnvironment)));
    }

    /**
     * Resolve the storage directory
     *
     * @param jipEnvironment the environment
     * @return path the path to the storage directory
     */
    private static String getDirectory(JipEnvironment jipEnvironment) {
        String path = (String) JipConfiguration.get(jipEnvironment.getConfiguration(), "storage", "directory");
        if(!path.startsWith("/")){
            path = new File(jipEnvironment.getJipHome(true), path).getAbsolutePath();
        }
        return path;
    }

    @Override
    public void save(PipelineJob pipelineJob) {
        FileLock lock = null;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            RandomAccessFile rw = new RandomAccessFile(getJobFile(pipelineJob, false), "rw");
            channel = rw.getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.lock();
            Map<String, Object> data = DefaultPipelineJob.toMap(pipelineJob);
            Gson gson = new Gson();
            rw.writeChars(gson.toJson(data));
        } catch (Exception e) {
            log.error("Error while writing job file", e);
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

    private File getJobFile(PipelineJob pipelineJob, boolean archive) {
        return new File(archive ? archiveDirectory : storageDirectory, pipelineJob.getId()+".job");
    }

    @Override
    public void delete(PipelineJob pipelineJob) {
        File jobFile = getJobFile(pipelineJob, false);
        if(jobFile.exists()){
            jobFile.delete();
        }else{
            jobFile = getJobFile(pipelineJob, true);
            if(jobFile.exists()){
                jobFile.delete();
            }
        }
    }

    @Override
    public void archive(PipelineJob pipelineJob) {
        File jobFile = getJobFile(pipelineJob, false);
        if(jobFile.exists()){
            try {
                Files.move(jobFile, getJobFile(pipelineJob, true));
            } catch (IOException e) {
                throw new RuntimeException("Failed to move job file to archive !");
            }
        }
    }

    FileStoreJob lock(String id){
        if(id == null){
            throw new NullPointerException("NULL pipeline job id not permitted!");
        }
        FileLock lock = null;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            File file = getJobFile(id);
            if(!file.exists()){
                throw new RuntimeException("Storage file for job " + id + " not found !");
            }

            RandomAccessFile rw = new RandomAccessFile(file, "rw");
            channel = rw.getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.lock();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Gson gson = new Gson();
            String content = map.asCharBuffer().toString();
            HashMap jobMap = gson.fromJson(content, HashMap.class);
            FileStoreJob job = new FileStoreJob(jobMap);
            job.lock = lock;
            job.channel = channel;
            job.rw = rw;
            return job;
        } catch (Exception e) {
            log.error("Error while writing job file", e);
            // Release the lock
            if (lock != null) {
                try {lock.release();} catch (IOException ignore) {}
            }
            // Close the file
            if (channel != null) {
                try {channel.close();} catch (IOException ignore) {}
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine the storage file for a job
     *
     * @param id the job id
     * @return
     */
    private File getJobFile(String id) {
        File file = new File(storageDirectory, id + ".job");
        if(!file.exists()){
            file = new File(archiveDirectory, id + ".job");
        }
        return file;
    }

    @Override
    public PipelineJob get(String id) {
        FileLock lock = null;
        FileChannel channel = null;
        try {
            // Get a file channel for the file
            File file = getJobFile(id);
            if(!file.exists()){
                throw new RuntimeException("Job " + id + " not found !");
            }

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
            log.error("Error while writing job file", e);
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
        File[] files = (archived ? archiveDirectory : storageDirectory).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".job");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                long l1 = Long.parseLong(file.getName().substring(0, file.getName().length() - 4));
                long l2 = Long.parseLong(file2.getName().substring(0, file2.getName().length() - 4));
                if(l1 < l2) return 1;
                if(l1 == l2) return 0;
                return -1;
            }
        });
        return new JobIterable(files);
    }

    @Override
    public void setState(String pipelineId, String jobId, JobState state, String reason){
        FileStoreJob pipelineJob = lock(pipelineId);
        for (Job job1 : pipelineJob.getJobs()) {
            if(job1.getId().equals(jobId)){
                job1.setState(state);
                job1.setStateReason(reason);
                if(state == JobState.Running){
                    job1.getJobStats().setStartDate(new Date());
                    job1.getJobStats().setEndDate(null);
                }else if(state == JobState.Submitted ||state == JobState.Queued ){
                    job1.getJobStats().setStartDate(null);
                    job1.getJobStats().setEndDate(null);
                }else if(state.isDoneState()){
                    job1.getJobStats().setEndDate(new Date());
                }
                pipelineJob.saveAndRelease();
                break;
            }
        }

    }

    @Override
    public void save(Job job){
        FileStoreJob pipelineJob = lock(job.getPipelineId());
        int index = 0;
        for (Job job1 : pipelineJob.getJobs()) {
            if(job1.getId().equals(job.getId())){
                pipelineJob.getJobs().set(index, job);
                pipelineJob.saveAndRelease();
                return;
            }
            index++;
        }
    }


    /**
     * Internal job store pipeline job
     * that hold a reference to the lock
     */
    private class FileStoreJob extends DefaultPipelineJob{
        /**
         * The file lock
         */
        FileLock lock;
        /**
         * The file channel
         */
        FileChannel channel;

        /**
         * Random access
         */
        RandomAccessFile rw;

        public FileStoreJob(String id) {
            super(id);
        }

        public FileStoreJob(String id, String name) {
            super(id, name);
        }

        public FileStoreJob(Map config) {
            super(config);
        }


        public void saveAndRelease(){
            if(lock == null || channel == null) return;
            try {
                Map<String, Object> data = DefaultPipelineJob.toMap(this);
                Gson gson = new Gson();
                // to the beginning
                rw.seek(0);
                rw.setLength(0);
                rw.writeChars(gson.toJson(data));
            } catch (Exception e) {
                log.error("Error while writing job file", e.getMessage(), e);
                throw new RuntimeException(e);
            } finally {
                // Release the lock
                if (rw != null) {
                    try {rw.close();} catch (IOException ignore) {}
                }
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

    }

    private class JobIterable implements Iterable<PipelineJob>, Iterator<PipelineJob> {
        private List<File> files;
        private final Iterator<File> iterator;

        public JobIterable(File[] files) {
            this.files = new ArrayList<File>(Arrays.asList(files));
            this.iterator = this.files.iterator();
        }

        @Override
        public Iterator<PipelineJob> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public PipelineJob next() {
            String name = iterator.next().getName();
            String id = name.substring(0, name.length() - 4);
            return get(id);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
