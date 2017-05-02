package org.wiyanto.zip4agd.engine;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Compression interface.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public interface Compress<T> extends Closeable {

    /**
     * Compress directory or file
     *
     * @param path directory or file
     * @return
     * @throws IOException
     */
    T compress(Path path) throws IOException;
}
