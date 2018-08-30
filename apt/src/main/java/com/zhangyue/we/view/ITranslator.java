package com.zhangyue.we.view;

/**
 * @author：chengwei 2018/8/23
 * @description
 */
public interface ITranslator {
    /**
     * XML 中 Attribute 的解析处理
     *
     * @param stringBuffer
     * @param key
     * @param value
     * @return
     */
    boolean translate(StringBuffer stringBuffer, String key, String value);

    void onAttributeEnd(StringBuffer stringBuffer);
}
