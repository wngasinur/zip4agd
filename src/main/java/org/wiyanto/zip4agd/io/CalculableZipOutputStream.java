package org.wiyanto.zip4agd.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The extension class of ZipOutputStream which added capability to get written size of ZipOutputStream during writing process.
 * It's internally used ByteArrayOutputStream wrapped by ZipOutputStream to write in memory.
 *
 * Created by wngasinur@gmail.com on 29/4/2017.
 */
public class CalculableZipOutputStream extends ZipOutputStream  {

    private ZipOutputStream zos2;
    private ByteArrayOutputStream baos;

    private ZipEntry lastEntry;
    private int counter = 0;
    private String outputName;

    public String getOutputName() {
        return outputName;
    }

    public CalculableZipOutputStream(OutputStream os, String outputName) {
        super(os);
        this.baos = new ByteArrayOutputStream();
        this.zos2 = new ZipOutputStream(baos);
        this.outputName = outputName;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        zos2.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b,off,len);
        zos2.write(b,off,len);
    }

    @Override
    public void putNextEntry(ZipEntry e) throws IOException {
        super.putNextEntry(e);
        zos2.putNextEntry(e);
        lastEntry = e;
    }

    @Override
    public void closeEntry() throws IOException {
        super.closeEntry();
        zos2.closeEntry();
    }

    @Override
    public void close() throws IOException {
        super.close();
        zos2.close();
    }

    /**
     * Close internal output stream and get written size.
     *
     * @return
     * @throws IOException
     */
    public int closeEntryAndGetSize() throws IOException {
        try {
            // close entry is required to get more precise written size
            zos2.closeEntry();
            zos2.putNextEntry(new ZipEntry(lastEntry.getName() + counter));
            counter++;
        } catch (IOException e) {
            throw e;
        }

        return baos.size();
    }

}
