package org.wiyanto.zip4agd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wiyanto.zip4agd.engine.Compress;
import org.wiyanto.zip4agd.engine.CompressZipImpl;
import org.wiyanto.zip4agd.engine.Decompress;
import org.wiyanto.zip4agd.engine.DecompressZipImpl;
import org.wiyanto.zip4agd.io.ZipOutputStreamProducer;
import org.wiyanto.zip4agd.model.CompressResult;
import org.wiyanto.zip4agd.task.CompressTask;
import org.wiyanto.zip4agd.util.CompressUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * This class implements an compression service which use internal JDK ZLIB compression library.
 * Includes support for both compressed and uncompressed.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class CompressDecompressServiceZipImpl implements CompressDecompressService {

    private final static Logger logger = LogManager.getLogger(CompressDecompressServiceZipImpl.class);

    /**
     * Checking validity of arguments before decompressing.
     *
     * @param input
     * @param output
     */
    private void prepareDecompress(Path input, Path output) {
        if(!input.toFile().isDirectory()) {
            throw new IllegalArgumentException("Invalid input path");
        }

        if(!output.toFile().isDirectory()) {
            throw new IllegalArgumentException("Invalid output path");
        }
    }


    /**
     * Checking validity of arguments before compressing. It will throw exception if some arguments are invalid
     *
     * @param input
     * @param output
     * @param maxSizeInMB
     * @param nThread
     * @throws IOException
     */
    private void prepareCompress(Path input, Path output, int maxSizeInMB, int nThread) throws IOException{
        // if input is not directory or file, then throw error
        if(!input.toFile().isDirectory() && !input.toFile().isFile()) {
            throw new IllegalArgumentException("Invalid input path");
        }

        if(!output.toFile().isDirectory() && !output.getParent().toFile().isDirectory()) {
            throw new IllegalArgumentException("Invalid output path");
        }

        if(maxSizeInMB<1) {
            throw new IllegalArgumentException("Invalid max size in MB");
        }

        if(nThread<1) {
            throw new IllegalArgumentException("Invalid number of thread");
        }

        // if output is directory and is not the file, then create directory
        if(output.toFile().isDirectory() && !output.toFile().exists()) {
            output.toFile().mkdir();
        }
    }

    /**
     * Validate number of files to be processed against number of thread.
     *
     * @param fileList
     * @param nThread
     */
    private void validateFileList(BlockingQueue<Path> fileList, int nThread) {
        if(fileList.isEmpty()) {
            throw new IllegalArgumentException("Invalid input path : empty directory");
        }
        if(nThread>fileList.size()) {
            throw new IllegalArgumentException("Invalid number of thread : nThread is greater than number of files/folders");
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public List<CompressResult> parallelCompress(Path input, Path output, int maxSizeInMB, int nThread) throws IOException {
        logger.info("Starting Parallel compress");
        logger.info("Input : {}", input);
        logger.info("Output : {}", output);
        logger.info("Max Size (MB) : {}", maxSizeInMB);
        logger.info("Thread : {}", nThread);

        prepareCompress(input, output, maxSizeInMB, nThread);

        BlockingQueue<Path> fileList = CompressUtil.getFileQueueList(input);
        
        validateFileList(fileList,nThread);
        
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        ZipOutputStreamProducer zipOutputStreamCreator = new ZipOutputStreamProducer(output);

        List<Future<List<CompressResult>>> futures = new ArrayList<>();
        IntStream.rangeClosed(1,nThread).forEach(i -> {
            futures.add(executorService.submit(new CompressTask(fileList,zipOutputStreamCreator,input,maxSizeInMB)));
        });

        List<CompressResult> outputModels = new ArrayList<>();
        for(Future<List<CompressResult>> future: futures) {
            try {
                outputModels.addAll(future.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        executorService.shutdownNow();

        return outputModels;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public List<CompressResult> compress(Path input, Path output, int maxSizeInMB) throws IOException {
        logger.info("Starting compress");
        logger.info("Input : {}", input);
        logger.info("Output : {}", output);
        logger.info("Max Size (MB) : {}", maxSizeInMB);

        prepareCompress(input, output, maxSizeInMB, 1);

        BlockingQueue<Path> fileList = CompressUtil.getFileQueueList(input);

        validateFileList(fileList,1);
        
        ZipOutputStreamProducer zipOutputStreamProducer = new ZipOutputStreamProducer(output);
        try(Compress<CompressResult> compress = new CompressZipImpl(zipOutputStreamProducer,input,maxSizeInMB)) {
            List<CompressResult> zipResults = new ArrayList<>();
            for(Path path: fileList) {
                CompressResult zipResult = compress.compress(path);
                zipResults.add(zipResult);
            }

            return zipResults;
        }

    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public List<CompressResult> decompress(Path input, Path output) throws IOException {
        logger.info("Starting decompress");
        logger.info("Input : {}", input);
        logger.info("Output : {}", output);

        prepareDecompress(input, output);

        BlockingQueue<Path> fileList = CompressUtil.getFileQueueList(input);
        Decompress<CompressResult> decompress = new DecompressZipImpl(input,output);

        List<CompressResult> zipResults = new ArrayList<>();
        for(Path path: fileList) {
            CompressResult zipResult = decompress.decompress(path);
            zipResults.add(zipResult);
        }

        return zipResults;
    }


}
