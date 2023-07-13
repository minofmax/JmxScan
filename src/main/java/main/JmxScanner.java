package main;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author caiwei
 * @date 2022/5/14 10:16 下午
 */

public class JmxScanner {
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;

    static class JmxExecutor implements Runnable {
        private String ipAddress;
        private String port;

        public JmxExecutor() {
            super();
        }

        public JmxExecutor(String ipAddress, String port) {
            this.ipAddress = ipAddress;
            this.port = port;
        }


        public void jmxScan() {
            Thread.currentThread().interrupt();
            try {
                Socket socket = new Socket();
                // 增加socket探测，避免由于部分端口不通造成的超时等待。本质是因为JMXConnectorFactory.connect没有timeout参数。
                socket.connect(new InetSocketAddress(ipAddress, Integer.parseInt(port)), 3000);
                JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + ipAddress + ":" + port + "/jmxrmi");
                System.out.println(String.format("[+] Connecting to JMX server, ip: %s, port: %s", ipAddress, port));
                JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
                if (jmxConnector != null) {
                    MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
                    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
                    System.out.println("[+] " + ipAddress + ":" + port + " exist vuln, Current OS: " + operatingSystemMXBean.getName() + " " + operatingSystemMXBean.getArch() + " " + operatingSystemMXBean.getVersion());
                    Utils.appendIntoFile(ipAddress + ":" + port);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Connection refused to host: 127.0.0.1")) {
                    System.out.println("[+] " + e.getMessage());
                    Utils.appendIntoFile(ipAddress + ":" + port);
                } else if (e.getMessage().contains("Authentication failed! Credentials required")) {
                    Utils.appendCredentialsJmxIntoFile(ipAddress + ":" + port);
                    System.out.println("[+] " + e.getMessage());
                } else {
                    System.out.println("++++++++");
                    e.printStackTrace();
                    System.out.println("[-] " + e.getMessage());
                }
            }
        }

        @Override
        public void run() {
            jmxScan();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        List<String> lines = new ArrayList<>();
        int argNum = args.length;

        switch (argNum) {
            case 2:
                String hostname = args[0].trim();
                String port = args[1].trim();
                lines.add(hostname + ":" + port);
                break;
            case 1:
                String filePath = args[0].trim();
                lines = Arrays.asList(Utils.readToString(filePath).split("\n"));
                break;
            case 0:
                lines = Arrays.asList(Utils.readToString("targets.txt").split("\n"));
                break;
            default:
                System.out.println("输入参数长度有误");
                return;
        }
        if (lines.isEmpty()) {
            System.out.println("没有扫描目标, 退出");
            return;
        }

        ExecutorService es = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (String line : lines) {
            String[] split = line.split(":");
            if (split.length < 2) {
                continue;
            }
            String ipAddress = split[0];
            String port = split[1];
            JmxExecutor jmxExecutor = new JmxExecutor(ipAddress, port);
            Future<?> future = es.submit(jmxExecutor);
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                future.cancel(false);
//                System.out.println(future.isDone());
            }
        }
        es.shutdownNow();
    }
}
