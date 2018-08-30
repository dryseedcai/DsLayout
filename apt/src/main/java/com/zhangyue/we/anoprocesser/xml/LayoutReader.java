package com.zhangyue.we.anoprocesser.xml;

import com.zhangyue.we.anoprocesser.Log;
import com.zhangyue.we.view.View;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.Stack;

import javax.annotation.processing.Filer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author：chengwei 2018/8/7
 * @description
 */
public class LayoutReader {
    private Filer mFiler;
    private SAXParser mParser;
    private String mName;
    private String mLayoutName;
    private String mPackageName;
    private File mFile;

    public LayoutReader(File file, String name, Filer filer, String packageName, int groupId) {
        mFile = file;
        mFiler = filer;
        mPackageName = packageName;
        mLayoutName = name;
        mName = getJavaName(groupId, name);
    }

    /**
     * 解析xml文件， 将java poet生成的代码写入mFiler
     *
     * @return
     */
    public String parse() {
        try {
            // xmlFile : E:\CodeDs\DsLayout\app\src\main\res\layout\activity_main.xml
            File xmlFile = new File(mFile + File.separator + mLayoutName + ".xml");
            mParser = SAXParserFactory.newInstance().newSAXParser();
            mParser.parse(xmlFile, new XmlHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mName;
    }

    private class XmlHandler extends DefaultHandler {
        private Stack<View> mStack;
        private View mRootView;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            mStack = new Stack<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            View view = createView(qName, attributes);
            /**
             * 1. 设置view的parent
             * 2. setParent 方法会调用 view.generateView(mAttributes) 生成view的构建代码
             */
            if (mStack.size() > 0) {
                view.setParent(mStack.get(mStack.size() - 1));
            } else {
                view.setParent(null);
            }
            mStack.push(view);
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            View view = mStack.pop();
            if (mStack.size() == 0) {
                mRootView = view;
                StringBuffer stringBuffer = new StringBuffer();
                // 整合mRootView的构建代码
                mRootView.translate(stringBuffer);
                stringBuffer.append("return ").append(mRootView.getObjName());
                // java poet 生成java代码
                LayoutWriter writer = new LayoutWriter(stringBuffer.toString(), mFiler, mName, mPackageName
                        , mLayoutName, mRootView.getImports());
                Log.w(String.format("endElement [name:%s] \n %s", mName, stringBuffer.toString()));
                writer.write();
            }
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }
    }

    private View createView(String name, Attributes attributes) {
        return new View(mPackageName, name, attributes);
    }

    /**
     * 生成 JavaName
     * 1. groupId = 127 ; name = "activity_main"
     * 2. return "X2C_127_Activity_Main"
     *
     * @param groupId
     * @param name
     * @return
     */
    private String getJavaName(int groupId, String name) {
        String retName = groupId + "_" + name;
        String[] ss = retName.split("_");
        StringBuilder stringBuilder = new StringBuilder("X2C_");
        for (int i = 0; i < ss.length; i++) {
            stringBuilder.append(ss[i].substring(0, 1).toUpperCase())
                    .append(ss[i].substring(1));
            if (i < ss.length - 1) {
                stringBuilder.append("_");
            }
        }
        return stringBuilder.toString();
    }


}
