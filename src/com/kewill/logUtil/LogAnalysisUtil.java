package com.kewill.logUtil;

import com.kewill.common.LogAttribute;
import com.kewill.model.LogModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by YanJun on 2016/4/11.
 */
public class LogAnalysisUtil {
    public static List<LogModel> analysisExecQueryTime(int bufSize, FileChannel fcin, ByteBuffer rBuffer) {
        String enterStr = "\r\n";
        String enterStr2 = "\n";
        List<LogModel> logList = new ArrayList<LogModel>();
        try {
            int sqlFindMode = 0;
            int execTimeMode = 0;
            byte[] bs = new byte[bufSize];
            StringBuffer strBuf = new StringBuffer("");
            Map<Integer, String> sqlMap = new TreeMap<Integer, String>();
            Map<Integer, String> execTimeMap = new TreeMap<Integer, String>();
            StringBuffer sqlBuf = new StringBuffer("");
            StringBuffer execTimeBuf = new StringBuffer("");
            int sqlIndex = 0;
            int execTimeIndex = 0;
            int lineIndex = 0;
            while (fcin.read(rBuffer) != -1) {
                int rSize = rBuffer.position();
                rBuffer.rewind();
                rBuffer.get(bs);
                rBuffer.clear();
                String tempString = new String(bs, 0, rSize);
                int fromIndex = 0;
                int endIndex = 0;
                while ((endIndex = tempString.indexOf(enterStr, fromIndex)) != -1 ||
                        (endIndex = tempString.indexOf(enterStr2, fromIndex)) != -1) {
                    String line = tempString.substring(fromIndex, endIndex);
                    line = new String(strBuf.toString() + line);
                    //System.out.println(line);
                    lineIndex++;
                    if (sqlFindMode == 1 && line.indexOf(LogAttribute.USER_FLAG) >= 0) {
                        sqlFindMode = 0;
                    } else if (sqlFindMode == 1) {
                        sqlBuf.append(line).append(" ");
                    } else if (line.indexOf(LogAttribute.EXEC_SQL_FLAG) > 0 && line.indexOf(LogAttribute.USER_FLAG) < 0) {
                        sqlFindMode = 1;
                        int execSqlFlagIndex = line.indexOf(LogAttribute.EXEC_SQL_FLAG);
                        //判断exec sql 标志位是不是最后的字符串
                        if((execSqlFlagIndex + 1 + LogAttribute.EXEC_SQL_FLAG.length()) != line.length()){
                            sqlBuf.append(line.substring(line.indexOf(LogAttribute.EXEC_SQL_FLAG)));
                        }
                    }
                    if (execTimeMode == 1 && line.indexOf(LogAttribute.EXEC_TIME_FLAG) < 0) {
                        execTimeMode = 0;
                    }else if (line.indexOf(LogAttribute.EXEC_TIME_FLAG) > 0) {
                        execTimeMode = 1;
                        execTimeBuf.append(
                                line.substring(line.indexOf(LogAttribute.EXEC_TIME_FLAG),
                                        line.indexOf(LogAttribute.MS) + 2)
                        );
                    }

                    strBuf.delete(0, strBuf.length());
                    fromIndex = endIndex + 1;
                }
                if (!StringUtil.isEmpty(sqlBuf.toString()) && sqlFindMode == 0) {
                    sqlMap.put(lineIndex, sqlBuf.toString());
                    sqlBuf.delete(0, sqlBuf.length());

                }
                if (!StringUtil.isEmpty(execTimeBuf.toString()) && execTimeMode == 1) {
                    execTimeMap.put(lineIndex-1, execTimeBuf.toString());
                    execTimeBuf.delete(0, execTimeBuf.length());
                    execTimeIndex++;
                }
                if (rSize > tempString.length()) {
                    strBuf.append(tempString.substring(fromIndex, tempString.length()));
                } else {
                    strBuf.append(tempString.substring(fromIndex, rSize));
                }
            }

            for (Integer key : sqlMap.keySet()) {
                LogModel log = new LogModel();
                log.setId(key);
                String execSqlStr = sqlMap.get(key);
                if(!StringUtil.isEmpty(execSqlStr)){
                    log.setExecSelectSql(execSqlStr.substring(execSqlStr.indexOf(":") + 1));
                }
                String execTimeStr = execTimeMap.get(key);
                if(!StringUtil.isEmpty(execTimeStr)){
                    log.setExecTime(execTimeStr.substring(execTimeStr.indexOf(":") + 1,execTimeStr.indexOf(" MS")));
                }
                logList.add(log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(fcin != null) {
                    fcin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return logList;

    }

    public static List<LogModel> analysisError(int bufSize, FileChannel fcin, ByteBuffer rBuffer) {
        String enterStr = "\r\n";
        String enterStr2 = "\n";
        List<LogModel> logList = new ArrayList<LogModel>();
        try {
            byte[] bs = new byte[bufSize];
            StringBuffer strBuf = new StringBuffer("");
            while (fcin.read(rBuffer) != -1) {
                int rSize = rBuffer.position();
                rBuffer.rewind();
                rBuffer.get(bs);
                rBuffer.clear();
                String tempString = new String(bs, 0, rSize);
                int fromIndex = 0;
                int endIndex = 0;
                while ((endIndex = tempString.indexOf(enterStr, fromIndex)) != -1 ||
                        (endIndex = tempString.indexOf(enterStr2, fromIndex)) != -1) {
                    String line = tempString.substring(fromIndex, endIndex);
                    line = new String(strBuf.toString() + line);
                    if(line.indexOf(LogAttribute.ERROR_FLAG)> -1){
                        LogModel model = new LogModel();
                        model.setError(line);
                        logList.add(model);
                    }

                    strBuf.delete(0, strBuf.length());
                    fromIndex = endIndex + 1;
                }
                if (rSize > tempString.length()) {
                    strBuf.append(tempString.substring(fromIndex, tempString.length()));
                } else {
                    strBuf.append(tempString.substring(fromIndex, rSize));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(fcin != null) {
                    fcin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return logList;

    }

    public static void writeLog(List<LogModel> logList,String mode) throws IOException {
        String datePattern = new SimpleDateFormat(LogAttribute.DATE_PATTERN).format(System.currentTimeMillis());

        String path = System.getProperty("user.home") + "\\logAnalysis";
        String fileName = mode + "_" + datePattern +".log";
        System.out.println(path);
        File fileDir = new File(path);
        if(!fileDir.exists() && !fileDir.isDirectory()){
            fileDir.mkdir();
        }
        File writeFile = new File(path + "\\" + fileName);
        if(fileDir.exists()){
            writeFile.createNewFile();
        }else{
            System.out.println("Path error");
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeFile)));
        if(mode.equals(LogAttribute.MODE_QUERY)){
            for(LogModel model : logList){
                bw.write("Exec Query SQL" + "\n");
                bw.write("---------------------------------------------------------" + "\n");
                bw.write(model.getExecSelectSql()+"\n");
                bw.write("==> Exec Time (ms) " + model.getExecTime() + "\n");
                bw.write("---------------------------------------------------------" + "\n");
            }
        }else if(mode.equals(LogAttribute.MODE_ERROR)){
            for(LogModel model : logList){
                bw.write("ERROR" + "\n");
                bw.write(model.getError()+"\n");
                bw.write("---------------------------------------------------------" + "\n");
            }
        }
        bw.flush();
        bw.close();

    }


}
