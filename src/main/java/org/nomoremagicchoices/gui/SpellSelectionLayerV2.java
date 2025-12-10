package org.nomoremagicchoices.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ClientScrollData;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.List;

/**
 * 法术选择层V2
 * 负责渲染法术选择界面，所有逻辑由ClientScrollData处理
 * 该层只从ClientScrollData获取数据并进行渲染
 */
public class SpellSelectionLayerV2 implements ILayerState {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Nomoremagicchoices.MODID, "textures/gui/icons.png");

    private int screenWidth;
    private int screenHeight;

    /**
     * 渲染方法
     * 从ClientScrollData获取Widget列表并渲染
     */
    @Override
    public void render(GuiGraphics context, DeltaTracker partialTick) {
        // 更新屏幕尺寸
        updateScreenSize(context);

        // 执行ClientScrollData的tick处理
        ClientScrollData.tickHandle();

        // 获取Widget列表并渲染
        List<ScrollSpellWight> wightList = ClientScrollData.getSpellWightList();

        if (wightList == null || wightList.isEmpty()) {
            return;
        }

        // 渲染所有Widget
        for (ScrollSpellWight wight : wightList) {
            if (wight != null && wight != ScrollSpellWight.EMPTY) {
                wight.render(context, partialTick);
            }
        }
    }

    /**
     * 更新屏幕尺寸
     */
    private void updateScreenSize(GuiGraphics guiGraphics) {
        this.screenHeight = guiGraphics.guiHeight();
        this.screenWidth = guiGraphics.guiWidth();
    }

}

