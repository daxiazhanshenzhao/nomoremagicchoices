package org.nomoremagicchoices.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.event.RenderBgEvent;
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

    /** Widget在Y轴的垂直间隔（像素） */
    public static final int WIDGET_VERTICAL_SPACING = 8;

    /** Focus状态下Widget相对于基础位置的Y轴偏移（像素，负值表示向上） */
    public static final int FOCUS_Y_OFFSET = -50;

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

        // 渲染所有Widget - 从大到小倒序渲染，让小index的Widget显示在最上层
        for (int i = wightList.size() - 1; i >= 0; i--) {
            ScrollSpellWight wight = wightList.get(i);
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


    @Override
    public void renderBg(ResourceLocation texture, GuiGraphics context, int x, int y,int uOffset,int vOffset, int width, int height) {
        RenderBgEvent event = new RenderBgEvent(texture, context, x, y,uOffset, vOffset, width, height);
        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) return;

        context.blit(event.getTexture(), event.getX(), event.getY(), 0, 0, event.getWidth(), event.getHeight());

    }
}

