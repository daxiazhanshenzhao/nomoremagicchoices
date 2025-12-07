f% Smoothstep 函数详细分析
% 专注于当前Java代码中使用的缓动函数
% 公式: f(t) = 3t² - 2t³ = t² * (3 - 2t)
% 日期: 2025-12-07

clear all;
close all;
clc;

%% 定义Smoothstep函数
smoothstep = @(t) t.^2 .* (3 - 2*t);

% 创建高精度时间序列
t = linspace(0, 1, 1000);
y = smoothstep(t);

%% 创建可视化
figure('Position', [100, 100, 1400, 900], 'Name', 'Smoothstep 函数完整分析');

% 子图1: 函数曲线
subplot(2, 3, 1);
plot(t, y, 'r-', 'LineWidth', 3);
hold on;
plot(t, t, 'k--', 'LineWidth', 1.5);
grid on;
xlabel('输入 t', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('输出 f(t)', 'FontSize', 12, 'FontWeight', 'bold');
title('Smoothstep: f(t) = 3t² - 2t³', 'FontSize', 14, 'FontWeight', 'bold');
legend('Smoothstep', 'Linear', 'Location', 'northwest');
axis([0 1 0 1]);
hold off;

% 子图2: 速度曲线 (一阶导数)
subplot(2, 3, 2);
% 解析导数: f'(t) = 6t - 6t²
velocity = 6*t - 6*t.^2;
plot(t, velocity, 'b-', 'LineWidth', 3);
hold on;
plot([0 1], [1 1], 'k--', 'LineWidth', 1.5);
yline(0, 'k-', 'LineWidth', 1);
grid on;
xlabel('时间 t', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('速度 f''(t)', 'FontSize', 12, 'FontWeight', 'bold');
title('速度曲线: f''(t) = 6t - 6t²', 'FontSize', 14, 'FontWeight', 'bold');
legend('Smoothstep速度', 'Linear速度', 'Location', 'north');
hold off;

% 子图3: 加速度曲线 (二阶导数)
subplot(2, 3, 3);
% 解析二阶导数: f''(t) = 6 - 12t
acceleration = 6 - 12*t;
plot(t, acceleration, 'g-', 'LineWidth', 3);
hold on;
yline(0, 'k--', 'LineWidth', 1.5);
grid on;
xlabel('时间 t', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('加速度 f''''(t)', 'FontSize', 12, 'FontWeight', 'bold');
title('加速度曲线: f''''(t) = 6 - 12t', 'FontSize', 14, 'FontWeight', 'bold');
text(0.2, 3, '正加速 (加速阶段)', 'FontSize', 11, 'Color', 'g');
text(0.6, -3, '负加速 (减速阶段)', 'FontSize', 11, 'Color', 'r');
hold off;

% 子图4: 与其他函数对比
subplot(2, 3, 4);
hold on;
plot(t, t, 'k--', 'LineWidth', 2, 'DisplayName', 'Linear');
plot(t, smoothstep(t), 'r-', 'LineWidth', 3, 'DisplayName', 'Smoothstep');
plot(t, t.^2, 'b-', 'LineWidth', 2, 'DisplayName', 'Quadratic (t²)');
plot(t, sqrt(t), 'm-', 'LineWidth', 2, 'DisplayName', 'Square Root (√t)');
grid on;
xlabel('时间 t', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('位置', 'FontSize', 12, 'FontWeight', 'bold');
title('与基本函数对比', 'FontSize', 14, 'FontWeight', 'bold');
legend('Location', 'northwest');
axis([0 1 0 1]);
hold off;

% 子图5: Java代码模拟 (20个tick)
subplot(2, 3, 5);
TOTAL_TICKS = 20;
ticks = 0:TOTAL_TICKS;
% 模拟Java中的offset计算
tick_offsets = ticks / TOTAL_TICKS;
tick_positions = smoothstep(tick_offsets);

hold on;
% 绘制连续曲线
plot(t * TOTAL_TICKS, y, 'r-', 'LineWidth', 2, 'DisplayName', '理论曲线');
% 绘制实际tick点
plot(ticks, tick_positions, 'bo-', 'LineWidth', 1.5, 'MarkerSize', 8, ...
    'MarkerFaceColor', 'b', 'DisplayName', '实际tick点');
grid on;
xlabel('Tick数', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('位置', 'FontSize', 12, 'FontWeight', 'bold');
title(sprintf('游戏中的实际动画 (TOTAL_TICKS=%d)', TOTAL_TICKS), ...
    'FontSize', 14, 'FontWeight', 'bold');
legend('Location', 'northwest');
hold off;

% 子图6: 关键特性展示
subplot(2, 3, 6);
axis off;

% 添加文本说明
text_x = 0.1;
text(text_x, 0.95, 'Smoothstep 函数特性分析', ...
    'FontSize', 16, 'FontWeight', 'bold', 'Color', 'r');

text(text_x, 0.85, '数学公式:', 'FontSize', 13, 'FontWeight', 'bold');
text(text_x, 0.80, '    f(t) = 3t² - 2t³', 'FontSize', 12, 'FontFamily', 'monospace');
text(text_x, 0.75, '    f(t) = t² × (3 - 2t)', 'FontSize', 12, 'FontFamily', 'monospace');

text(text_x, 0.65, '关键点:', 'FontSize', 13, 'FontWeight', 'bold');
text(text_x, 0.60, sprintf('    f(0) = %.4f  (起点)', smoothstep(0)), 'FontSize', 11);
text(text_x, 0.56, sprintf('    f(0.25) = %.4f', smoothstep(0.25)), 'FontSize', 11);
text(text_x, 0.52, sprintf('    f(0.5) = %.4f  (中点)', smoothstep(0.5)), 'FontSize', 11);
text(text_x, 0.48, sprintf('    f(0.75) = %.4f', smoothstep(0.75)), 'FontSize', 11);
text(text_x, 0.44, sprintf('    f(1) = %.4f  (终点)', smoothstep(1)), 'FontSize', 11);

text(text_x, 0.34, '速度特性:', 'FontSize', 13, 'FontWeight', 'bold');
text(text_x, 0.29, '    最大速度在 t=0.5 处', 'FontSize', 11);
text(text_x, 0.25, sprintf('    v_max = %.4f', max(velocity)), 'FontSize', 11);
text(text_x, 0.21, '    起点和终点速度 = 0', 'FontSize', 11);

text(text_x, 0.11, 'Java实现代码:', 'FontSize', 13, 'FontWeight', 'bold');
text(text_x, 0.06, '    realOffset = t * t * (3.0 - 2.0 * t);', ...
    'FontSize', 11, 'FontFamily', 'monospace', 'BackgroundColor', [0.95 0.95 0.95]);

%% 输出数值表格
fprintf('\n========================================\n');
fprintf('  Smoothstep 函数数值表 (f(t) = 3t² - 2t³)\n');
fprintf('========================================\n\n');
fprintf('时间 t\t\t位置 f(t)\t速度 f''(t)\t加速度 f''''(t)\n');
fprintf('--------------------------------------------------------\n');

key_times = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0];
for t_val = key_times
    pos = smoothstep(t_val);
    vel = 6*t_val - 6*t_val^2;
    acc = 6 - 12*t_val;
    fprintf('%.2f\t\t%.6f\t%.6f\t%.6f\n', t_val, pos, vel, acc);
end

fprintf('\n========================================\n');
fprintf('  动画特性总结\n');
fprintf('========================================\n\n');
fprintf('✓ 开始阶段 (t: 0 → 0.5):\n');
fprintf('  - 从静止开始 (v=0)\n');
fprintf('  - 正加速度，速度逐渐增加\n');
fprintf('  - 给人"缓缓启动"的感觉\n\n');

fprintf('✓ 结束阶段 (t: 0.5 → 1.0):\n');
fprintf('  - 负加速度，速度逐渐减小\n');
fprintf('  - 最终静止 (v=0)\n');
fprintf('  - 给人"平稳停止"的感觉\n\n');

fprintf('✓ 最适合场景:\n');
fprintf('  - UI元素滚动动画\n');
fprintf('  - 相机平滑移动\n');
fprintf('  - 物品栏切换\n');
fprintf('  - 任何需要自然开始和结束的动画\n\n');

%% 保存图像
saveas(gcf, 'smoothstep_detailed_analysis.png');
fprintf('详细分析图已保存为: smoothstep_detailed_analysis.png\n\n');

