package org.nomoremagicchoices.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.nomoremagicchoices.SpellKeyVisuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(SpellBarOverlay.class)
public class SpellBarOverlayMixin {

    @Shadow
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/icons.png");

    @Shadow
    static final int CONTEXTUAL_FADE_WAIT = 80;

    @Shadow
    public static int fadeoutDelay;

    @Shadow
    static int lastTick;

    @Shadow
    static float alpha;

    @Shadow
    static int lastSpellCount;

    // 自定义位置缓存
    @Unique
    private static List<Vec2> customSpellBarSlotLocations$nomoremagicchoices = new ArrayList<>();

    /**
     * @author nomoremagicchoices
     * @reason 完全重写法术栏渲染逻辑，实现水平排列和按键提示
     */
    @Overwrite
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui ||
            (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator())) {
            return;
        }
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
        Player player = Minecraft.getInstance().player;
        ManaBarOverlay.Display displayMode = ClientConfigs.SPELL_BAR_DISPLAY.get();
        if (displayMode == ManaBarOverlay.Display.Never || player == null) {
            return;
        } else if (displayMode == ManaBarOverlay.Display.Contextual) {
            handleFading(player);
            if (fadeoutDelay <= 0) {
                return;
            }
        } else {
            alpha = 1f;
        }

        var ssm = ClientMagicData.getSpellSelectionManager();
        if (ssm.getSpellCount() != lastSpellCount) {
            lastSpellCount = ssm.getSpellCount();
            generateCustomHorizontalLocations$nomoremagicchoices();
            if (displayMode == ManaBarOverlay.Display.Contextual) {
                fadeoutDelay = CONTEXTUAL_FADE_WAIT;
            }
        }
        if (ssm.getSpellCount() <= 0) {
            return;
        }

        // 计算中心位置 - 改为水平居中显示，与物品栏同一水平
        int centerX = screenWidth / 2;
        int centerY = screenHeight - 22; // 与物品栏同一水平

        // 应用用户配置偏移
        int configOffsetY = ClientConfigs.SPELL_BAR_Y_OFFSET.get();
        int configOffsetX = ClientConfigs.SPELL_BAR_X_OFFSET.get();
        centerX += configOffsetX;
        centerY += configOffsetY;

        // 渲染法术
        List<SpellData> spells = ssm.getAllSpells().stream().map((slot) -> slot.spellData).toList();
        var locations = customSpellBarSlotLocations$nomoremagicchoices;
        int selectedSpellIndex = ssm.getGlobalSelectionIndex();

        // 第一层：槽位背景边框（灰暗）
        prepTranslucency();
        for (Vec2 location : locations) {
            guiHelper.blit(TEXTURE, centerX + (int) location.x, centerY + (int) location.y, 66, 84, 22, 22);
        }

        // 第二层：暗色边框（稍微灰暗）
        RenderSystem.setShaderColor(0.7f, 0.7f, 0.7f, alpha * 0.8f); // 降低亮度和透明度
        for (int i = 0; i < locations.size(); i++) {
            if (i != selectedSpellIndex) { // 非选中的法术使用暗色边框
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 22, 84, 22, 22);
            }
        }
        flushTranslucency();

        // 第三层：法术图标（明亮，不受透明度影响）
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // 完全不透明和明亮
        for (int i = 0; i < locations.size(); i++) {
            guiHelper.blit(spells.get(i).getSpell().getSpellIconResource(),
                centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3,
                0, 0, 16, 16, 16, 16);
        }

        // 第四层：冷却遮罩（在图标之上）
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        for (int i = 0; i < locations.size(); i++) {
            float f = ClientMagicData.getCooldownPercent(spells.get(i).getSpell());
            if (f > 0) {
                int pixels = (int) (16 * f + 1f);
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels, 47, 87, 16, pixels);
            }
        }

        // 第五层：选中的明亮边框（最顶层，完全明亮）
        RenderSystem.setShaderColor(1.2f, 1.2f, 1.0f, 1.0f); // 稍微增强亮度，带一点暖色调
        for (int i = 0; i < locations.size(); i++) {
            if (i == selectedSpellIndex) {
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 0, 84, 22, 22);
            }
        }

        // 恢复渲染状态
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 渲染按键提示
        renderKeyHints$nomoremagicchoices(guiHelper, centerX, centerY, locations);
    }

    // 生成自定义智能排列的位置
    @Unique
    private void generateCustomHorizontalLocations$nomoremagicchoices() {
        List<Vec2> locations = new ArrayList<>();
        var spellSelectionManager = ClientMagicData.getSpellSelectionManager();
        int spellCount = spellSelectionManager.getSpellCount();

        // 固定槽位宽度为 22 像素
        final int SLOT_WIDTH = 22;

        if (spellCount <= 7) {
            // 法术数量<=7时，全部显示在物品栏左边
            for (int i = 0; i < spellCount; i++) {
                // 计算相对于物品栏左边的偏移
                // 物品栏左边缘大约在中心点-91像素处，我们从那里向左排列
                float x = -91 - (spellCount - i) * SLOT_WIDTH;
                locations.add(new Vec2(x, 0));
            }
        } else {
            // 法术数量>7时，前7个在左边，剩余的在右边
            for (int i = 0; i < spellCount; i++) {
                if (i < 7) {
                    // 前7个法术在物品栏左边
                    float x = -91 - (7 - i) * SLOT_WIDTH;
                    locations.add(new Vec2(x, 0));
                } else {
                    // 超出7个的法术在物品栏右边
                    int rightIndex = i - 7; // 右边的索引从0开始
                    float x = 91 + (rightIndex + 1) * SLOT_WIDTH;
                    locations.add(new Vec2(x, 0));
                }
            }
        }

        customSpellBarSlotLocations$nomoremagicchoices = locations;
    }

    // 渲染按键提示
    @Unique
    private void renderKeyHints$nomoremagicchoices(GuiGraphics guiHelper, int centerX, int centerY, List<Vec2> locations) {
        if (locations == null || locations.isEmpty()) return;

        // 渲染按键提示
        int actualSpellCount = Math.min(locations.size(), 10);
        for (int i = 0; i < actualSpellCount; i++) {
            Vec2 location = locations.get(i);

            // 检查按键是否绑定
            if (!isSpellKeyBound$nomoremagicchoices(i)) {
                continue;
            }

            // 按键提示位置：法术图标中心上方，向下移动2像素
            int keyX = centerX + (int) location.x + 11; // 法术槽位中心 (22/2 = 11)
            int keyY = centerY + (int) location.y - 6; // 法术槽位上方6像素（原来是8像素，现在向下移动2像素）

            SpellKeyVisuals.renderNumberKey(guiHelper, keyX, keyY, i);
        }
    }

    // 检查指定索引的法术按键是否已绑定
    @Unique
    private boolean isSpellKeyBound$nomoremagicchoices(int spellIndex) {
        try {
            Minecraft mc = Minecraft.getInstance();

            // 首先收集所有法术按键并按正确顺序排序
            List<net.minecraft.client.KeyMapping> spellKeys = new ArrayList<>();
            for (net.minecraft.client.KeyMapping binding : mc.options.keyMappings) {
                String keyName = binding.getName();
                if (keyName.contains("spell_quick_cast")) {
                    spellKeys.add(binding);
                }
            }

            // 按按键名称中的数字排序
            spellKeys.sort((a, b) -> {
                int numA = extractSpellNumber$nomoremagicchoices(a.getName());
                int numB = extractSpellNumber$nomoremagicchoices(b.getName());
                return Integer.compare(numA, numB);
            });

            // 检查对应索引的按键是否绑定
            if (spellIndex >= 0 && spellIndex < spellKeys.size()) {
                net.minecraft.client.KeyMapping spellKey = spellKeys.get(spellIndex);
                return spellKey != null && !spellKey.isUnbound();
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 从按键名称中提取数字
    @Unique
    private int extractSpellNumber$nomoremagicchoices(String keyName) {
        try {
            // 尝试从按键名称中提取数字
            String[] parts = keyName.split("[._]");
            for (String part : parts) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                    // 继续尝试下一个部分
                }
            }

            // 如果没有找到数字，尝试从末尾提取
            for (int i = keyName.length() - 1; i >= 0; i--) {
                if (Character.isDigit(keyName.charAt(i))) {
                    int start = i;
                    while (start > 0 && Character.isDigit(keyName.charAt(start - 1))) {
                        start--;
                    }
                    return Integer.parseInt(keyName.substring(start, i + 1));
                }
            }

        } catch (Exception e) {
            // 忽略错误，返回默认值
        }
        return 0; // 默认返回0
    }

    @Shadow
    private static void handleFading(Player player) {
        if (lastTick != player.tickCount) {
            lastTick = player.tickCount;
            if (ClientMagicData.isCasting() || ClientMagicData.getCooldowns().hasCooldownsActive() || ClientMagicData.getRecasts().hasRecastsActive()) {
                fadeoutDelay = CONTEXTUAL_FADE_WAIT;
            }
            if (fadeoutDelay > 0) {
                fadeoutDelay--;
            }
        }
        alpha = Mth.clamp(fadeoutDelay / 20f, 0, 1);
        if (fadeoutDelay <= 0) {
            return;
        }
    }

    @Shadow
    private static void prepTranslucency() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    @Shadow
    private static void flushTranslucency() {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
