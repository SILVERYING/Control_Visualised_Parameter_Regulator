软件环境
JDK：Oracle OpenJDK 17（必须）
构建工具：Maven 3.8+
依赖库：
JFreeChart 1.5.3（图表绘制）
Apache Commons Math 3.6.1（FFT 计算）
Gson 2.10.1（JSON 序列化 / 反序列化）

常见错误信息及触发条件
出错信息	触发条件
Auto-tune failed: Not enough oscillation...	自动整定时振荡周期数不足
Auto-tune failed: Invalid oscillation data	自动整定时采集的幅值或周期数据无效
No data available for analysis	FFT 分析时曲线无数据
Error saving file	保存文件失败（权限 / 路径 / 磁盘不足）
Error loading or parsing file	读取文件失败或 JSON 格式错误
Invalid input for 参数名	参数文本框输入非数字

功能结构图
控制系统可视化参数调节器
1. 仿真控制层（ControlVisualizer）
启动仿真
停止仿真
系统复位
自动整定
打开数据管理器
频域分析 (FFT)
2. 控制算法层
ControlAlgorithm 接口
PIDController 实现
正常运行模式
自动整定模式（继电器输出）
三种 Z-N 参数规则
3. 数据管理层
保存运行（SimulationRun）
加载运行（JSON）
对比曲线显示
删除运行记录
4. 数据表现层
ChartPanel 实时曲线
RealTimeDisplayPanel 数值 + 光柱
RunManagerDialog 管理运行
FrequencyAnalysisDialog 频谱分析
5. 数据计算层
PerformanceMetricsCalculator
上升时间
超调量
稳定时间
积分绝对误差（IAE）
