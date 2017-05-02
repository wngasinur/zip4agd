package org.wiyanto.zip4agd.generator;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by wngasinur@gmail.com on 2/5/2017.
 */
public class FileDummyGenerator {

    private static SecureRandom random = new SecureRandom();

    public static Path generateFile(Path rootTmpDir, long bytes) throws IOException {
        Path file = Paths.get(rootTmpDir.toString(), UUID.randomUUID().toString()+".txt");

        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.toFile()))) {
            long currentBytes = 0;
            do {
                String text = new BigInteger(130, random).toString(32);
                bufferedWriter.write(text);
                currentBytes+=text.getBytes().length;
            } while(currentBytes<bytes);
        }

        return file;

    }


}
