package org.wiyanto.zip4agd.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The producer class to generate zip output stream.
 * The instance can be shared to multiple threads for creating output stream with sequential file name part.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class ZipOutputStreamProducer {

    private final static Logger logger = LogManager.getLogger(ZipOutputStreamProducer.class);

    private Path output;

    // output part counter
    private AtomicInteger part = new AtomicInteger(1);

    // output file extension
    private String fileExt = ".zip";

    // output file part with digit numbers
    private String filePart = "part%04d";

    public ZipOutputStreamProducer(Path output) {

        if(output.toFile().isDirectory()) { // if output is directory
            output = Paths.get(output.toString(), UUID.randomUUID().toString()+fileExt);
        }
        else { // else is file then enforce output file name ext as per fileExt
            int idx = output.toFile().toString().lastIndexOf(fileExt);
            if(idx==-1) {
                output = Paths.get(output.toString()+fileExt);
            }
        }

        this.output = output;
    }

    /**
     * Create splittable zip output stream
     *
     * @return
     * @throws FileNotFoundException
     */
    public CalculableZipOutputStream createOutputStream() throws FileNotFoundException {
        try {
            String outputName = getNewOutputNameAndIncrementPart();
            CalculableZipOutputStream czos = new CalculableZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputName)),outputName);
            logger.info ("{} - Created zip part file : {}", Thread.currentThread().getName(), outputName);
            return czos;
        } catch (FileNotFoundException e) {
            throw e;
        }
    }

    private String getNewOutputNameAndIncrementPart() {
        int idx = output.toFile().toString().lastIndexOf(fileExt);
        String fileName = String.format("%s_"+filePart+fileExt,output.toFile().toString().substring(0,idx),part.getAndIncrement());
        return fileName;
    }

}
