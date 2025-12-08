package org.nomoremagicchoices.api.selection;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.event.ChangeSpellEvent;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.gui.SpellSelectionProvider;
import org.nomoremagicchoices.gui.component.IMoveWight;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientScrollData {


    public static Minecraft mc;

    public static SpellSelectionState state = SpellSelectionState.EmptyHand;
    public static final int TOTAL_TICKS = 8;
    public static int cTick = 0;
    public static boolean isRunning = false;

    private static boolean isSlideUp = false;

    private static ClientScrollData instance = new ClientScrollData();

    private ClientScrollData() {
        mc = Minecraft.getInstance();
    }

    public static void tickHandle(){
        handleRunning();
        handleCurrentTick();
        handleWightTick();
        handleState();
    }

    private static void handleState() {
        if (mc.player != null) {
            var player = mc.player;
            var mainHand = player.getMainHandItem();


            if (mainHand.isEmpty() && player.getOffhandItem().isEmpty()) {
                state = SpellSelectionState.EmptyHand;
            } else if (mainHand.is(TagInit.SKILL_WEAPON)) {
                state = SpellSelectionState.Weapon;
            } else if (player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT) ||player.getOffhandItem().has(ComponentRegistry.CASTING_IMPLEMENT)) {
                state = SpellSelectionState.Staff;
            }
        }
    }

    public static void renderHandle(GuiGraphics context,DeltaTracker partialTick){
        handleWightRender(context,partialTick);

    }


    private static void handleWightRender(GuiGraphics context, DeltaTracker partialTick) {
        if (spellWightList == null) return;

        spellWightList.stream()
                .map(Pair::getFirst)
                .forEach(wight -> wight.render(context, partialTick));
    }

    private static void handleWightTick() {
        if (spellWightList == null) return;

        spellWightList.stream()
                .map(Pair::getFirst)
                .forEach(IMoveWight::tick);
    }

    public static void handleRunning(){
        if (ModKeyMapping.CHANG_GROUP.get().consumeClick() && !isRunning){
            isRunning = true;

            // 切换滑动方向
            isSlideUp = !isSlideUp;

            Nomoremagicchoices.LOGGER.info("按下R键，切换方向: " + (isSlideUp ? "向上" : "向下") + ", isRunning: " + isRunning + ", cTick: " + cTick);

            // 确保spellWightList已初始化
            if (spellWightList == null || spellWightList.isEmpty()) {
                Nomoremagicchoices.LOGGER.warn("spellWightList 未初始化，正在初始化...");
                update();
            }

            // 检查索引是否有效并移动所有Widget
            if (spellWightList != null && !spellWightList.isEmpty()) {
                for (int i = 0; i < spellWightList.size(); i++) {
                    IMoveWight wight = spellWightList.get(i).getFirst();

                    // 计算目标位置
                    int initialX = 100;
                    int initialY = 100 + (i * 30); // 初始Y坐标

                    int targetX = initialX;
                    int targetY;

                    if (isSlideUp) {
                        // 向上滑动：Y坐标减小（向屏幕上方移动）
                        targetY = initialY - 200;
                    } else {
                        // 向下滑动：回到初始位置
                        targetY = initialY;
                    }

                    wight.moveTo(new Vector2i(targetX, targetY));
                    Nomoremagicchoices.LOGGER.info("Widget " + i + " 移动: " + (isSlideUp ? "向上" : "向下") + " 到 (" + targetX + ", " + targetY + ")");
                }
            } else {
                Nomoremagicchoices.LOGGER.warn("spellWightList 为空，无法移动Widget");
            }
        }
    }
    public static void handleCurrentTick(){
        if (isRunning){
            cTick++;
            if (cTick >= TOTAL_TICKS) {
                cTick = 0; // 达到8后归0
                isRunning = false; // 允许下次触发
            }
        }
    }


    //每次更新法术槽位时更新
    public static List<Pair<ScrollSpellWight,Integer>> update(){
        Nomoremagicchoices.LOGGER.info("开始初始化 Widget 列表");


        SpellGroupData groupData = getSpellGroupData();
        groupData.updateSpells();


        int groupCount = SpellGroupData.getGroupCount();


        spellWightList = NonNullList.withSize(groupCount, Pair.of(ScrollSpellWight.EMPTY, 0));

        for (int i = 0; i < groupCount; i++) {
            List<SpellData> groupSpells = groupData.getSpellsByIndex(i);

            int initialX = 100; // 屏幕中心偏左
            int initialY = 100 + (i * 30); // 每个widget垂直间隔30像素

            ScrollSpellWight wight = ScrollSpellWight.create(initialX, initialY, groupSpells);

            // 将widget和索引存储到列表中
            spellWightList.set(i, Pair.of(wight, i));
        }

        Nomoremagicchoices.LOGGER.info("初始化了 " + groupCount + " 个法术组Widget");

        return spellWightList;
    }

    private static List<Pair<ScrollSpellWight,Integer>> spellWightList;

    public static void drawWight(){

    }
    public static void addWight(){
        
    }

    public static SpellGroupData getSpellGroupData(){

        return SpellGroupData.instance;
    }

    public static List<Pair<ScrollSpellWight, Integer>> getSpellWightList() {
        if (spellWightList == null) {
            update();
        }
        return spellWightList;
    }

    @SubscribeEvent
    public static void updateHandle(ChangeSpellEvent event){
        update();
    }
}
