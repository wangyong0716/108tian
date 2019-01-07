package com.ksider.mobile.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import com.ksider.mobile.android.WebView.*;
import com.ksider.mobile.android.adaptor.BannerAlbumAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.SubjectModel;
import com.umeng.message.PushAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;

public class Utils {

    private static PushAgent mPushAgent;

    public static void init(PushAgent agent) {
        mPushAgent = agent;
    }

    /****/
    public static void alisUser(final String userId) {
        if (mPushAgent != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean added = mPushAgent.addAlias(userId, Constants.CUMSTOM_ID_ALIAS_TYPE);
                    } catch (JSONException js) {
                        js.printStackTrace();
                    }
                }
            }).start();
        }
//		if(mPushAgent != null){
//				new Handler().post(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							mPushAgent.addAlias(userId, Constants.CUMSTOM_ID_ALIAS_TYPE);
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//					}
//				});
//		}
    }

    /**
     * @param context
     * @param packageName 被检查报名
     * @return 是否安装指定报名
     */
    public static boolean checkPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                if (packName != null && packageName.contains(packName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getDistance(JSONObject item) {
        Double dist = -1.0;
        try {
            JSONArray lngLatitude = item.getJSONArray("lngLatitude");
            Double lng = lngLatitude.getDouble(0);
            Double lat = lngLatitude.getDouble(1);
            dist = Maths.getSelfDistance(lat, lng);
            if (dist == 0) {
                dist = Math.random() * 0.4 + 0.1;
                dist = Math.round(dist * 100) * 1.0 / 100;
            }
        } catch (JSONException e) {
        } catch (Exception e) {
            Log.v(Constants.LOG_TAG, e.toString());
        }
        return dist;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean hasSmartBar() {
        try {
            // 新型号可用反射调用Build.hasSmartBar()
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
        }

        // 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
        if (Build.DEVICE.equals("mx2")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }

        return false;
    }

    public static void initDetailPageArg(Intent intent, BaseDataModel data) {
        if (intent == null) {
            return;
        }
        if (data.description != null) {
            intent.putExtra(BaseDataModel.description_name, data.description);
        }
        if (data.id != null) {
            intent.putExtra(BaseDataModel.id_name, data.id);
        }
        if (data.imgUrl != null) {
            intent.putExtra(BaseDataModel.imgUrl_name, data.imgUrl);
        }
        if (data.subTitle != null) {
            intent.putExtra(BaseDataModel.subTitle_name, data.subTitle);
        }
        if (data.title != null) {
            intent.putExtra(BaseDataModel.title_name, data.title);
        }
        if (data.type != null) {
            intent.putExtra(BaseDataModel.type_name, data.type);
        }
    }

    public static BaseDataModel retrieveArgData(Intent intent) {
        BaseDataModel data = new BaseDataModel();
        if (intent != null) {
            data.description = intent.getStringExtra(BaseDataModel.description_name);
            data.id = intent.getStringExtra(BaseDataModel.id_name);
            data.imgUrl = intent.getStringExtra(BaseDataModel.imgUrl_name);
            data.subTitle = intent.getStringExtra(BaseDataModel.subTitle_name);
            data.title = intent.getStringExtra(BaseDataModel.title_name);
            data.type = intent.getStringExtra(BaseDataModel.type_name);
        }
        return data;
    }

    public static void appendArgs(Intent intent, BannerAlbumAdaptor.BannerAlbumItem item) {
        if (item != null && item.type.equals("outapp")) {
            intent.putExtra("url", item.dest);
        } else {
            intent.putExtra(BaseDataModel.id_name, item.dest);
        }
        intent.putExtra(BaseDataModel.type_name, item.type);
    }

    public static void appendArgs(Intent intent, SubjectModel item) {
        if (item != null && item.getType().equals("outapp")) {
            intent.putExtra("url", item.getTo());
        } else {
            intent.putExtra(BaseDataModel.id_name, item.getTo());
        }
        intent.putExtra(BaseDataModel.type_name, item.getType());
    }

    public static Intent getLandingActivity(Activity context, String type) {
        Intent intent = null;
        if (type.equals("resort")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.RESORT.toString());
        } else if (type.equals("farm")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.FARMYARD.toString());
        } else if (type.equals("scene")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.ATTRACTIONS.toString());
        } else if (type.equals("pick")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.PICKINGPART.toString());
        } else if (type.equals("event")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.ACTIVITY.toString());
        } else if (type.equals("weekly")) {
            intent = new Intent(context, DetailActivity.class);
            intent.putExtra("category", BasicCategory.GUIDE.toString());
        } else if (type.equals("recommend")||type.equals("choice")||type.equals("theme")) {
            intent = new Intent(context, ChoicenessActivity.class);
        } else if (type.equals("outapp")) {
            intent = new Intent(context, WebViewLandingActivity.class);
        } else {
            intent = new Intent(context, HomeActivity.class);
        }
        return intent;
    }

    public static Intent getLandingActivity(Activity context, BasicCategory category) {
        Intent intent = null;
        switch (category) {
            case ACTIVITY:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.ACTIVITY.toString());
                break;
            case GUIDE:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.GUIDE.toString());
                break;
            case ATTRACTIONS:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.ATTRACTIONS.toString());
                break;
            case FARMYARD:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.FARMYARD.toString());
            case RESORT:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.RESORT.toString());
                break;
            case PICKINGPART:
                intent = new Intent(context, DetailActivity.class);
                intent.putExtra("category", BasicCategory.PICKINGPART.toString());
                break;
            default:
                break;
        }
        return intent;
    }
}
