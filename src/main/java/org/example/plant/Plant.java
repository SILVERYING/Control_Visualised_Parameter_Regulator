package org.example.plant;

/**
 * 被控对象(Plant)接口
 * 代表物理系统或过程
 */
public interface Plant {
    /**
     * 执行仿真步进
     * 
     * @param input 控制输入
     * @return 新的过程变量值
     */
    double update(double input);

    /**
     * 获取当前状态
     * 
     * @return 过程变量值
     */
    double getState();

    /**
     * 重置系统状态
     */
    void reset();

    /**
     * 获取系统名称
     */
    String getName();
}
