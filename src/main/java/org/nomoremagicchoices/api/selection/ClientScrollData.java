package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector2i;
import org.nomoremagicchoices.api.event.ChangeSpellEvent;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.gui.SpellSelectionLayerV2;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

/**
 * Client-side scroll data manager for spell widgets
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientScrollData {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int TOTAL_TICKS = 8;

    private static SpellSelectionState state = SpellSelectionState.EmptyHand;
    private static int currentTick = 0;
    private static boolean isRunning = false;
    private static List<ScrollSpellWight> spellWightList;

    private ClientScrollData() {}

    public static void tickHandle() {
        handleKeyPress();
        updateTick();
        updateWidgets();
        updateState();
        syncSelection();
    }

    private static void updateState() {
        if (mc.player == null) return;

        var player = mc.player;
        var mainHand = player.getMainHandItem();
        var offHand = player.getOffhandItem();
        SpellSelectionState oldState = state;

        if (mainHand.isEmpty() && offHand.isEmpty()) {
            state = SpellSelectionState.EmptyHand;
        } else if (mainHand.is(TagInit.SKILL_WEAPON)) {
            state = SpellSelectionState.Weapon;
        } else if (mainHand.has(ComponentRegistry.CASTING_IMPLEMENT) || offHand.has(ComponentRegistry.CASTING_IMPLEMENT)) {
            state = SpellSelectionState.Staff;
        } else {
            state = SpellSelectionState.EmptyHand;
        }

        if (oldState != state) {
            handleStateChange(state);
        }
    }

    private static void handleStateChange(SpellSelectionState newState) {
        if (spellWightList == null || spellWightList.isEmpty()) return;

        List<Vector2i> positions = calculatePositions(newState);

        if (newState == SpellSelectionState.EmptyHand) {
            for (int i = 0; i < spellWightList.size(); i++) {
                spellWightList.get(i).moveDown(positions.get(i));
            }
        } else {
            spellWightList.getFirst().moveFocus(positions.getFirst());
            for (int i = 1; i < spellWightList.size(); i++) {
                spellWightList.get(i).moveDown(positions.get(i));
            }
        }
    }

    private static void updateWidgets() {
        if (spellWightList != null) {
            spellWightList.forEach(ScrollSpellWight::tick);
        }
    }

    private static void syncSelection() {
        if (spellWightList == null || spellWightList.isEmpty()) return;

        if (SpellGroupData.instance.syncGroupFromSelection()) {
            switchToGroup(SpellGroupData.instance.getCurrentGroupIndex());
        }
    }

    private static void handleKeyPress() {
        boolean nextPressed = ModKeyMapping.NEXT_GROUP.get().consumeClick();
        boolean prevPressed = ModKeyMapping.PREV_GROUP.get().consumeClick();
        boolean changPressed = ModKeyMapping.CHANG_GROUP.get().consumeClick();

        if ((nextPressed || prevPressed || changPressed) && !isRunning) {
            isRunning = true;

            if (spellWightList == null || spellWightList.isEmpty()) {
                update();
                return;
            }

            int currentGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();
            int totalGroups = SpellGroupData.getGroupCount();
            int nextGroupIndex = prevPressed
                ? (currentGroupIndex - 1 + totalGroups) % totalGroups
                : (currentGroupIndex + 1) % totalGroups;

            SpellGroupData.instance.setCurrentGroupIndex(nextGroupIndex);
            switchToGroup(nextGroupIndex);
        }
    }

    private static void switchToGroup(int targetGroupIndex) {
        if (spellWightList == null || spellWightList.isEmpty()) return;

        int targetListIndex = -1;
        for (int i = 0; i < spellWightList.size(); i++) {
            if (spellWightList.get(i).getGroupIndex() == targetGroupIndex) {
                targetListIndex = i;
                break;
            }
        }

        if (targetListIndex != -1) {
            List<Vector2i> positions = calculatePositions(state);
            boolean isEmptyHand = (state == SpellSelectionState.EmptyHand);
            ScrollGroupHelper.drawWight(spellWightList, targetListIndex, positions, isEmptyHand);
        }
    }

    private static void updateTick() {
        if (isRunning) {
            currentTick++;
            if (currentTick >= TOTAL_TICKS) {
                currentTick = 0;
                isRunning = false;
            }
        }
    }

    /**
     * Updates the widget list when spell slots change.
     * Creates widgets for all groups and initializes their states.
     */
    public static void update() {
        SpellGroupData groupData = SpellGroupData.instance;
        groupData.updateSpells();

        int groupCount = SpellGroupData.getGroupCount();
        if (groupCount == 0) {
            spellWightList = NonNullList.create();
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        List<Vector2i> positions = calculatePositionsInternal(groupCount, screenWidth, screenHeight, state);

        List<ScrollSpellWight> tempList = NonNullList.withSize(groupCount, ScrollSpellWight.EMPTY);
        int currentGroupIndex = groupData.getCurrentGroupIndex();

        for (int i = 0; i < groupCount; i++) {
            List<SpellData> groupSpells = groupData.getSpellsByIndex(i);
            tempList.set(i, ScrollSpellWight.create(positions.get(i).x, positions.get(i).y, groupSpells, i));
        }

        spellWightList = NonNullList.withSize(groupCount, ScrollSpellWight.EMPTY);
        spellWightList.set(0, tempList.get(currentGroupIndex));

        int insertIndex = 1;
        for (int i = 0; i < groupCount; i++) {
            if (i != currentGroupIndex) {
                spellWightList.set(insertIndex++, tempList.get(i));
            }
        }

        for (int i = 0; i < spellWightList.size(); i++) {
            ScrollSpellWight wight = spellWightList.get(i);
            if (state == SpellSelectionState.EmptyHand) {
                wight.down();
            } else if (i == 0) {
                wight.focus();
            } else {
                wight.down();
            }
        }
    }

    /**
     * Calculates target positions for all widgets based on current state.
     *
     * @param currentState The current selection state
     * @return List of widget positions
     */
    public static List<Vector2i> calculatePositions(SpellSelectionState currentState) {
        if (spellWightList == null) return List.of();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        return calculatePositionsInternal(spellWightList.size(), screenWidth, screenHeight, currentState);
    }

    /**
     * Internal method to calculate widget positions.
     * Positions are calculated from bottom to top to prevent overflow when adding new spells.
     *
     * @param count Number of widgets
     * @param screenWidth Screen width
     * @param screenHeight Screen height
     * @param currentState Current selection state
     * @return List of calculated positions
     */
    private static List<Vector2i> calculatePositionsInternal(int count, int screenWidth, int screenHeight, SpellSelectionState currentState) {
        List<Vector2i> positions = NonNullList.withSize(count, new Vector2i(0, 0));

        int baseX = screenWidth / 2 + SpellSelectionLayerV2.BASE_X_OFFSET;
        int baseY = screenHeight - SpellSelectionLayerV2.BASE_Y_OFFSET_FROM_BOTTOM;
        int spacing = SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING;

        if (currentState == SpellSelectionState.EmptyHand) {
            for (int i = 0; i < count; i++) {
                int y = baseY - ((count - 1 - i) * spacing);
                positions.set(i, new Vector2i(baseX, y));
            }
        } else {
            int focusY = baseY - ((count - 1) * spacing) + SpellSelectionLayerV2.FOCUS_Y_OFFSET;
            positions.set(0, new Vector2i(baseX, focusY));

            for (int i = 1; i < count; i++) {
                int y = baseY - ((count - 1 - i) * spacing);
                positions.set(i, new Vector2i(baseX, y));
            }
        }

        return positions;
    }

    /**
     * Gets the spell group data manager instance.
     */
    public static SpellGroupData getSpellGroupData() {
        return SpellGroupData.instance;
    }

    /**
     * Gets the widget list, initializes if needed.
     */
    public static List<ScrollSpellWight> getSpellWightList() {
        if (spellWightList == null) {
            update();
        }
        return spellWightList;
    }

    /**
     * Gets the current selection state.
     */
    public static SpellSelectionState getState() {
        return state;
    }

    @SubscribeEvent
    public static void updateHandle(ChangeSpellEvent event) {
        update();
    }

    @SubscribeEvent
    public static void onChangeGroup(org.nomoremagicchoices.api.event.ChangeGroupEvent event) {
        if (spellWightList != null && !spellWightList.isEmpty()) {
            switchToGroup(event.getNewGroup());
        }
    }
}

