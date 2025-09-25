#!/bin/bash

# 打印启动信息
echo "=================================="
echo "Starting fish-chat application..."
echo "=================================="

# 等待文件生成
while [ ! -f bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar ]; do
    echo "Waiting for jar file to be generated..."
    sleep 2
done

# 启动应用
java -jar bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar