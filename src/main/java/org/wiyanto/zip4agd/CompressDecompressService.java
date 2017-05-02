package org.wiyanto.zip4agd;

import org.wiyanto.zip4agd.model.CompressResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The main compression service interface.
 *
 * Created by wngasinur@gmail.com on 1/5/2017.
 */
public interface CompressDecompressService {

    /**
     * Run compression in number of threads
     *
     * @param input
     * @param output
     * @param maxSizeInMB
     * @param nThread
     * @return
     * @throws IOException
     */
    List<CompressResult> parallelCompress(Path input, Path output, int maxSizeInMB, int nThread) throws IOException;

    /**
     * Run compression in main thread
     *
     * @param input the full path of the folder or file to read from
     * @param output the full path of the folder or file to write, if
     * @param maxSizeInMB
     * @return
     * @throws IOException
     */
    List<CompressResult> compress(Path input, Path output, int maxSizeInMB) throws IOException;

    /**
     * Run decompression in main thread
     *
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    List<CompressResult> decompress(Path input, Path output) throws IOException;
}
