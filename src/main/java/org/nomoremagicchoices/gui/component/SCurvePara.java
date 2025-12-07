package org.nomoremagicchoices.gui.component;




public class SCurvePara {

    // 距离
    private double totalDistance;
    // 位置参数 (Position Parameters)
    private double startPosition;      // 起始位置
    private double endPosition;        // 最终位置

    // 速度参数 (Velocity Parameters)
    private double maxVelocity;        // 最大速度(pixel/tick)
    private double currentVelocity;    // 当前速度

    // 加速度参数 (Max Acceleration Parameters)
    private double maxAcceleration;       // 最大加速度


    // 时间参数 (Time Parameters)
    private int totalTicks;            // 总时间 (单位: tick)
    private int currentTick;           // 当前时间点 (单位: tick)



    // 可选: 加加速度参数 (Jerk Parameters) - 用于更平滑的S曲线
    private double jerk;               // 加加速度 (acceleration的变化率)



    /**
     * 构造函数 - 基础参数
     * @param startPosition 起始位置
     * @param endPosition 最终位置
     * @param maxVelocity 最大速度
     * @param maxAcceleration 加速度
     * @param totalTicks 总时间(ticks)
     */
    public SCurvePara(double startPosition, double endPosition,
                      double maxVelocity, double maxAcceleration, int totalTicks) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.totalDistance = Math.abs(endPosition - startPosition);
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.totalTicks = totalTicks;
        this.currentTick = 0;
        this.currentVelocity = 0.0;
    }

    public double jerk(){
        double jerk = ((Math.pow(maxAcceleration,2))*maxVelocity)
                / (maxAcceleration*maxVelocity*totalTicks-Math.pow(maxVelocity,2)-totalDistance*maxAcceleration);
        return jerk;
    }

}
