package org.wiyanto.zip4agd.engine;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Decompression interface.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public interface Decompress<T> {

    /**
     * Decompress directory or file
     *
     * @param path directory or file
     * @return
     * @throws IOException
     */
    T decompress(Path path) throws IOException;
}
