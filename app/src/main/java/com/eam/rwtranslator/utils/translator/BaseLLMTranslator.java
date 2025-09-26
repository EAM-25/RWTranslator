package com.eam.rwtranslator.utils.translator;

import com.eam.rwtranslator.utils.Translator;

import app.nekogram.translator.BaseTranslator;

public abstract class BaseLLMTranslator extends BaseTranslator {
    public abstract void translate(String query, String fl, String tl, Translator.TranslateCallBack callback);
}
