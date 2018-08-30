package com.zhangyue.we.anoprocesser.xml;

import com.zhangyue.we.anoprocesser.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

/**
 * @author：chengwei 2018/8/10
 * @description
 */
public class LayoutManager {

    private static LayoutManager sInstance;
    /**
     * 根目录 D:\CodeDs\DsLayout\app
     */
    private File mRootFile;
    /**
     * 资源文件 D:\CodeDs\DsLayout\app\src\main\res\layout
     */
    private File mLayoutFile;
    /**
     * 包名 com.dryseed.dslayout
     */
    private String mPackageName;
    private int mGroupId;
    private Filer mFiler;
    /**
     * key is layoutId, value is javaName
     */
    private HashMap<Integer, String> mMap;
    /**
     * key is layoutName,value is layoutId
     */
    private HashMap<String, Integer> mRJava;
    /**
     * key is layoutId,value is layoutName
     */
    private HashMap<Integer, String> mRJavaId;
    /**
     * key is styleName,value is style
     */
    private HashMap<String, Style> mStyles;
    /**
     * key is layoutName.xml,value is javaName.java
     */
    private HashMap<String, String> mTranslateMap;

    /**
     * key is attrName,value is attr
     */
    private HashMap<String, Attr> mAttrs;

    private LayoutManager() {
        mMap = new HashMap<>();
        mRJavaId = new HashMap<>();
        mTranslateMap = new HashMap<>();
    }

    public static LayoutManager instance() {
        if (sInstance == null) {
            synchronized (LayoutManager.class) {
                if (sInstance == null) {
                    sInstance = new LayoutManager();
                }
            }
        }
        return sInstance;
    }

    public void setFiler(Filer filer) {
        this.mFiler = filer;
        getLayoutPath();
        mPackageName = getPackageName();
        mRJava = getR();
        mAttrs = new Attr2FuncReader(new File(mRootFile, "X2C_CONFIG.xml")).parse();
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }

    /**
     * 根据 layoutId 获取 layoutName
     * 1. 查找映射表mRJavaId
     *
     * @param id
     * @return
     */
    public String getLayoutName(int id) {
        return mRJavaId.get(id);
    }

    public Integer getLayoutId(String layoutName) {
        return mRJava.get(layoutName);
    }

    /**
     * 解析xml，生成代码
     * 1. 设置mMap（layoutId : fileName）
     * 2. 设置mTranslateMap（fileName.java : layoutName.xml）
     *
     * @param layoutName
     * @return
     */
    public String translate(String layoutName) {
        String fileName;
        Integer layoutId = getLayoutId(layoutName);
        if (mMap.containsKey(layoutId)) {
            fileName = mMap.get(layoutId);
        } else {
            LayoutReader reader = new LayoutReader(mLayoutFile, layoutName, mFiler, mPackageName, mGroupId);
            fileName = reader.parse();
            mMap.put(layoutId, fileName);
        }
        mTranslateMap.put(fileName + ".java", layoutName + ".xml");
        return fileName;
    }

    public Style getStyle(String name) {
        if (name == null) {
            return null;
        }
        if (mStyles == null) {
            mStyles = new StyleReader(mLayoutFile.getParentFile()).parse();
        }
        return mStyles.get(name);
    }

    /**
     * 生成资源id映射java文件
     */
    public void generateMap() {
        printTranslate();

        MapWriter mapWriter = new MapWriter(mGroupId, mMap, mFiler);
        mapWriter.write();
        mMap.clear();
        mTranslateMap.clear();
    }

    /**
     * 打印映射表
     */
    private void printTranslate() {
        if (mTranslateMap.size() == 0) {
            return;
        }
        int maxTab = 0;
        int tabCount;
        for (String layoutName : mTranslateMap.values()) {
            tabCount = layoutName.length() / 4 + 1;
            if (tabCount > maxTab) {
                maxTab = tabCount;
            }
        }

        StringBuilder stringBuilder;
        String layoutName;
        for (String javaName : mTranslateMap.keySet()) {
            layoutName = mTranslateMap.get(javaName);
            tabCount = layoutName.length() / 4;
            stringBuilder = new StringBuilder(layoutName);
            if (tabCount < maxTab) {
                for (int j = 0; j < maxTab - tabCount; j++) {
                    stringBuilder.append("\t");
                }
            }
            Log.w(String.format("%s->\t%s", stringBuilder.toString(), javaName));
        }
    }

    /**
     * 获取layout资源文件目录，赋值给 mLayoutFile (D:\CodeDs\DsLayout\app\src\main\res\layout)
     * 1. 设置根目录路径 mRootFile eg:D:\CodeDs\DsLayout\app
     */
    private void getLayoutPath() {
        try {
            JavaFileObject fileObject = mFiler.createSourceFile("bb");
            String path = fileObject.toUri().toString();
            String preFix = "file:/";
            if (path.startsWith(preFix)) {
                path = path.substring(preFix.length() - 1);
            }
            File file = new File(path);

            // D:\CodeDs\DsLayout\app\build\generated\source\apt\debug\bb.java
            Log.w("getLayoutPath file : " + file.getAbsolutePath());
            //bb.java
            Log.w("getLayoutPath file.getName() : " + file.getName());

            while (!file.getName().equals("build")) {
                file = file.getParentFile();
            }
            mRootFile = file.getParentFile();
            String sep = File.separator;
            mLayoutFile = new File(mRootFile.getAbsolutePath() + sep + "src" + sep + "main" + sep + "res" + sep + "layout");

            // D:\CodeDs\DsLayout\app\src\main\res\layout
            Log.w("getLayoutPath mLayoutFile : " + mLayoutFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过 build.gradle 的 applicationId 获取 PackageName
     *
     * @return
     */
    private String getPackageName() {
        File buildGradle = new File(mRootFile, "build.gradle");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(buildGradle));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("applicationId")) {
                    return line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 layoutName 和 layoutId 的映射关系
     * 1. 获取R.java文件
     * 2. 解析R.java文件，设置 mRJavaId （layoutId:layoutName）
     * 3. 设置 return map （layoutName:layoutId）
     *
     * @return
     */
    private HashMap<String, Integer> getR() {
        HashMap<String, Integer> map = new HashMap<>();
        File rFile = getRFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rFile));
            String line;
            boolean layoutStarted = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("public static final class layout")) {
                    layoutStarted = true;
                } else if (layoutStarted) {
                    if (line.contains("}")) {
                        break;
                    } else {
                        // line eg : public static final int activity_main=0x7f09001b;
                        line = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(";"));
                        String ss[] = line.split("=");
                        int id = Integer.decode(ss[1]);
                        map.put(ss[0], id);
                        mRJavaId.put(id, ss[0]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取R.java文件
     *
     * @return
     */
    private File getRFile() {
        String sep = File.separator;
        String stringBuilder = mRootFile.getAbsolutePath() + sep + "build" + sep +
                "generated" + sep + "source" + sep + "r";
        File rDir = new File(stringBuilder);

        File files[] = rDir.listFiles();
        if (files == null) {
            return null;
        }
        File rFile = null;
        long time = 0;
        for (File file : files) {
            File f = new File(file.getAbsolutePath() + sep + mPackageName.replace(".", sep) + sep + "R.java");
            if (f.lastModified() > time) {
                rFile = f;
                time = f.lastModified();
            }
        }
        return rFile;
    }

    public HashMap<String, Attr> getAttrs() {
        return mAttrs;
    }

}
