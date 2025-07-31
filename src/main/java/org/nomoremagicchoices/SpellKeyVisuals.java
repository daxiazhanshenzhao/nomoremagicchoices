package org.nomoremagicchoices;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellKeyVisuals {
    record IronSpellShowKey(String id, String translateMsg, boolean isUnbound){}

    //keyInfo ===========================================================================================
    private static final DrawableInfo.Texture mouseKeyTexture = new DrawableInfo.Texture(
            ResourceLocation.fromNamespaceAndPath(
                    Nomoremagicchoices.MODID,
                    "textures/gui/background.png"),
            256,256);

    public static Map<String,DrawableInfo.Component> mouseMap = Map.ofEntries(
            Map.entry("key.mouse.left",new DrawableInfo.Component(new DrawableInfo.DrawRect(0, 0, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.right",new DrawableInfo.Component(new DrawableInfo.DrawRect(15, 0, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.middle",new DrawableInfo.Component(new DrawableInfo.DrawRect(30, 0, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.4",new DrawableInfo.Component(new DrawableInfo.DrawRect(45, 0, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.5",new DrawableInfo.Component(new DrawableInfo.DrawRect(60, 0, 11, 12),mouseKeyTexture))
    );

    public static Map<String,DrawableInfo.Component> mousePressMap = Map.ofEntries(
            Map.entry("key.mouse.left",new DrawableInfo.Component(new DrawableInfo.DrawRect(0, 17, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.right",new DrawableInfo.Component(new DrawableInfo.DrawRect(15, 17, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.middle",new DrawableInfo.Component(new DrawableInfo.DrawRect(30, 17, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.4",new DrawableInfo.Component(new DrawableInfo.DrawRect(45, 17, 11, 12),mouseKeyTexture)),
            Map.entry("key.mouse.5",new DrawableInfo.Component(new DrawableInfo.DrawRect(60, 17, 11, 12),mouseKeyTexture))
    );

    public static DrawableInfo.Component keyBack = new DrawableInfo.Component(new DrawableInfo.DrawRect(0, 34, 13, 13), mouseKeyTexture);
    public static DrawableInfo.Component keyBackPress = new DrawableInfo.Component(new DrawableInfo.DrawRect(0, 52, 13, 13), mouseKeyTexture);

    // 渲染数字按键的方法
    public static void renderNumberKey(GuiGraphics guiGraphics, int x, int y, int keyIndex) {
        Minecraft minecraft = Minecraft.getInstance();

        // 检查font是否可用
        if (minecraft.font == null) return;

        // 获取对应的按键绑定
        KeyMapping spellKey = findSpellKeyMapping(keyIndex);
        String keyText = getSpellKeyText(keyIndex);
        boolean isPressed = isSpellKeyPressed(keyIndex);

        // 检查是否是鼠标按键
        if (spellKey != null && !spellKey.isUnbound()) {
            String mouseKeyId = getMouseKeyId(spellKey);
            if (mouseKeyId != null) {
                renderMouseKeyFromMapping(guiGraphics, x, y, spellKey, mouseKeyId);
                return;
            }
        }

        // 渲染键盘按键背景和文本
        DrawableInfo.Component background = isPressed ? keyBackPress : keyBack;
        background.draw(guiGraphics, x, y, DrawableInfo.Anchor.CENTER, DrawableInfo.Anchor.CENTER);

        // 渲染按键文本 - 非鼠标按键按下时变灰色
        int textColor = isPressed ? 0xA0A0A0 : 0xFFFFFF; // 按下时灰色，否则白色
        int textX = x - minecraft.font.width(keyText) / 2;
        int textY = y - minecraft.font.lineHeight / 2;
        guiGraphics.drawString(minecraft.font, keyText, textX, textY, textColor);
    }

    // 获取鼠标按键ID的新方法，支持更多的按键名称格式
    private static String getMouseKeyId(KeyMapping keyMapping) {
        try {
            String keyName = keyMapping.getTranslatedKeyMessage().getString().toLowerCase();
            String originalKeyName = keyMapping.getName().toLowerCase();

            // 检查原始按键名称（通常更准确）
            if (originalKeyName.contains("mouse")) {
                if (originalKeyName.contains("left") || originalKeyName.contains("1")) {
                    return "key.mouse.left";
                } else if (originalKeyName.contains("right") || originalKeyName.contains("2")) {
                    return "key.mouse.right";
                } else if (originalKeyName.contains("middle") || originalKeyName.contains("3")) {
                    return "key.mouse.middle";
                } else if (originalKeyName.contains("4")) {
                    return "key.mouse.4";
                } else if (originalKeyName.contains("5")) {
                    return "key.mouse.5";
                }
            }

            // 检查翻译后的按键名称
            if (keyName.contains("mouse") || keyName.contains("鼠标")) {
                if (keyName.contains("left") || keyName.contains("左键") || keyName.contains("左") || keyName.contains("1")) {
                    return "key.mouse.left";
                } else if (keyName.contains("right") || keyName.contains("右键") || keyName.contains("右") || keyName.contains("2")) {
                    return "key.mouse.right";
                } else if (keyName.contains("middle") || keyName.contains("中键") || keyName.contains("中") || keyName.contains("3")) {
                    return "key.mouse.middle";
                } else if (keyName.contains("button 4") || keyName.contains("按钮 4") || keyName.contains("4")) {
                    return "key.mouse.4";
                } else if (keyName.contains("button 5") || keyName.contains("按钮 5") || keyName.contains("5")) {
                    return "key.mouse.5";
                }
            }

            // 检查GLFW按键代码
            int keyCode = keyMapping.getKey().getValue();
            return switch (keyCode) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "key.mouse.left";
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "key.mouse.right";
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "key.mouse.middle";
                case GLFW.GLFW_MOUSE_BUTTON_4 -> "key.mouse.4";
                case GLFW.GLFW_MOUSE_BUTTON_5 -> "key.mouse.5";
                default -> null;
            };

        } catch (Exception e) {
            Debug.LOGGER.warn("Error getting mouse key ID: " + e.getMessage());
            return null;
        }
    }

    // 根据按键映射渲染鼠标按键 - 改进版本
    private static void renderMouseKeyFromMapping(GuiGraphics guiGraphics, int x, int y, KeyMapping keyMapping, String mouseKeyId) {
        try {
            // 检查鼠标按键是否被按下
            boolean isPressed = keyMapping.isDown() || isMouseKeyPressed(mouseKeyId);

            // 获取对应的贴图组件
            DrawableInfo.Component component = isPressed ?
                mousePressMap.get(mouseKeyId) :
                mouseMap.get(mouseKeyId);

            if (component != null) {
                component.draw(guiGraphics, x, y, DrawableInfo.Anchor.CENTER, DrawableInfo.Anchor.CENTER);
            } else {
                // 如果没有对应的鼠标贴图，回退到显示文本
                renderFallbackMouseKey(guiGraphics, x, y, mouseKeyId, isPressed);
            }
        } catch (Exception e) {
            Debug.LOGGER.warn("Error rendering mouse key from mapping: " + e.getMessage());
            // 回退到显示按键文本
            renderFallbackMouseKey(guiGraphics, x, y, mouseKeyId, false);
        }
    }

    // 回退的鼠标按键渲染方法（当贴图不可用时）
    private static void renderFallbackMouseKey(GuiGraphics guiGraphics, int x, int y, String mouseKeyId, boolean isPressed) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.font == null) return;

        // 渲染背景
        DrawableInfo.Component background = isPressed ? keyBackPress : keyBack;
        background.draw(guiGraphics, x, y, DrawableInfo.Anchor.CENTER, DrawableInfo.Anchor.CENTER);

        // 获取简化的鼠标按键文本
        String displayText = switch (mouseKeyId) {
            case "key.mouse.left" -> "L";
            case "key.mouse.right" -> "R";
            case "key.mouse.middle" -> "M";
            case "key.mouse.4" -> "4";
            case "key.mouse.5" -> "5";
            default -> "?";
        };

        // 渲染文本
        int textColor = isPressed ? 0xFFFF00 : 0xFFFFFF;
        int textX = x - minecraft.font.width(displayText) / 2;
        int textY = y - minecraft.font.lineHeight / 2;
        guiGraphics.drawString(minecraft.font, displayText, textX, textY, textColor);
    }

    // 获取法术按键的文本显示
    public static String getSpellKeyText(int spellIndex) {
        try {
            // 获取所有按键绑定并寻找法术快捷键
            KeyMapping spellKey = findSpellKeyMapping(spellIndex);
            if (spellKey != null && !spellKey.isUnbound()) {
                String keyName = spellKey.getTranslatedKeyMessage().getString();
                // 如果按键名太长，截取前几个字符
                if (keyName.length() > 3) {
                    return keyName.substring(0, 3);
                }
                return keyName;
            }

            // 如果没有绑定快速施法键，使用数字键
            return String.valueOf(spellIndex + 1);
        } catch (Exception e) {
            // 如果出错，回退到数字显示
            return String.valueOf(spellIndex + 1);
        }
    }

    // 查找对应的法术按键映射
    private static KeyMapping findSpellKeyMapping(int spellIndex) {
        try {
            Minecraft mc = Minecraft.getInstance();

            // 首先收集所有法术按键并按正确顺序排序
            List<KeyMapping> spellKeys = new ArrayList<>();
            for (KeyMapping binding : mc.options.keyMappings) {
                String keyName = binding.getName();
                if (keyName.contains("spell_quick_cast")) {
                    spellKeys.add(binding);
                }
            }

            // 按按键名称中的数字排序
            spellKeys.sort((a, b) -> {
                int numA = extractSpellNumber(a.getName());
                int numB = extractSpellNumber(b.getName());
                return Integer.compare(numA, numB);
            });

            // 直接根据索引返回对应的按键
            if (spellIndex >= 0 && spellIndex < spellKeys.size()) {
                return spellKeys.get(spellIndex);
            }

        } catch (Exception e) {
            Debug.LOGGER.warn("Error finding spell key mapping: " + e.getMessage());
        }
        return null;
    }

    // 从按键名称中提取数字
    private static int extractSpellNumber(String keyName) {
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
            Debug.LOGGER.warn("Error extracting spell number from: " + keyName);
        }
        return 0; // 默认返回0
    }

    // 检查对应的法术按键是否被按下
    public static boolean isSpellKeyPressed(int spellIndex) {
        try {
            KeyMapping spellKey = findSpellKeyMapping(spellIndex);
            if (spellKey != null && !spellKey.isUnbound()) {
                return spellKey.isDown();
            }

            // 检查数字键是否被按下
            int numberKey = GLFW.GLFW_KEY_1 + spellIndex;
            if (numberKey <= GLFW.GLFW_KEY_9) {
                return isKeyPressed(numberKey);
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 检查物理按键是否被按下
    public static boolean isKeyPressed(int glfwKeyCode) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            long windowHandle = minecraft.getWindow().getWindow();
            return GLFW.glfwGetKey(windowHandle, glfwKeyCode) == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }

    // 渲染鼠标按键的方法
    public static void renderMouseKey(GuiGraphics guiGraphics, int x, int y, String mouseKeyName) {
        try {
            // 检查鼠标按键是否被按下
            boolean isPressed = isMouseKeyPressed(mouseKeyName);

            // 获取对应的贴图组件
            DrawableInfo.Component component = isPressed ?
                mousePressMap.get(mouseKeyName) :
                mouseMap.get(mouseKeyName);

            if (component != null) {
                component.draw(guiGraphics, x, y, DrawableInfo.Anchor.CENTER, DrawableInfo.Anchor.CENTER);
            }
        } catch (Exception e) {
            Debug.LOGGER.warn("Error rendering mouse key: " + e.getMessage());
        }
    }

    // 检查鼠标按键是否被按下
    public static boolean isMouseKeyPressed(String mouseKeyName) {
        try {
            int mouseButton = switch (mouseKeyName) {
                case "key.mouse.left" -> GLFW.GLFW_MOUSE_BUTTON_LEFT;
                case "key.mouse.right" -> GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                case "key.mouse.middle" -> GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
                case "key.mouse.4" -> GLFW.GLFW_MOUSE_BUTTON_4;
                case "key.mouse.5" -> GLFW.GLFW_MOUSE_BUTTON_5;
                default -> -1;
            };

            if (mouseButton != -1) {
                Minecraft minecraft = Minecraft.getInstance();
                long windowHandle = minecraft.getWindow().getWindow();
                return GLFW.glfwGetMouseButton(windowHandle, mouseButton) == GLFW.GLFW_PRESS;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //获取ironsspellbooks的注册的魔法技能按键
    //转化为翻译后的文本
    public static void getSpellKey(){
        Debug.LOGGER.info("Searching for spell keybindings...");
        getAllKeyBindings().forEach(keys->{
            if (!keys.isUnbound && keys.id.contains("spell_quick_cast")){
                 Debug.LOGGER.info("Found spell key: " + keys.translateMsg + " (ID: " + keys.id + ")");
            }
        });
    }

    // 调试方法：打印指定法术按键的详细信息
    public static void debugSpellKey(int spellIndex) {
        try {
            KeyMapping spellKey = findSpellKeyMapping(spellIndex);
            if (spellKey != null) {
                String keyName = spellKey.getTranslatedKeyMessage().getString();
                String originalName = spellKey.getName();
                int keyCode = spellKey.getKey().getValue();
                boolean isUnbound = spellKey.isUnbound();
                String mouseKeyId = getMouseKeyId(spellKey);

                Debug.LOGGER.info("Spell Key {} Debug Info:", spellIndex);
                Debug.LOGGER.info("  Original Name: {}", originalName);
                Debug.LOGGER.info("  Translated Name: {}", keyName);
                Debug.LOGGER.info("  Key Code: {}", keyCode);
                Debug.LOGGER.info("  Is Unbound: {}", isUnbound);
                Debug.LOGGER.info("  Mouse Key ID: {}", mouseKeyId);
                Debug.LOGGER.info("  Is Mouse Key: {}", mouseKeyId != null);
            } else {
                Debug.LOGGER.info("No spell key mapping found for index: {}", spellIndex);
            }
        } catch (Exception e) {
            Debug.LOGGER.error("Error debugging spell key {}: {}", spellIndex, e.getMessage());
        }
    }

    private static ArrayList<IronSpellShowKey> getAllKeyBindings() {
        Minecraft mc = Minecraft.getInstance();
        ArrayList<IronSpellShowKey> bindingList = new ArrayList<>();

        for (KeyMapping binding : mc.options.keyMappings) {
            IronSpellShowKey info = new IronSpellShowKey(
                    binding.getName(),
                    binding.getTranslatedKeyMessage().getString(),
                    binding.isUnbound());
            bindingList.add(info);
        }
        return bindingList;
    }
}
