package com.eam.rwtranslator.utils;

import androidx.annotation.Keep;

/**
 * 多语言翻译字段枚举，定义所有支持的翻译键名。
 */
public enum TranslationKeys {
    DISPLAY_TEXT("displayText"),
    DISPLAY_DESCRIPTION("displayDescription"),
    TEXT("text"),
    DESCRIPTION("description"),
    IS_LOCKED_MESSAGE("isLockedMessage"),
    IS_LOCKED_ALT_MESSAGE("isLockedAltMessage"),
    IS_LOCKED_ALT2_MESSAGE("isLockedAlt2Message"),
    SHOW_MESSAGE_TO_PLAYERS("showMessageToPlayer"),
    SHOW_MESSAGE_TO_ALL_PLAYERS("showMessageToAllPlayer"),
    SHOW_MESSAGE_TO_ALL_ENEMY_PLAYERS("showMessageToAllEnemyPlayers"),
    CANNOT_PLACE_MESSAGE("cannotPlaceMessage"),
    SHOW_QUICK_WAR_LOG_TO_PLAYER("showQuickWarLogToPlayer"),
    SHOW_QUICK_WAR_LOG_TO_ALL_PLAYERS("showQuickWarLogToAllPlayers"),
    DISPLAY_NAME("displayName"),
    DISPLAY_NAME_SHORT("displayNameShort");
    
    private final String keyName;

    TranslationKeys(String key) {
        this.keyName = key;
    }

    public String getKeyName() {
        return this.keyName;
    }
}