package com.ksider.mobile.android.model;

import java.util.ArrayList;

/**
 * Created by yong on 7/29/15.
 */
public class AtlasModel {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    private String desc;
    private ArrayList<String> imgs;

    public AtlasModel() {
        this.imgs = new ArrayList<String>();
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {

        return desc;
    }

    public void setImgs(ArrayList<String> imgs) {
        this.imgs = imgs;
    }

    public ArrayList<String> getImgs() {

        return imgs;
    }

    public String getImgString(){
        String imgString = "";
        for (int i=0;i<imgs.size();i++){
            imgString+=imgs.get(i);
            if (i!=imgs.size()){
                imgString+="|";
            }
        }
        return imgString;
    }

    public void addImg(String img){
        imgs.add(img);
    }

    public int getImgsCount(){
        return imgs.size();
    }
}
