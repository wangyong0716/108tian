package com.ksider.mobile.android.adaptor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import java.util.List;


public class FragmentTabAdapter implements RadioGroup.OnCheckedChangeListener{
    private List<Fragment> fragments; 
    private RadioGroup rgs; 
    private FragmentManager fragmentActivity; 
    private int fragmentContentId; 
    private int currentTab; 

    private OnRgsExtraCheckedChangedListener onRgsExtraCheckedChangedListener; 

    public FragmentTabAdapter(FragmentManager fragment, List<Fragment> fragments, int fragmentContentId, RadioGroup rgs) {
        this(fragment, fragments, fragmentContentId, rgs, 0);
    }
    public FragmentTabAdapter(FragmentManager fragment, List<Fragment> fragments, int fragmentContentId, RadioGroup rgs, int defaultTab) {
        this.fragments = fragments;
        this.rgs = rgs;
        this.fragmentActivity = fragment;
        this.fragmentContentId = fragmentContentId;

        FragmentTransaction ft = fragment.beginTransaction();
        ft.add(fragmentContentId, fragments.get(defaultTab));
        ft.commitAllowingStateLoss();
        rgs.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        for(int i = 0; i < rgs.getChildCount(); i++){
            if(rgs.getChildAt(i).getId() == checkedId){
            	Fragment fragment = fragments.get(i);
                FragmentTransaction ft = obtainFragmentTransaction(i);
                getCurrentFragment().onPause(); 
                if(fragment.isAdded()){
                    fragment.onResume(); 
                }else{
                    ft.replace(fragmentContentId, fragment);
                }
                showTab(i);
                ft.commit();
                if(null != onRgsExtraCheckedChangedListener){
                    onRgsExtraCheckedChangedListener.OnRgsExtraCheckedChanged(radioGroup, checkedId, i);
                }
            }
        }
    }

    /**
     * 切换tab
     * @param idx
     */
    private void showTab(int idx){
        for(int i = 0; i < fragments.size(); i++){
            Fragment fragment = fragments.get(i);
            FragmentTransaction ft = obtainFragmentTransaction(idx);

            if(idx == i){
                ft.show(fragment);
            }else{
                ft.hide(fragment);
            }
            ft.commit();
        }
        currentTab = idx; // 更新目标tab为当前tab
    }

    /**
     * 获取一个带动画的FragmentTransaction
     * @param index
     * @return
     */
    private FragmentTransaction obtainFragmentTransaction(int index){
        FragmentTransaction ft = fragmentActivity.beginTransaction();
//        // 设置切换动画
//        if(index > currentTab){
//            ft.setCustomAnimations(R.anim.ksider_left_in, R.anim.ksider_left_out);
//        }else{
//            ft.setCustomAnimations(R.anim.ksider_right_in, R.anim.ksider_right_out);
//        }
        return ft;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public Fragment getCurrentFragment(){
        return fragments.get(currentTab);
    }

    public OnRgsExtraCheckedChangedListener getOnRgsExtraCheckedChangedListener() {
        return onRgsExtraCheckedChangedListener;
    }

    public void setOnRgsExtraCheckedChangedListener(OnRgsExtraCheckedChangedListener onRgsExtraCheckedChangedListener) {
        this.onRgsExtraCheckedChangedListener = onRgsExtraCheckedChangedListener;
    }

    /**
     *  切换tab额外功能功能接口
     */
    static class OnRgsExtraCheckedChangedListener{
        public void OnRgsExtraCheckedChanged(RadioGroup radioGroup, int checkedId, int index){

        }
    }

}
