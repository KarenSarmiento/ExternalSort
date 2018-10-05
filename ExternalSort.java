import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExternalSort {

    private static PrintWriter pwDebug;
    private static PrintWriter pwResult;

    private static long INPUT_SIZE;
    // Each integer is 4 bytes long. Start by merging two integers of 4 bytes.
    private final static long MIN_MERGE_SIZE = 4;



    public static void sort(String fileA, String fileB) throws FileNotFoundException, IOException {
        pwDebug = new PrintWriter(
                new FileWriter("C:\\Users\\KSarm\\Documents\\Intellij Projects\\ExternalSort\\out_test.txt"));
        pwResult = new PrintWriter(
                new FileWriter("C:\\Users\\KSarm\\Documents\\Intellij Projects\\ExternalSort\\out_test2.txt"));

        pwDebug.println("  **    STARTING SORT    **  ");
        pwResult.println("  **    STARTING SORT    **  ");

        // Create two pointers for each file.
        FileAccessor a1 = new FileAccessor(fileA);
        FileAccessor a2 = new FileAccessor(fileA);

        FileAccessor b1 = new FileAccessor(fileB);
        FileAccessor b2 = new FileAccessor(fileB);

        pwDebug.println();
        pwDebug.println("\n-----  File initially  -----");
        for (int i = 0; i < a1.length(); i += 4) {
            pwDebug.write(a1.readInt() + " *0*, ");
        }
        a1.reset();

        // Return if file is empty or only has one element.
        INPUT_SIZE = a1.length();
        System.out.println("Input size is " + INPUT_SIZE);
        if (INPUT_SIZE <= MIN_MERGE_SIZE) {
            System.out.println("Small input size does not need sorting so return");
            return;
        }

        // Begin merge sort.
        boolean accessFromA = true;
        // Each integer is 4 ints long.
        long maxBlockSize = MIN_MERGE_SIZE;
        // Keep applying mergeStep until the file is completely sorted
        while (maxBlockSize < INPUT_SIZE) {
            if (accessFromA) {
                mergeStep(a1, a2, b1, maxBlockSize);
                //b1.flush();
            }
            else {
                mergeStep(b1, b2, a1, maxBlockSize);
                //a1.flush();
            }

            maxBlockSize = Math.min(maxBlockSize*2, INPUT_SIZE);
            accessFromA = !accessFromA;
        }
        // Copy result to fileA if final merge was done inside fileB
        if (!accessFromA)
            copyFileBToFileA(a1, b1);
        pwDebug.close();
        pwResult.close();
        a1.close();
        a2.close();
        b1.close();
        b2.close();
    }

    // Executes a step in the merge sort.
    private static void mergeStep(FileAccessor data1, FileAccessor data2, FileAccessor result, long maxBlockSize) {
        //result.clear();
        pwDebug.println("SIZE OF RESULT (at beginning of merge) WAS " + result.length());
        pwDebug.println("SIZE OF RESULT (after clear) IS " + result.length());
        pwDebug.println();
        pwDebug.println("\n-----  New mergestep  -----");
        pwDebug.println("maxblockSize = " + maxBlockSize);
        pwResult.println();
        pwResult.println("\n-----  New mergestep  -----");
        pwResult.println("maxblockSize = " + maxBlockSize);
        // Reset write pointer to the start of the files.
        // (read pointers use seek() inside merge(), and so, do not need to be reset.)
        data1.reset();
        data2.reset();
        result.reset();

        // Set initial state for bounds.
        //bounds defines the: [first element of block 1,
        //                     second element of block 1,
        //                     first element of block 2, //TODO: Do we need this bound?
        //                     second element of block 2]
        long[] bounds = new long[4];
        byte incResult;
        bounds[0] = 0;
        bounds[1] = Math.min(maxBlockSize -1, INPUT_SIZE);
        if (bounds[1] == INPUT_SIZE)
            return;
        bounds[2] = Math.min(maxBlockSize, INPUT_SIZE);
        if (bounds[2] == INPUT_SIZE)
            return;
        bounds[3] = Math.min(2*maxBlockSize -1, INPUT_SIZE);
        if (bounds[3] == INPUT_SIZE)
            incResult = 3;
        else
            incResult = -1;

        printBounds(bounds);

        // Iterate through entire file and merge accordingly.
        while(true) {
            switch (incResult) {
                // Merge step finished in last iteration.
                case 0:
                    return;
                // Odd number of blocks. First block is cut short and can be returned as is.
                case 1:
                    // Odd number of blocks and first block can be returned as is.
                case 2:
                    copyRestOfBlock(data1, result, bounds[0]);
                    return;
                // Even number of blocks. Second block is cut short and must be merged with first.
                case 3:
                    merge(data1, data2, result, bounds);
                    return;
                // EOF not reached. Continue merging.
                case -1:
                    merge(data1, data2, result, bounds);
                    incResult = incrementBounds(bounds, maxBlockSize);
                    break;
            }
        }
    }

    private static void printBounds(long[] bounds) {
        for (int i = 0; i < bounds.length; i++) {
            System.out.println("bound[" + i + "] = " + bounds[i]);
        }
    }

    // Increments each bound by 2*maxBlockSize. Returns -1 if INPUT_SIZE is never exceeded. Otherwise,
    // returns index of bound that exceeded INPUT_SIZE.
    private static byte incrementBounds(long[] bounds, long maxBlockSize) {
        for (byte i = 0; i <= 3; i++) {
            long newBound = bounds[i] + 2*maxBlockSize;
            if (newBound < INPUT_SIZE)
                bounds[i] = newBound;
            else {
                bounds[i] = INPUT_SIZE;
                return i;
            }
        }
        //printBounds(bounds);
        return -1;
    }

    // Merge subsections 1 and 2 of the file from [min, max]. (?)
    private static void merge(FileAccessor data1, FileAccessor data2, FileAccessor result, long[] bounds) {
        pwDebug.println();
        pwDebug.println("SIZE OF data1: " + data1.length());
        pwDebug.println("SIZE OF data2: " + data2.length());
        pwDebug.println("SIZE OF result: " + result.length());
        pwDebug.println();
        int writeCount = 0;

        data1.seek(bounds[0]);
        data2.seek(bounds[2]);
        int curr1 = data1.readInt();
        int curr2 = data2.readInt();
        long index1 = bounds[0] + 4;
        long index2 = bounds[2] + 4;
        // Iterate through the chunks and merge until we clear all elements.
        // The current index[1/2] represents the current position of the file pointer.
        // This means that in order to terminate, we require that index[1/2] is at least 2 greater than
        // the specified bound.
        while ((index1 <= bounds[1] +1) || (index2 <= bounds[3] +1)){
            // if index1 has exceed the bound, then write the rest of data2's chunk to the result
            if (index1 > bounds[1] +1) {
                //pwDebug.println("writing the rest to file..");
                for (; index2 < ((int) bounds[3]) /*-1*/; index2 += 4) {
                    result.writeInt(curr2);
                    writeCount++;
                    /*pwDebug.write(curr2 + "(curr2) *1*, ");
                    pwResult.write(curr2 + ", ");*/
                    curr2 = data2.readInt();
                }
                result.writeInt(curr2);
                writeCount++;
                pwDebug.write(curr2 + "(curr2) *3*, ");
                pwResult.write(curr2 + ", ");
                pwDebug.println("done");
                break;
            }
            // if index2 has exceed the bound, then write the rest of data1's chunk to the result
            if (index2 > bounds[3] +1) {
                //pwDebug.println("writing the rest to file..");
                for (; index1 < ((int) bounds[1]) /*-1*/; index1 += 4) {
                    result.writeInt(curr1);
                    writeCount++;
                    pwDebug.write(curr1 + "(curr1) *5*, ");
                    pwResult.write(curr1 + ", ");
                    curr1 = data1.readInt();
                }
                result.writeInt(curr1);
                writeCount++;
                pwDebug.write(curr1 + "(curr1) *7*, ");
                pwResult.write(curr1 + ", ");
                pwDebug.println("done");
                break;
            }
            if (curr1 <= curr2) {
                result.writeInt(curr1);
                writeCount++;
                pwDebug.write(curr1 + "(curr1) *9*, ");
                pwResult.write(curr1 + ", ");
                index1 += 4;
                if (index1 <= bounds[1] +1)
                    curr1 = data1.readInt();
            }
            if (curr2 <= curr1) {
                result.writeInt(curr2);
                writeCount++;
                pwDebug.write(curr2 + "(curr2) *11*, ");
                pwResult.write(curr2 + ", ");
                index2 += 4;
                if (index2 <= bounds[3] +1)
                    curr2 = data2.readInt();
            }
        }
        pwDebug.println("Wrote " + writeCount + " ints.");
    }

    private static void copyFileBToFileA(FileAccessor fA, FileAccessor fB)  {
        try {
            //fA.clear();
            fA.reset();
            fB.reset();
            for (int i = 0; i < INPUT_SIZE; i += 4) {
                fA.writeInt(fB.readInt());
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    private static void copyRestOfBlock(FileAccessor data, FileAccessor result, long startPoint)  {
        try {
            data.seek(startPoint);
            for (; startPoint < INPUT_SIZE; startPoint += 4) {
                result.writeInt(data.readInt());
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
    }

    private static String byteToHex(byte b) {
        String r = Integer.toHexString(b);
        if (r.length() == 8) {
            return r.substring(6);
        }
        return r;
    }

    public static String checkSum(String f) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream ds = new DigestInputStream(
                    new FileInputStream(f), md);
            byte[] b = new byte[512];
            while (ds.read(b) != -1)
                ;

            String computed = "";
            for(byte v : md.digest())
                computed += byteToHex(v);

            return computed;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<error computing checksum>";
    }

    public static void main(String[] args) throws Exception {
        String f1 = args[0];
        String f2 = args[1];
        sort(f1, f2);
        System.out.println("The checksum is: "+checkSum(f1));
    }
}