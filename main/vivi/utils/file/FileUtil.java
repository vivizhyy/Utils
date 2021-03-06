package vivi.utils.file;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FileUtil {

    private static final String LINE_TEMINAL = "\r\n";
    
    public static String getContents(String filePath) {
        return getContents(filePath, LINE_TEMINAL);
    }
    
    public static List<String> getLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("file " + filePath + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return lines;
    }
    
    public static String getContents(String filePath, String lineTerminal) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(lineTerminal);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("file " + filePath + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    public static void writeContents(Collection<? extends Object> contents, String filePath) {
        writeContents(contents, filePath, LINE_TEMINAL);
    }
    
    public static void writeContents(Collection<? extends Object> contents, String filePath, String lineTerminal) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (Object line : contents) {
                writer.append(line.toString()).append(lineTerminal);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static BufferedWriter appendContents(BufferedWriter writer, List<String> contents) throws IOException {
        for (String content : contents) {
            writer.append(content).append(LINE_TEMINAL);
        }
        return writer;
    }
    
    public static String getTxtFileNameWithTimestamp(String fileHead) {
        return fileHead + " " +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Timestamp(System.currentTimeMillis())) + 
                ".txt";
    }
    
    public static String getFileNameWithTimestampLong(String head) {
        return head + "-" + System.currentTimeMillis();
    }
}
