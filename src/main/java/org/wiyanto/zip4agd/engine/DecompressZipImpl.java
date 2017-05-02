package org.wiyanto.zip4agd.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wiyanto.zip4agd.model.CompressResult;
import org.wiyanto.zip4agd.util.CompressUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.wiyanto.zip4agd.util.CompressUtil.BUFFER_SIZE;

/**
 * Compression engine which use internal JDK zip algorithm to decompress.
 *
 * Created by wngasinur@gmail.com on 1/5/2017.
 */
public class DecompressZipImpl implements Decompress<CompressResult> {

    private final static Logger logger = LogManager.getLogger(DecompressZipImpl.class);

    private Path input,output;

    public DecompressZipImpl(Path input, Path output) {
        this.input = input;
        this.output = output;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public CompressResult decompress(Path file) throws IOException {

        File inputFile = file.toFile();

        logger.info ("Starting decompress : {}", inputFile);

        List<Path> extractedFiles = new ArrayList<>();
        try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                logger.info ("Extracting : {}", entry);

                extractedFiles.add(Paths.get(output.toString(), entry.getName()));

                if (entry.getName().endsWith("/")) {
                    Paths.get(output.toString(), entry.getName()).toFile().mkdir();
                    continue;
                }
                File outputFile = Paths.get(output.toString(), entry.getName()).toFile();
                int count;
                byte data[] = new byte[BUFFER_SIZE];

                if(outputFile.exists()) { // if the file is already exist
                    logger.info ("Merging : {}", entry);
                    long fileLength = outputFile.length();

                    // write the file using random access file
                    try(RandomAccessFile raf = new RandomAccessFile(outputFile,"rw")) {
                        raf.seek(fileLength); // go to end of the file
                        while ((count = zis.read(data, 0, BUFFER_SIZE))
                                != -1) {
                            raf.write(data, 0, count);
                        }
                    }
                } else {
                    // for parallel compression, sometimes folders are not in the order of the compressed file
                    // when decompressing file, ensure folder of the file is created
                    Paths.get(outputFile.toString()).getParent().toFile().mkdirs();

                    try(BufferedOutputStream bos = new
                            BufferedOutputStream(new
                            FileOutputStream(outputFile), BUFFER_SIZE)) {
                        while ((count = zis.read(data, 0, BUFFER_SIZE))
                                != -1) {
                            bos.write(data, 0, count);
                        }
                    }
                }
            }
        }
        logger.info ("Finished  decompress : {}", inputFile);
        return new CompressResult(input,extractedFiles);
    }
}
