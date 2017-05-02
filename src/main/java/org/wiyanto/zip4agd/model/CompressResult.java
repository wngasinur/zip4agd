package org.wiyanto.zip4agd.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Compression result model which can be used to trace output files generated by compression service
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class CompressResult {

    private Path input;
    private List<Path> outputs;

    public CompressResult(Path input, List<Path> outputs) {
        this.input = input;
        this.outputs = outputs;
    }

    /**
     * Get input path
     * @return
     */
    public Path getInput() {
        return input;
    }

    /**
     * Get output paths
     * @return
     */
    public List<Path> getOutputs() {
        return outputs;
    }
}