package org.wiyanto.zip4agd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Main launcher class or entry point of the application.
 *
 * Created by wngasinur@gmail.com on 28/4/2017.
 */
public class MainLauncher {

    private final static String CMD_COMPRESS = "compress";
    private final static String CMD_DECOMPRESS = "decompress";

    private final static Logger logger = LogManager.getLogger(MainLauncher.class);

    public static void main(String[] args) throws IOException {

        if(args==null || args.length==0) {
            System.out.println(showHelp());
            return;
        }
        String cmd = args[0];

        CompressDecompressService service = new CompressDecompressServiceZipImpl();

        long start = System.currentTimeMillis();
        switch (cmd) {
            case CMD_COMPRESS:
                if(args.length<4) {
                    System.out.println("Insufficient parameters");
                    return;
                }
                if(args.length==5) {
                    service.parallelCompress(Paths.get(args[1]),Paths.get(args[2]),Integer.valueOf(args[3]),Integer.valueOf(args[4]));
                }
                else {
                    service.compress(Paths.get(args[1]),Paths.get(args[2]),Integer.valueOf(args[3]));
                }
                break;
            case CMD_DECOMPRESS:
                if(args.length<3) {
                    System.out.println("Insufficient parameters");
                    return;
                }
                service.decompress(Paths.get(args[1]),Paths.get(args[2]));
                break;
            default:
                System.out.println(showHelp());
                return;
        }
        long end = System.currentTimeMillis();

        logger.info("Elapsed time : {}ms", (end-start));

    }

    public static String showHelp() {
        return  System.lineSeparator() + "zip4agd - archive and compress/decompress directories or files. To invoke : " + System.lineSeparator() +
                "[compress]" + System.lineSeparator() +
                "java -jar <jar file> "+CMD_COMPRESS+" [input folder or file] [output folder or file] [number of output max size in mb] [optional - number of threads]" + System.lineSeparator() +
                "[decompress]" + System.lineSeparator() +
                "java -jar <jar file> "+CMD_DECOMPRESS+" [input folder] [output folder]";
    }
}
