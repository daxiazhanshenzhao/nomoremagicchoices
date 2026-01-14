# 🎉 NoMoreMagicChoices v2.7 Update Log

**Release Date:** January 14, 2026  
**Minecraft Version:** 1.20.1  
**Mod Version:** 1.20.1-2.7

---

## 📋 Overview

Version 2.7 brings significant UI improvements and enhanced customization options, focusing on better user experience and visual feedback for spell selection.

---

## ✨ New Features

### 🎨 Visual Improvements

#### **Enhanced Spell Slot Focus Indicator**
- Added visual focus highlighting for currently selected spell in **EmptyHand state**
- Current spell now displays with a **golden border** (FOCUS_YELLOW) to clearly indicate selection
- Improved spell slot layering system - selected spells render on top for better visibility
- Fixed issue where current spell selection was not visible when hands are empty

#### **Texture Refinements**
- Redesigned spell slot textures with better alignment
- Updated cooldown overlay graphics for clearer visual feedback
- **Golden border** indicates spell is ready (no cooldown)
- **Silver border** indicates spell is on cooldown
- Optimized texture coordinates for pixel-perfect rendering

### ⚙️ Configuration Options

#### **Animation Speed Curve Modes**
Added multiple animation speed curve options via `ClientConfig.SPEED_LINE_MODE`:
- **Mode 0**: `smoothstep` - Smooth acceleration and deceleration (default)
- **Mode 1**: `easeOutBack` - Fast reach with 1.2x overshoot and bounce-back effect
- **Mode 2**: `linear` - Constant speed movement

Users can now customize the feel of UI animations to match their preference.

### 🔧 Technical Improvements

#### **Hot-Swappable Component Positioning**
- Implemented dynamic component position adjustment system
- UI elements can now adjust their spacing and position based on state
- **Down State**: Compact spacing (20px between slots)
- **Focus State**: Wider spacing (22px between slots) for better readability
- Smooth transitions between states using configurable animation curves

#### **Code Refactoring**
- Introduced `BlitContext` record for better texture coordinate management
- Centralized all texture coordinates as static constants for easier maintenance
- Separated rendering logic for spell slots and key bindings
- Improved code readability with descriptive constant names

---

## 🐛 Bug Fixes

### EmptyHand State Focus Display
- **Fixed**: Previously, when player's hands were empty, there was no visual indication of which spell was currently selected
- **Solution**: Implemented focus border rendering for the selected spell in Down state, with proper layering to ensure visibility

### Texture Alignment Issues
- Fixed spell slot border misalignment
- Adjusted cooldown progress bar positioning for pixel-perfect overlay
- Corrected texture coordinate offsets for consistent rendering

---

## 🔄 Changes

### Configuration File Updates
- Modified `ClientConfig` to support new animation curve selection
- Added `SPEED_LINE_MODE` configuration option

### Rendering Pipeline Optimization
- Implemented two-phase rendering system:
  1. **Phase 1**: Render all normal spell slots
  2. **Phase 2**: Render focused spell slot on top layer
- This ensures selected spells are always visible and not obscured by adjacent slots

---

## 📝 Technical Details

### BlitContext Constants
```java
// Spell Slot Borders
FOCUS_YELLOW  // Ready state with focus (Golden, 22x22)
FOCUS_SLIVER  // Cooldown state with focus (Silver, 22x22)
DOWN_YELLOW   // Ready state normal (Golden, 22x22)
DOWN_SLIVER   // Cooldown state normal (Silver, 20x20)

// Cooldown Indicator
COOLDOWN_SQUARE // Cooldown progress overlay (22x22)

// Key Button Backgrounds
KEY_BG_LEFT   // Left border (3x12)
KEY_BG_MIDDLE // Stretchable middle section (dynamic width)
KEY_BG_RIGHT  // Right border (3x12)
```

### Animation Curves
- **smoothstep**: `f(x) = x² * (3 - 2x)` - Smooth S-curve
- **easeOutBack**: `f(x) = 1 + c₃(x-1)³ + c₁(x-1)²` where `c₁ = 3.5` - Overshoot and settle

---

## 🎯 Known Issues

None reported at this time.

---

## 🙏 Credits

**Mod Author:** Galaxy  
**Contributors:** Community feedback and bug reports

---

## 📦 Download

Available on CurseForge and Modrinth.

---

## 💬 Feedback

If you encounter any issues or have suggestions, please report them on our issue tracker!

---

*Thank you for using NoMoreMagicChoices!*

