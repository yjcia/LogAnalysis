import com.kewill.common.LogAttribute;
import com.kewill.logUtil.LogAnalysisUtil;
import com.kewill.model.LogModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class LogAnalysis {

    public static void main(String[] args) {
        int bufSize = 100;
        String filePath = "";
        String mode = "";
        if (args.length < 1) {
            System.out.println("Log file path cannot be empty");
        } else if (args.length == 1) {
            filePath = args[0];
        } else if (args.length == 2) {
            filePath = args[0];
            mode = args[1];
        }

//        String filePath = "J:\\kewillfwd.log.2016-03-31";
//        String mode = "error";
        File fin = new File(filePath);
        FileChannel fcin;
        try {
            if (fin.exists()) {
                fcin = new RandomAccessFile(fin, "r").getChannel();
                ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);
                if (mode.equals(LogAttribute.MODE_QUERY)) {
                    System.out.println("Start Analysis ...");
                    List<LogModel> logList = LogAnalysisUtil.analysisExecQueryTime(bufSize, fcin, rBuffer);
                    LogAnalysisUtil.writeLog(logList,mode);
                    System.out.println("Query Exec Analysis finish ...");
                } else if (mode.equals(LogAttribute.MODE_ERROR)) {
                    System.out.println("Start Analysis ...");
                    List<LogModel> logList = LogAnalysisUtil.analysisError(bufSize, fcin, rBuffer);
                    LogAnalysisUtil.writeLog(logList,mode);
                    System.out.println("Query Exec Analysis finish ...");
                }
            } else {
                System.out.println("Log file cannot be found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
