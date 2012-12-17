package jip.jobs

import com.google.common.io.Files
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class FileIdServiceTest {
    File dir


    @Test
    public void testWritingNonExistingFile() throws Exception {
        def file = new File(dir, "idfile")
        def service = new FileIdService(file)
        assert service.next() == "0"
        assert service.next() == "1"
        assert service.next() == "2"
        assert service.next() == "3"

        //create FileInputStream object
        FileInputStream fin = new FileInputStream(file);
        DataInputStream din = new DataInputStream(fin);
        assert din.readLong() == 4
        din.close()
    }

    @Test
    public void testReadingFromEmptyFile() throws Exception {
        def file = new File(dir, "idfile")
        assert file.createNewFile()
        def service = new FileIdService(file)
        assert service.next() == "0"
        assert service.next() == "1"
        assert service.next() == "2"
        assert service.next() == "3"

        //create FileInputStream object
        FileInputStream fin = new FileInputStream(file);
        DataInputStream din = new DataInputStream(fin);
        assert din.readLong() == 4
        din.close()
    }

    @Test
    public void testReadingMultithreads() throws Exception {
        def file = new File(dir, "idfile")
        def service = new FileIdService(file)

        def pool = Executors.newFixedThreadPool(4)
        for(int i=0; i<1000; i++){
            pool.submit(new Runnable() {
                @Override
                void run() {
                    service.next()
                }
            })
        }

        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)

        //create FileInputStream object
        FileInputStream fin = new FileInputStream(file);
        DataInputStream din = new DataInputStream(fin);
        assert din.readLong() == 1000
        din.close()
    }

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDir()
    }


    @After
    public void tearDown() throws Exception {
        if (dir != null){
            assert "rm -Rf ${dir.getAbsolutePath()}".execute().waitFor() == 0
        }

    }
}
