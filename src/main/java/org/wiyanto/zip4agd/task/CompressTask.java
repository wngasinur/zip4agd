package org.wiyanto.zip4agd.task;

import org.wiyanto.zip4agd.io.ZipOutputStreamProducer;
import org.wiyanto.zip4agd.engine.CompressZipImpl;
import org.wiyanto.zip4agd.model.CompressResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * A callable compression task which use queue to get the list of files/directories to compress.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class CompressTask extends CompressZipImpl implements Callable<List<CompressResult>> {

    private BlockingQueue<Path> queue;

    public CompressTask(BlockingQueue<Path> queue, ZipOutputStreamProducer zipOutputStreamProducer, Path input, int maxSizeInMB){
        super(zipOutputStreamProducer,input,maxSizeInMB);
        this.queue = queue;
    }

    @Override
    public List<CompressResult> call() throws Exception {

        List<CompressResult> zipResults = new ArrayList<>();

        try {
            // iterate the queue till is empty
            while (queue.peek() != null) {
                Path file = queue.poll();
                CompressResult zipResult = compress(file);
                zipResults.add(zipResult);
            }
        } finally {
            // close zip output stream
            close();
        }

        return zipResults;
    }

}
