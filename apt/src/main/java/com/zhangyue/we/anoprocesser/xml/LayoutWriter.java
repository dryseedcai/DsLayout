package com.zhangyue.we.anoprocesser.xml;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * @author：chengwei 2018/8/8
 * @description
 */
public class LayoutWriter {
    private Filer mFiler;
    private String mName;
    /**
     * 生成方法的具体代码
     */
    private String mMethodSpec;
    private String mPkgName;
    private String mLayoutName;
    private TreeSet<String> mImports;

    public LayoutWriter(String methodSpec, Filer filer, String javaName, String pkgName, String layoutName
            , TreeSet<String> imports) {
        this.mMethodSpec = methodSpec;
        this.mFiler = filer;
        this.mName = javaName;
        this.mPkgName = pkgName;
        this.mLayoutName = layoutName;
        this.mImports = imports;
    }

    public void write() {

        MethodSpec methodSpec = MethodSpec.methodBuilder("createView")
                .addParameter(ClassName.get("android.content", "Context"), "ctx")
                .addParameter(int.class, "layoutId")
                .addStatement(mMethodSpec)
                .returns(ClassName.get("android.view", "View"))
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(mName)
                .addMethod(methodSpec)
                .addSuperinterface(ClassName.get("com.zhangyue.we.x2c", "IViewCreator"))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(String.format("WARN!!! dont edit this file\ntranslate from {@link  %s.R.layout.%s}" +
                        "\nautho chengwei \nemail chengwei@zhangyue.com\n", mPkgName, mLayoutName))
                .build();

        JavaFile javaFile = JavaFile.builder("com.zhangyue.we.x2c", typeSpec)
                .addImports(mImports)
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
    Code Example :
        public class X2C_127_Activity_Main implements IViewCreator {
            @Override
            public View createView(Context ctx, int layoutId) {
                Resources res = ctx.getResources();

                LinearLayout linearLayout0 = new LinearLayout(ctx);
                linearLayout0.setOrientation(LinearLayout.VERTICAL);


                TextView textView1 = new TextView(ctx);
                LinearLayout.LayoutParams layoutParam1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParam1.gravity= Gravity.CENTER ;
                textView1.setText("Hello World!");
                textView1.setLayoutParams(layoutParam1);
                linearLayout0.addView(textView1);


                Button button2 = new Button(ctx);
                LinearLayout.LayoutParams layoutParam2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50,res.getDisplayMetrics())));
                layoutParam2.topMargin= (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40,res.getDisplayMetrics())) ;
                button2.setGravity(Gravity.CENTER);
                button2.setText("button");
                button2.setLayoutParams(layoutParam2);
                linearLayout0.addView(button2);


                return linearLayout0;
            }
        }
 */