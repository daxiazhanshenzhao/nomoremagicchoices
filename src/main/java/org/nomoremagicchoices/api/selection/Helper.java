package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import java.util.List;

public class Helper {

    public static List<SpellData> getAllSpell(){
        return ClientMagicData.getSpellSelectionManager().getAllSpells().stream().map(selectionOption -> selectionOption.spellData).toList();
    }

    public static SpellSelectionManager getSpellSelectionManager(){
        return ClientMagicData.getSpellSelectionManager();
    }

}
