package vivi.utils.file;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;


public class FileUtil {

    private static final String LINE_TEMINAL = "\r\n";
    
    public static String getContents(String filePath) {
        return getContents(filePath, LINE_TEMINAL);
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
    
    public static void writeContents(List<String> contents, String filePath) {
        writeContents(contents, filePath, LINE_TEMINAL);
    }
    
    public static void writeContents(List<String> contents, String filePath, String lineTerminal) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (String line : contents) {
                writer.append(line).append(lineTerminal);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getTxtFileNameWithTimestamp(String fileHead) {
        return fileHead + 
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Timestamp(System.currentTimeMillis())) + 
                ".txt";
    }
}
