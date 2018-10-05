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

    public void clear() {
        try {
            seek(0);
            FileChannel.open(Paths.get(filename), StandardOpenOption.WRITE).truncate(0).close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    public void reset() {
        initStreams();
        seek(0);
    }

    public void seek(long pos) {
        try {
            initStreams();
            randomAccessFile.seek(pos);
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
            // return randomAccessFile.readInt();
            return inputStream.readInt();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
        return -1;
    }

    public void writeInt(int n) {
        try {
            //randomAccessFile.writeInt(n);
            writeCount++;
            outputStream.writeInt(n);
//            if (writeCount > 4000) {
//                flush();
//            }
            flush();
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
