package com.ksider.mobile.android.utils.stat;

import android.util.Log;
import com.ksider.mobile.android.WebView.Constants;

/**
 * Created by wenkui on 4/20/15.
 */
public class RequestStat {
    protected long count = 0;
    protected long total = 0;
    protected double mAvg = 0;
    protected double mMax = 0;
    protected double mMin = -1;
    protected long mStart = 0;
    public static RequestStat stat = new RequestStat();
    public void start(long start){
        mStart = start;
    }
    public void end(long end){
        count += 1;
        long duration = end-mStart;
        total += duration;
        mAvg = (total*1.0)/count;
        mMax = mMax<duration?duration:mMax;
        if(mMin<0){
            mMin = duration;
        }else{
            mMin = mMin<=duration?mMin:duration;
        }
    }
    public String toString(){
        return "count:"+count+"\n"
                +"mAvg:"+mAvg+"\n"
                +"mMax:"+mMax+"\n"
                +"mMin:"+mMin+"\n";
    }
}
