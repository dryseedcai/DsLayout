package com.zhangyue.we.anoprocesser;

import com.zhangyue.we.anoprocesser.xml.LayoutManager;
import com.zhangyue.we.x2c.ano.Xml;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author：chengwei 2018/8/7
 * @description
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.zhangyue.we.x2c.ano.Xml")
public class XmlProcessor extends AbstractProcessor {

    private int mGroupId = -1;
    private LayoutManager mLayoutMgr;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Log.init(processingEnvironment.getMessager());
        Log.w("XmlProcessor init");
        mLayoutMgr = LayoutManager.instance();
        mLayoutMgr.setFiler(processingEnvironment.getFiler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Log.w("XmlProcessor process");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Xml.class);
        TreeSet<Integer> layouts = new TreeSet<>();
        for (Element element : elements) {
            Xml xml = element.getAnnotation(Xml.class);
            int[] ids = xml.layouts();
            for (int id : ids) {
                layouts.add(id);
            }
        }

        // 十六进制一位四个字节，eg : activity_main=0x7f09001b; -> mGroupId = 0x7f
        for (Integer id : layouts) {
            if (mGroupId == -1) {
                mGroupId = (id >> 24);
            }
            mLayoutMgr.setGroupId(mGroupId);
            // 翻译、生成布局代码
            mLayoutMgr.translate(mLayoutMgr.getLayoutName(id));
        }

        //生成布局id映射代码
        mLayoutMgr.generateMap();
        return false;
    }


}
