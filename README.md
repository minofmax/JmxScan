# jmxscanner

jmxscanner

## 使用

1. 命令行指定扫描目标

   java -jar JxmScan-1.0-SNAPSHOT.jar 127.0.0.1 8080

   一次只能扫描一个目标

2. 指定扫描目标文件

   java -jar JxmScan-1.0-SNAPSHOT.jar [file_path]

   目标文件数据格式：每行 ip:port

3. 默认模式

   java -jar JxmScan-1.0-SNAPSHOT.jar

   需要在jar包目录下放一个targets.txt作为扫描目标

   

## 打包

mvn clean compile package