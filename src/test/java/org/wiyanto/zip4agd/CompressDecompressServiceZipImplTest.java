package org.wiyanto.zip4agd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wiyanto.zip4agd.generator.FileDummyGenerator;
import org.wiyanto.zip4agd.model.CompressResult;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

/**
 * Created by wngasinur@gmail.com on 1/5/2017.
 */
@RunWith(JUnit4.class)
public class CompressDecompressServiceZipImplTest {

    private final static Logger logger = LogManager.getLogger(CompressDecompressServiceZipImplTest.class);

    private CompressDecompressService service = new CompressDecompressServiceZipImpl();

    private Path rootTmpDir,rootTmpInputDir,rootTmpOutputZipDir,rootTmpOutputExtractDir;

    private int maxSizeInMB = 1;

    private int nThreads = 2;

    private Set<Path> generatedFiles = new HashSet<>();

    private int expectedGeneratedFile;

    @Before
    public void setUp() throws IOException {
        prepareFiles();
    }

    @After
    public void tearDown() throws IOException {

        // cleanup tmp dir -> delete folders and files
        Files.walkFileTree(rootTmpDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

    }

    public void prepareFiles() throws IOException {

        String property = "java.io.tmpdir";

        rootTmpDir = Paths.get(System.getProperty(property),UUID.randomUUID().toString());
        rootTmpInputDir = Paths.get(rootTmpDir.toString(),"input");
        rootTmpInputDir.toFile().mkdirs();

        rootTmpOutputZipDir = Paths.get(rootTmpDir.toString(),"out","zip");
        rootTmpOutputZipDir.toFile().mkdirs();

        rootTmpOutputExtractDir = Paths.get(rootTmpDir.toString(),"out","extract");
        rootTmpOutputExtractDir.toFile().mkdirs();

        logger.info("Root tmp dir : {}",rootTmpInputDir);

        generatedFiles.clear();
        // generate input files
        generatedFiles.add(FileDummyGenerator.generateFile(rootTmpInputDir,1024));
        generatedFiles.add(FileDummyGenerator.generateFile(rootTmpInputDir,1024*1024));

        // input file greater than max size
        generatedFiles.add(FileDummyGenerator.generateFile(rootTmpInputDir,1024*1024*maxSizeInMB*2));

        expectedGeneratedFile = generatedFiles.size();

    }
    @Test
    public void testCompressSequential() throws IOException {

        List<CompressResult> zipResultList = service.compress(rootTmpInputDir,rootTmpOutputZipDir,maxSizeInMB);
        validateResult(zipResultList);

    }

    @Test
    public void testCompressParallel() throws IOException {

        List<CompressResult> zipResultList = service.parallelCompress(rootTmpInputDir,rootTmpOutputZipDir,maxSizeInMB,nThreads);
        validateResult(zipResultList);

    }

    private void validateResult(List<CompressResult> zipResultList) throws IOException {

        // compress should generate compressed files
        Assert.assertEquals(expectedGeneratedFile,zipResultList.size());

        Set<Path> zipFileResultList = zipResultList.stream().flatMap(x -> x.getOutputs().stream()).collect(Collectors.toSet());
        zipFileResultList.stream().forEach(path -> {
            // compressed files should not exceed max size
            Assert.assertTrue(path.toFile().length() < maxSizeInMB * 1024 * 1024);
        });

        List<CompressResult> extractResultList = service.decompress(rootTmpOutputZipDir,rootTmpOutputExtractDir);
        Set<Path> extractFileResultList = extractResultList.stream().flatMap(x -> x.getOutputs().stream()).collect(Collectors.toSet());

        extractFileResultList.forEach(path -> {
            try {
                Optional<Path> inputFile = generatedFiles.stream().filter(file -> file.getFileName().equals(path.getFileName())).findFirst();
                if(inputFile.isPresent()) {
                    logger.info("Comparing between {} and {}",path,inputFile.get());

                    // extracted content should be the same with input content
                    Assert.assertArrayEquals(Files.readAllBytes(inputFile.get()),Files.readAllBytes(path));
                } else {
                    fail("File not found "+path);
                }

            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }


}
