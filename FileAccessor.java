import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileAccessor {
    private RandomAccessFile randomAccessFile;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String filename;
    private long writeCount;

    public FileAccessor(String filename) {
            this.filename = filename;
        try {
            randomAccessFile = new RandomAccessFile(filename,"rw");
            outputStream = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD())));
            inputStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(randomAccessFile.getFD())));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initStreams() {
        try {
            outputStream.close();
            inputStream.close();
            randomAccessFile = new RandomAccessFile(filename,"rw");
            outputStream = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD())));
            inputStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(randomAccessFile.getFD())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            initStreams();
            randomAccessFile.seek(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void skipBytesForInputStream(int n) {
        try {
            //initStreams();
            //randomAccessFile.seek(pos);
            inputStream.skipBytes(n);

        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    public void flush() {
        try {
            outputStream.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    public int readInt() {
        try {
            return inputStream.readInt();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
        return -1;
    }

    public void writeInt(int n) {
        try {
            writeCount++;
            outputStream.writeInt(n);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    public long length() {
        try {
            return randomAccessFile.length();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
        return -1;
    }

    public void close() {
        try {
            outputStream.close();
            inputStream.close();
            randomAccessFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

}
