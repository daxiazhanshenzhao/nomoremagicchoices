% 缓动函数可视化脚本
% 用于展示不同缓动曲线的效果对比
% 日期: 2025-12-07

clear all;
close all;
clc;

% 创建时间序列 (0到1)
t = linspace(0, 1, 1000);

%% 1. Smoothstep (当前使用的函数)
% f(t) = 3t² - 2t³
smoothstep = 3 * t.^2 - 2 * t.^3;

%% 2. 线性插值 (对比基准)
linear = t;

%% 3. Sin函数缓动
% f(t) = (sin(π(t - 0.5)) + 1) / 2
sin_easing = (sin((t - 0.5) * pi) + 1) / 2;

%% 4. 开方组合 (分段函数)
sqrt_easing = zeros(size(t));
for i = 1:length(t)
    if t(i) < 0.5
        sqrt_easing(i) = 2 * t(i)^2;
    else
        sqrt_easing(i) = 1 - 2 * (1 - t(i))^2;
    end
end

%% 5. Smootherstep (更平滑的版本)
% f(t) = 6t⁵ - 15t⁴ + 10t³
smootherstep = 6 * t.^5 - 15 * t.^4 + 10 * t.^3;

%% 6. 计算速度 (一阶导数)
dt = t(2) - t(1);
velocity_smoothstep = gradient(smoothstep, dt);
velocity_linear = gradient(linear, dt);
velocity_sin = gradient(sin_easing, dt);
velocity_sqrt = gradient(sqrt_easing, dt);
velocity_smootherstep = gradient(smootherstep, dt);

%% 绘制位置曲线对比图
figure('Position', [100, 100, 1200, 800], 'Name', '缓动函数对比');

% 子图1: 位置曲线
subplot(2, 2, 1);
hold on;
plot(t, linear, 'k--', 'LineWidth', 1.5, 'DisplayName', 'Linear (线性)');
plot(t, smoothstep, 'r-', 'LineWidth', 2.5, 'DisplayName', 'Smoothstep (当前使用)');
plot(t, sin_easing, 'b-', 'LineWidth', 2, 'DisplayName', 'Sin缓动');
plot(t, sqrt_easing, 'g-', 'LineWidth', 2, 'DisplayName', '开方组合');
plot(t, smootherstep, 'm-', 'LineWidth', 2, 'DisplayName', 'Smootherstep');
grid on;
xlabel('时间 t (0-1)', 'FontSize', 12);
ylabel('位置 (0-1)', 'FontSize', 12);
title('位置曲线对比', 'FontSize', 14, 'FontWeight', 'bold');
legend('Location', 'northwest', 'FontSize', 10);
hold off;

% 子图2: 速度曲线
subplot(2, 2, 2);
hold on;
plot(t, velocity_linear, 'k--', 'LineWidth', 1.5, 'DisplayName', 'Linear');
plot(t, velocity_smoothstep, 'r-', 'LineWidth', 2.5, 'DisplayName', 'Smoothstep (当前)');
plot(t, velocity_sin, 'b-', 'LineWidth', 2, 'DisplayName', 'Sin缓动');
plot(t, velocity_sqrt, 'g-', 'LineWidth', 2, 'DisplayName', '开方组合');
plot(t, velocity_smootherstep, 'm-', 'LineWidth', 2, 'DisplayName', 'Smootherstep');
grid on;
xlabel('时间 t (0-1)', 'FontSize', 12);
ylabel('速度 (导数)', 'FontSize', 12);
title('速度曲线对比 (体现加速度变化)', 'FontSize', 14, 'FontWeight', 'bold');
legend('Location', 'northeast', 'FontSize', 10);
hold off;

% 子图3: Smoothstep详细分析
subplot(2, 2, 3);
hold on;
plot(t, smoothstep, 'r-', 'LineWidth', 3);
plot(t, t, 'k--', 'LineWidth', 1.5);
% 标注关键点
plot(0, 0, 'ro', 'MarkerSize', 10, 'MarkerFaceColor', 'r');
plot(0.5, smoothstep(500), 'go', 'MarkerSize', 10, 'MarkerFaceColor', 'g');
plot(1, 1, 'bo', 'MarkerSize', 10, 'MarkerFaceColor', 'b');
text(0.05, 0.05, '起点: 慢速启动', 'FontSize', 11);
text(0.55, 0.5, sprintf('中点: t=0.5, y=%.3f', smoothstep(500)), 'FontSize', 11);
text(0.7, 0.95, '终点: 慢速停止', 'FontSize', 11);
grid on;
xlabel('时间 t', 'FontSize', 12);
ylabel('位置', 'FontSize', 12);
title('Smoothstep 函数详解 (f(t) = 3t² - 2t³)', 'FontSize', 14, 'FontWeight', 'bold');
legend('Smoothstep', 'Linear', 'Location', 'northwest', 'FontSize', 10);
hold off;

% 子图4: 动画模拟 (使用Smoothstep)
subplot(2, 2, 4);
animation_frames = 20;
animation_indices = round(linspace(1, length(t), animation_frames));
hold on;
for i = 1:length(animation_indices)
    idx = animation_indices(i);
    % 绘制当前位置
    plot(t(idx), smoothstep(idx), 'ro', 'MarkerSize', 8, 'MarkerFaceColor', 'r');
    % 绘制轨迹
    plot(t(1:idx), smoothstep(1:idx), 'r-', 'LineWidth', 1.5);
end
plot(t, smoothstep, 'b--', 'LineWidth', 1, 'DisplayName', '完整轨迹');
grid on;
xlabel('时间 t', 'FontSize', 12);
ylabel('位置', 'FontSize', 12);
title(sprintf('动画模拟 (共%d帧)', animation_frames), 'FontSize', 14, 'FontWeight', 'bold');
hold off;

%% 输出数值对比表
fprintf('\n========== 缓动函数关键点数值对比 ==========\n\n');
fprintf('时间点 t\t\tLinear\t\tSmoothstep\tSin缓动\t\t开方组合\tSmootherstep\n');
fprintf('-----------------------------------------------------------------------------------\n');
key_points = [0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0];
for tp = key_points
    idx = round(tp * (length(t) - 1)) + 1;
    fprintf('%.2f\t\t\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f\n', ...
        tp, linear(idx), smoothstep(idx), sin_easing(idx), sqrt_easing(idx), smootherstep(idx));
end

fprintf('\n========== 速度特性分析 ==========\n\n');
fprintf('函数名称\t\t最大速度\t\t最小速度\t\t平均速度\n');
fprintf('------------------------------------------------------------------\n');
fprintf('Linear\t\t\t%.4f\t\t%.4f\t\t%.4f\n', max(velocity_linear), min(velocity_linear), mean(velocity_linear));
fprintf('Smoothstep\t\t%.4f\t\t%.4f\t\t%.4f\n', max(velocity_smoothstep), min(velocity_smoothstep), mean(velocity_smoothstep));
fprintf('Sin缓动\t\t\t%.4f\t\t%.4f\t\t%.4f\n', max(velocity_sin), min(velocity_sin), mean(velocity_sin));
fprintf('开方组合\t\t%.4f\t\t%.4f\t\t%.4f\n', max(velocity_sqrt), min(velocity_sqrt), mean(velocity_sqrt));
fprintf('Smootherstep\t%.4f\t\t%.4f\t\t%.4f\n', max(velocity_smootherstep), min(velocity_smootherstep), mean(velocity_smootherstep));

fprintf('\n========== 推荐使用场景 ==========\n\n');
fprintf('1. Smoothstep (当前使用): 最佳平衡，适合大多数UI动画\n');
fprintf('2. Sin缓动: 最自然平滑，适合需要柔和感觉的场景\n');
fprintf('3. 开方组合: 加速明显，适合快速响应的交互\n');
fprintf('4. Smootherstep: 极致平滑，适合慢动作或强调动画\n');
fprintf('5. Linear: 匀速运动，适合机械式移动\n\n');

%% 保存图像
saveas(gcf, 'easing_functions_comparison.png');
fprintf('图像已保存为: easing_functions_comparison.png\n\n');

