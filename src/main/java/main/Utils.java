package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author caiwei
 * @date 2022/5/14 10:17 下午
 * @descsiption 工具类
 */
public class Utils {

    public static String readToString(String fileName) throws IOException {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long fileLength = file.length();
        byte[] filecontent = new byte[fileLength.intValue()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
        }
        return new String(filecontent, encoding);
    }

    public static void appendIntoFile(String ipPort) {
        try (FileOutputStream fos = new FileOutputStream("jmx_results.txt", true)) {
            //true表示在文件末尾追加
            ipPort += "\n";
            fos.write(ipPort.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendCredentialsJmxIntoFile(String ipPort) {
        try (FileOutputStream fos = new FileOutputStream("jmx_credentials_results.txt", true)) {
            ipPort += "\n";
            fos.write(ipPort.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
