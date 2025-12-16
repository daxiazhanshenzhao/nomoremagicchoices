package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.gui.component.AbstractWight;
import org.nomoremagicchoices.gui.component.EmptyWight;
import org.nomoremagicchoices.gui.component.Moving;
import org.nomoremagicchoices.gui.component.ScrollSpellWightV2;
import org.nomoremagicchoices.gui.component.WightContext;
import org.nomoremagicchoices.gui.component.State;

import java.util.List;

public class ScrollWightData {


    private static final int SCROLL_WIGHT_LIST_SIZE = 4;
    private static final int SCROLL_WIGHT_FOCUS_INDEX = 0;
    private static final int SCROLL_WIGHT_HEIGHT = 8;

    private static final int FOCUS_HEIGHT = 30;


    private static final int centerXOffset = -200;
    private static final int centerYOffset = -50;
    /**
     *
     *    □□□□ 0
     *           ===空着表示是focus渲染的==========   □□□□ 0
     *    □□□□ 1                                   □□□□ 1
     *    □□□□ 2                                   □□□□ 2
     *                                      center↑  (左下角)
     *  固定渲染index为0的位置,
     *
     *
     */
    private List<AbstractWight> scrollWights;
    private Vector2i center;

    public ScrollWightData(SpellGroupData spellGroupData,ClientHandData clientHandData) {
        this.groupData = spellGroupData;
        this.handData = clientHandData;

        update();
    }

    private SpellGroupData groupData;
    private ClientHandData handData;

    /**
     * {@link SpellGroupData#update()} 这个方法触发时会触发update
     */
    public void update(){
        List<AbstractWight> oldScrollWights = scrollWights;
        List<AbstractWight> newScrollWights = getNewScrollWights();
        
        // 如果是第一次初始化，直接设置scrollWights
        if (oldScrollWights == null) {
            scrollWights = newScrollWights;
            return;
        }
        
        if (newScrollWights == null) return;

        // 为每个旧组件设置移动任务
        for(int i = 0; i < oldScrollWights.size(); i++){
            AbstractWight oldWight = oldScrollWights.get(i);
            if (oldWight.compareEqualsSpell(EmptyWight.EMPTY)) continue;
            
            // 找到对应的新组件（通过比较法术）
            for(int j = 0; j < newScrollWights.size(); j++){
                AbstractWight newWight = newScrollWights.get(j);
                if (newWight.compareEqualsSpell(EmptyWight.EMPTY)) continue;
                
                if (oldWight.compareEqualsSpell(newWight)) {
                    // 检查位置是否相同
                    WightContext oldContext = oldWight.getCenter();
                    WightContext newContext = newWight.getCenter();
                    
                    if (!oldContext.position().equals(newContext.position())) {
                        Moving moving = Moving.start();
                        // 创建移动任务
                        moving.addPos(newContext.position());

                        //对Focus做处理
                        if (ClientHandData.isFocus()){
                            //组件从size-1到0
                            if (scrollWights.getLast().compareEqualsSpell(newWight)){
                                var endeY = calculateCenter(0).position().y() - FOCUS_HEIGHT;
                                var ender = new Vector2i(calculateCenter(0).position().x,endeY);

                                moving.addPos(ender);
                                moving.endState(State.Focus);
                            }
                            //从0到其他位置
                            if(scrollWights.getFirst().compareEqualsSpell(oldWight)){

                            }
                        }else{
                            moving.addPos(newContext.position()).endState(State.Down);

                        }




                        oldWight.addTasks(moving);
                    }
                    break;
                }
            }
        }

        //等待移动完成过后，替换原组件
        wightTickHook();

    }//=========================================================
    private final int MOVE_TICKS = 3;
    private int cTick = 0;
    private boolean isTicking = false;

    /**
     * 通过独立内部计时器进行延迟替换
     */
    private void wightTickHook() {
        isTicking = true;
    }

    public void tick(){
        if (isTicking){
//            cTick++;
//            if (cTick == MOVE_TICKS){
//                isTicking = false;
//                cTick = 0;
//                scrollWights = getNewScrollWights();
//            }
            for (AbstractWight wight : scrollWights) {
                if (wight.isRunning()) return;
            }
            isTicking = false;
            scrollWights = getNewScrollWights();
        }
        
        // 检查scrollWights是否为null
        if (scrollWights == null) {
            return;
        }
        
        for(AbstractWight wight : scrollWights){
            if (wight != null && !wight.equals(EmptyWight.EMPTY)){
                wight.tick();
            }
        }
    }

    public List<AbstractWight> getNewScrollWights(){
        var window = Minecraft.getInstance().getWindow();
        center = new Vector2i(window.getGuiScaledWidth()/2 + centerXOffset, window.getGuiScaledHeight() + centerYOffset);

        var cIndex = SpellGroupData.getCurrentGroupIndex();
        var groupCount = SpellGroupData.getGroupCount();


        // 如果组数为0，返回一个空列表
        if (groupCount == 0) {
            return NonNullList.withSize(1, EmptyWight.EMPTY);
        }

        // 创建正确大小的列表
        List<AbstractWight> scrollWights = NonNullList.withSize(groupCount, EmptyWight.EMPTY);

        // 填充当前组（放在索引0位置）
        scrollWights.set(0,new ScrollSpellWightV2(calculateCenter(0),SpellGroupData.getIndexSpells(cIndex),3));

        // 填充当前组之后的组
        int scrollIndex = 1;
        for (int afterGroupIndex = cIndex + 1; afterGroupIndex < groupCount; afterGroupIndex++) {
            if (scrollIndex >= groupCount) break;
            WightContext wightContext = calculateCenter(scrollIndex);
            scrollWights.set(scrollIndex, new ScrollSpellWightV2(
                wightContext,
                SpellGroupData.getIndexSpells(afterGroupIndex),
                3
            ));
            scrollIndex++;
        }

        // 填充当前组之前的组
        for (int beforeGroupIndex = 0; beforeGroupIndex < cIndex; beforeGroupIndex++) {
            if (scrollIndex >= groupCount) break;
            WightContext wightContext = calculateCenter(scrollIndex);
            scrollWights.set(scrollIndex, new ScrollSpellWightV2(
                wightContext,
                SpellGroupData.getIndexSpells(beforeGroupIndex),
                    3
            ));
            scrollIndex++;
        }



        return scrollWights;
    }


    public WightContext calculateCenter(int wightIndex){
        int x = center.x();
        int y;

        int groupCount = SpellGroupData.getGroupCount();
        if (groupCount == 0) {
            return new WightContext(new Vector2i(x, center.y()), State.Down);
        }

        // 修改：当前组（wightIndex=0）在最上面（Y值最小）
        // wightIndex越大，位置越低（Y值越大）
        // 当前组在顶部：y = center.y() - (groupCount - 1) * SCROLL_WIGHT_HEIGHT
        // 其他组依次向下排列
        y = center.y() - (groupCount - 1 - wightIndex) * SCROLL_WIGHT_HEIGHT;

        // 对于index 0，如果是Focus状态，需要额外上移
        State state = State.Down;
//        if (wightIndex == 0) {
//            // 检查ClientHandData是否为Focus状态
//            if (handData != null && handData.isFocus()){
//                 y = y - FOCUS_HEIGHT;
//                 state = State.Focus;
//            }
//        }
        
        return new WightContext(new Vector2i(x, y), state);
    }

    public List<AbstractWight> getScrollWights() {
        return scrollWights;
    }

    public boolean isTicking() {
        return isTicking;
    }




    /**
     * 对玩家切换手作特殊处理
     */
    public void handleHand(SpellSelectionState oldState,SpellSelectionState newState){
        Nomoremagicchoices.LOGGER.info(oldState.toString() + "旧状态");
        Nomoremagicchoices.LOGGER.info(newState.toString()+ "新状态");

        if (oldState.unFocus() && newState.isFocus()){
            if (ClientMagicData.isCasting()) return;

            //从空手切换到focus
            var endeY = calculateCenter(0).position().y() - FOCUS_HEIGHT;
            var ender = new Vector2i(calculateCenter(0).position().x,endeY);
            scrollWights.getFirst()
                    .addTasks(Moving.start()
                            .addPos(ender)
                            .endState(State.Focus));
        } else if (oldState.isFocus() && newState.unFocus()) {
            //从focus切换到空手

            if (ClientMagicData.isCasting()) return;



            var endeY = calculateCenter(0).position().y();
            var ender = new Vector2i(calculateCenter(0).position().x,endeY);

            scrollWights.getFirst()
                    .addTasks(Moving.start().addPos(ender).endState(State.Down));
        }



        {

        }
        //当玩家从法杖，costing状态强制切换到空手时。


    }

    /**
     * 切换组时，做特殊判定
     */
    public void handleGroup() {
        //focus切换组
        if (ClientHandData.isFocus()){
            var endeY = calculateCenter(0).position().y() - FOCUS_HEIGHT;
            var ender = new Vector2i(calculateCenter(0).position().x,endeY);
            scrollWights.getFirst()
                    .addTasks(Moving.start()
                            .addPos(ender)
                            .endState(State.Focus));
        }
    }
}
