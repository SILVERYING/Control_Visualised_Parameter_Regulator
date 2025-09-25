软件环境
JDK：Oracle OpenJDK 17（必须）
构建工具：Maven 3.8+
依赖库：
JFreeChart 1.5.3（图表）
Apache Commons Math 3.6.1（FFT）
Gson 2.10.1（JSON序列化）

出错信息	触发条件
Auto-tune failed: Not enough oscillation...	  自动整定时振荡周期数不足
Auto-tune failed: Invalid oscillation data	  自动整定时采集的幅值或周期数据无效
No data available for analysis	FFT           分析时曲线无数据
Error saving file	                            保存文件失败（权限/路径/磁盘不足）
Error loading or parsing file	                读取文件失败 或 JSON 格式错误
Invalid input for                             参数名	参数文本框输入非数字
