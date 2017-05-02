package org.wiyanto.zip4agd.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Compression utility/helper class.
 *
 * Created by wngasinur@gmail.com on 30/4/2017.
 */
public class CompressUtil {

    public static final int BUFFER_SIZE = 2048;

    /**
     * Get files and directories queue list
     *
     * @param input the directory to read from
     * @return
     * @throws IOException
     */
    public static BlockingQueue<Path> getFileQueueList(Path input) throws IOException {
        GetFileQueueList visitor = new GetFileQueueList(input);
        Files.walkFileTree(input, visitor);
        return visitor.getQueueList();
    }

    private static class GetFileQueueList extends SimpleFileVisitor<Path> {

        private BlockingQueue<Path> queueList;
        private Path input;

        public GetFileQueueList(Path input) {
            this.input = input;
            queueList = new LinkedBlockingDeque<>();
        }

        public BlockingQueue<Path> getQueueList() {
            return queueList;
        }

        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            queueList.add(file);
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // don't add parent/empty directory
            if (!input.relativize(dir).toString().isEmpty()) {
                queueList.add(dir);
            }
            return FileVisitResult.CONTINUE;
        }

    }
}
