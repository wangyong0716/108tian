package com.ksider.mobile.android.WebView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.activity.fragment.buy.OrderCheckedFragment;
import com.ksider.mobile.android.activity.fragment.buy.OrderDetailFragment;
import com.ksider.mobile.android.activity.fragment.buy.OrderFragment;
import com.ksider.mobile.android.activity.fragment.buy.OrderSelectFragment;
import com.ksider.mobile.android.utils.CustomDialog;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wenkui on 4/28/15.
 */
public class PurchaseAcitvity extends ActionBarActivity implements FragmentCallback {
    public static final int SELECT = 1;
    public static final int ORDER = 2;
    public static final int ORDER_CHECKED = 3;
    public static final int PAYMENT = 4;
    public static final int PAYMENT_RESULT = 5;
    public static final int DIRECT_PAYMENT = -1;
    public static final String STAGE = "stage";
    protected int currentStage;
//    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.fragment_purchase_with_toolbar);
        customActionBar();
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("payment", false)) {
                provokePayment(getIntent().getStringExtra("order"));
                currentStage = DIRECT_PAYMENT;
            } else if (getIntent().getBooleanExtra("order", false)) {
                currentStage = ORDER;
                Bundle args = new Bundle();
                args.putString("product", getIntent().getStringExtra("product"));
                Fragment fragment = Fragment.instantiate(this, OrderFragment.class.getName(), args);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, fragment, "Purchase").commit();
            } else {
                Bundle args = new Bundle();
                args.putString("product", getIntent().getStringExtra("product"));
                args.putParcelableArrayList("stocks", getIntent().getParcelableArrayListExtra("stocks"));
                currentStage = SELECT;
                Fragment fragment = Fragment.instantiate(this, OrderSelectFragment.class.getName(), args);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, fragment, "Purchase").commit();
            }
        }
        upDateTitle(currentStage);
    }

    protected void customActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        backButton.setImageResource(R.drawable.backbutton_icon);
        backButton.getDrawable().setAlpha(255);
        findViewById(R.id.list_backbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (currentStage == ORDER_CHECKED) {
            CustomDialog.Builder builder = new CustomDialog.Builder(PurchaseAcitvity.this);
            builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    int stackSize = fragmentManager.getBackStackEntryCount();
                    if (stackSize == 0 || currentStage < 0 || currentStage == PAYMENT_RESULT) {
                        finish();
                    } else {
                        fragmentManager.popBackStack();
                    }
                    currentStage -= 1;
                    upDateTitle(currentStage);
                }
            }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).setTitle("返回后您将重复下单，所使\n用优惠券将失效！").show();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int stackSize = fragmentManager.getBackStackEntryCount();
            if (stackSize == 0 || currentStage < 0 || currentStage == PAYMENT_RESULT) {
                finish();
            } else {
                fragmentManager.popBackStack();
            }
            currentStage -= 1;
            upDateTitle(currentStage);
        }
        return;
    }

    protected void upDateTitle(int stage) {
        TextView title = (TextView) findViewById(R.id.list_title);
        switch (stage) {
            case SELECT:
                title.setText(R.string.buy_order_select);
                break;
            case ORDER:
                title.setText(R.string.buy_submit_order_btn);
                break;
            case ORDER_CHECKED:
                title.setText(R.string.buy_order_checked_btn);
                break;
            case PAYMENT:
            case DIRECT_PAYMENT:
                title.setText(R.string.buy_submit_payment_btn);
                break;
            case PAYMENT_RESULT:
                title.setText(R.string.buy_payment_result);
                break;
            default:
                break;
        }
    }

    @Override
    public void next(JSONObject response) {
        try {
            Fragment fragment = null;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
            switch (response.getInt(STAGE)) {
                case SELECT:
                    currentStage = ORDER;
                    fragment = new OrderFragment();
                    Bundle selectArgs = new Bundle();
                    selectArgs.putString("product", response.toString());
                    fragment.setArguments(selectArgs);
                    transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case ORDER:
                    currentStage = ORDER_CHECKED;
                    fragment = new OrderCheckedFragment();
                    Bundle ordersArgs = new Bundle();
                    ordersArgs.putString("order", response.toString());
                    fragment.setArguments(ordersArgs);
                    transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case ORDER_CHECKED:
                case PAYMENT:
                    try {
                        currentStage = PAYMENT_RESULT;
                        fragment = new OrderDetailFragment();
                        Bundle args = new Bundle();
                        try {
                            args.putString("result", response.getString("result"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            args.putString("out_trade_no", response.getString("out_trade_no"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        args.putInt("payment", response.getInt("payment"));
                        fragment.setArguments(args);
                        transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            upDateTitle(currentStage);
        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

    protected void provokePayment(String order) {
        try {
            Fragment fragment = new OrderCheckedFragment();
            Bundle args = new Bundle();
            args.putString("order", order);
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
