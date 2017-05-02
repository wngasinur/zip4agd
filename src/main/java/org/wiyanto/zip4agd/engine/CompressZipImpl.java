package org.wiyanto.zip4agd.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wiyanto.zip4agd.io.ZipOutputStreamProducer;
import org.wiyanto.zip4agd.io.CalculableZipOutputStream;
import org.wiyanto.zip4agd.model.CompressResult;
import org.wiyanto.zip4agd.util.CompressUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Compression engine which use internal JDK ZLIB compression library to compress.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class CompressZipImpl implements Compress<CompressResult> {

    private final static Logger logger = LogManager.getLogger(CompressZipImpl.class);

    private Path input;
    private ZipOutputStreamProducer zipOutputStreamProducer;

    private long maxSizeinBytes;
    private long writtenSize = 0;

    // Splittable zip output stream
    private CalculableZipOutputStream zipOutputStream;

    public CompressZipImpl(ZipOutputStreamProducer zipOutputStreamProducer, Path input, int maxSizeInMB) {
        this.zipOutputStreamProducer = zipOutputStreamProducer;
        this.input = input;
        this.maxSizeinBytes = maxSizeInMB * 1024 * 1024;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public CompressResult compress(Path file) throws IOException {

        List<Path> zipFiles = new ArrayList<>();

        Path outputName = initZipOutputStreamIfEmpty();
        zipFiles.add(outputName);

        if(file.toFile().isDirectory()) {
            String path = input.relativize(file).toString() + "/";
            logger.info ("{} - Starting compress : {}", Thread.currentThread().getName(), path);

            zipOutputStream.putNextEntry(new ZipEntry(path));
            zipOutputStream.closeEntry();
        }
        else {

            ZipEntry zipEntry;
            if(input.equals(file)) { // if single file compression
                zipEntry = new ZipEntry(input.getParent().relativize(file).toString());
            }
            else {
                zipEntry = new ZipEntry(input.relativize(file).toString());
            }
            logger.info ("{} - Starting compress : {}", Thread.currentThread().getName(), zipEntry.getName());
            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                byte[] buffer = new byte[CompressUtil.BUFFER_SIZE];

                zipOutputStream.putNextEntry(zipEntry);

                int read;
                while (-1 != (read = fis.read(buffer))) {
                    if (writtenSize + read > maxSizeinBytes - (2 * 1024)) {
                        int compressedSize = zipOutputStream.closeEntryAndGetSize();
                        if(compressedSize + read > maxSizeinBytes) {
                            zipOutputStream.close();
                            logger.info ("{} - split : {}", Thread.currentThread().getName(), zipEntry.getName());
                            zipOutputStream = zipOutputStreamProducer.createOutputStream();
                            zipFiles.add(Paths.get(zipOutputStream.getOutputName()));
                            zipEntry = new ZipEntry(input.relativize(file).toString());
                            zipOutputStream.putNextEntry(zipEntry);
                            writtenSize = 0;
                        } else {
                            writtenSize = compressedSize;
                        }
                    }
                    zipOutputStream.write(buffer, 0, read);
                    writtenSize += read;
                }
            }

            zipOutputStream.closeEntry();
            logger.info ("{} - Finished compress : {}", Thread.currentThread().getName(), zipEntry.getName());

        }

        return new CompressResult(input,zipFiles);
    }

    private Path initZipOutputStreamIfEmpty() throws FileNotFoundException {
        if(zipOutputStream==null) {
            zipOutputStream = zipOutputStreamProducer.createOutputStream();
        }

        return Paths.get(zipOutputStream.getOutputName());
    }

    /**
     * Close zip output stream
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(zipOutputStream!=null) {
            zipOutputStream.close();
        }
    }
}
