package com.ksider.mobile.android.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import com.ksider.mobile.android.WebView.R;
//import android.support.v7.widget.RecyclerView;

public class TestActivity extends Activity {
//private RecyclerView recyclerView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        recyclerView.setHasFixedSize(true);
//
//                 RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
//                 recyclerView.setLayoutManager(layoutManager);
//
//                 initData();
//                 adapter = new PersonAdapter(personList);
//                 adapter.setOnRecyclerViewListener(this);
//                 recyclerView.setAdapter(adapter);
    }


    // 功能：字符串半角转换为全角
// 说明：半角空格为32,全角空格为12288.
//       其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
// 输入参数：input -- 需要转换的字符串
// 输出参数：无：
// 返回值: 转换后的字符串
    public static String halfToFull(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }

            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;
            //数字，大小写英文字母不转换
            if (c[i] >= 48 && c[i] < 58 || c[i] >= 65 && c[i] < 91 || c[i] >= 97 && c[i] < 123) {
                continue;
            }

            if (c[i] > 32 && c[i] < 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }


    // 功能：字符串全角转换为半角
// 说明：全角空格为12288，半角空格为32
//       其他字符全角(65281-65374)与半角(33-126)的对应关系是：均相差65248
// 输入参数：input -- 需要转换的字符串
// 输出参数：无：
// 返回值: 转换后的字符串
    public static String fullToHalf(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    public int getDp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
