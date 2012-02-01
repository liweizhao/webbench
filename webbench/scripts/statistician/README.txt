记录用例运行之前，之中，之后的一些状态信息；

使用方法：
0. 获取帮助： python statistician --help
1. 创建用例，只需运行一次
    python statistician create [ -H host -u user -p password -P port -t tables -e engine -o output]
    其中-e为测试表引擎，-o为结果输出目录， -t tables的格式为"db1:t1 t2;db2 t3;"
2. 开始用例，运行测试之前运行
    python statistician start  -i iteration -o output
    i是测试期间查询infornmation_schema的次数
3. 结束用例，运行测试之后运行
    python statistician stop -o output
4. 清除用例运行中间文件
    python statistician clean -o output
5. 删除用例（不删除用例结果）
    python statistician drop -o output
