package com.ksider.mobile.android.WebView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.signup.ModifyPhoneDialogFragment;
import com.ksider.mobile.android.crop.Crop;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.utils.net.toolbox.MultiPartRequest;
import com.ksider.mobile.android.view.CircularImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {
    private Uri photoUri = null;
    private int birthYear = 0;
    private int birthMonth = 0;
    private int birthDayOfMonth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        new SlidingLayout(this);
        customActionBar("编辑资料");
        init();
    }

    protected void init() {
        String userInfo = Storage.getSharedPref().getString(Storage.USER_INFO,
                "{}");
        try {
            JSONObject data = new JSONObject(userInfo);
            renderAvatar(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void renderAvatar(JSONObject data) {
        String username = "未知";
        try {
            username = data.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (UserInfo.getPhone() != null && !UserInfo.getPhone().equals("")) {
            ((TextView) findViewById(R.id.phone)).setText(UserInfo.getPhone());
        } else {
            ((TextView) findViewById(R.id.phone)).setText("");
        }

        findViewById(R.id.avatar_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final UniversalDialog.Builder builder = new UniversalDialog.Builder(EditProfileActivity.this);
                builder.setItem1("相机", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String SDState = Environment.getExternalStorageState();
                        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImage();
                                    photoUri = Uri.fromFile(photoFile);
                                } catch (IOException ex) {
                                    Toast.makeText(EditProfileActivity.this, "拍照失败！", Toast.LENGTH_LONG).show();
                                }

                                if (photoFile != null) {
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                            photoUri);
                                    startActivityForResult(takePictureIntent, Crop.REQUEST_TAKE);
                                }
                            }
                        }
                    }
                }).setItem2("本地照片", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Crop.pickImage(EditProfileActivity.this);
                    }
                }).show();
            }
        });

        TextView textView = (TextView) findViewById(R.id.username);
        textView.setText(username);
        findViewById(R.id.username_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final UniversalDialog.Builder builder = new UniversalDialog.Builder(EditProfileActivity.this);
                builder.setConfirmButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        update("updateName", builder.getInput());
                    }
                }).setTitle("修改用户名").setHint("请输入新的用户名").show();
            }
        });
        CircularImageView imageView = (CircularImageView) findViewById(R.id.edit_avatar);
        try {
            imageView.setImageResource(ImageUtils.formatImageUrl(
                    data.getString("figureurl") + "?t=" + System.currentTimeMillis(), ImageUtils.THUMBNAIL));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String gender = "";
        try {
            gender = data.getString("gender");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        View bindPhone = (View) findViewById(R.id.phone_layout);
        bindPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                ModifyPhoneDialogFragment newFragment = new ModifyPhoneDialogFragment();
                Bundle args = new Bundle();
                args.putString("action", "bind");
                newFragment.setArguments(args);
                newFragment.show(ft, "dialog");
                newFragment.setOnHide(new ModifyPhoneDialogFragment.OnDialogHide() {
                    @Override
                    public void onHide() {
                        String phone = UserInfo.getPhone();
                        if (phone != null) {
                            init();
                            TextView textView = (TextView) findViewById(R.id.phone);
                            textView.setText(phone);
//                            MessageUtils.eventBus.post(new MessageEvent(
//                                    MessageEvent.NOTIFY_PERSONAL_INFO_CHANGE));
                        }
                    }
                });
            }
        });
        textView = (TextView) findViewById(R.id.gender);
        textView.setText(gender);
        findViewById(R.id.gender_layout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                final UniversalDialog.Builder builder = new UniversalDialog.Builder(EditProfileActivity.this);
                builder.setItem1("男", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        update("updateGender", "男");
                    }
                }).setItem2("女", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        update("updateGender", "女");
                    }
                }).setItem3("保密", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update("updateGender", "");
                    }
                }).show();
            }
        });

        textView = (TextView) findViewById(R.id.age);
        long temp;
        try {
            temp = data.getLong("birthday") * 1000;
        } catch (JSONException js) {
            temp = System.currentTimeMillis();
            js.printStackTrace();
        }
        final long birth = temp;
        textView.setText(DateUtils.getAge(birth));
        findViewById(R.id.age_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog.Builder builder = new DateDialog.Builder(EditProfileActivity.this);
                builder.setMinDate(1900, 1, 1).setMaxTime(System.currentTimeMillis()).setTitle("选择生日").setCallBack(new DateDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker startDatePicker, int year, int monthOfYear, int dayOfMonth) {
                        birthYear = year;
                        birthMonth = monthOfYear;
                        birthDayOfMonth = dayOfMonth;
                    }
                }).setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(birthYear, birthMonth, birthDayOfMonth);
                        updateBirthday((long) (calendar.getTimeInMillis() / 1000l));
                    }
                }).setNegativeButton("取消", new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).setTime(birth).show();
            }
        });

        Storage.putString(Storage.USER_INFO, data.toString());
    }

    public void update(String action, String value) {
        Map<String, Object> params = new HashMap<String, Object>();
        final String error;
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        params.put("action", action);
        if (action.contains("updateGender")) {
            error = "修改性别";
            params.put("gender", value);
        } else if (action.contains("updateName")) {
            error = "修改姓名";
            params.put("name", value);
        } else {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                APIUtils.register(params), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null
                            && response.getInt("status") == 0) {
                        renderAvatar(response.getJSONObject("data"));
                        MessageUtils.eventBus.post(new MessageEvent(
                                MessageEvent.NOTIFY_PERSONAL_INFO_CHANGE));
                        Toast.makeText(EditProfileActivity.this,
                                error + "成功", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(
                                EditProfileActivity.this,
                                error + "失败原因："
                                        + response.getString("msg"),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EditProfileActivity.this,
                            error + "失败原因：网络异常！", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditProfileActivity.this,
                        error + "失败原因：网络异常！", Toast.LENGTH_LONG).show();
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    public void updateBirthday(long birthDay) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "updateBirthday");
        params.put("birthday", birthDay);
        String url = APIUtils.register(params);
        Log.i("AAA", "update birthday->url=" + url);
        JsonObjectRequest request = new JsonObjectRequest(
                url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        renderAvatar(response.getJSONObject("data"));
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_PERSONAL_INFO_CHANGE));
                        Toast.makeText(EditProfileActivity.this, "修改出生日期成功", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(EditProfileActivity.this, "修改出生日期失败原因：" + response.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EditProfileActivity.this, "修改出生日期失败原因：网络异常！", Toast.LENGTH_LONG).show();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditProfileActivity.this,
                        error + "修改出生日期失败原因：网络异常！", Toast.LENGTH_LONG).show();
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_TAKE && resultCode == RESULT_OK) {
            beginCrop(photoUri);
        } else if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void beginCrop(Uri source) {
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        new Crop(source).output(outputUri).asSquare().start(EditProfileActivity.this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("action", "updateAvatar");
            params.put("t", System.currentTimeMillis());
            MultiPartRequest request = new MultiPartRequest(APIUtils.register(params), new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response != null
                                && response.getInt("status") == 0) {
                            renderAvatar(response.getJSONObject("data"));
                            MessageUtils.eventBus.post(new MessageEvent(
                                    MessageEvent.NOTIFY_PERSONAL_INFO_CHANGE));
                            if (photoUri != null) {
                                try {
                                    File localImage = new File(photoUri.getPath());
                                    if (localImage.exists()) {
                                        localImage.delete();
                                    }
                                } catch (Exception ex) {

                                }
                            }
                            return;
                        } else {
                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "修改头像失败原因："
                                            + response.getString("msg"),
                                    Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EditProfileActivity.this,
                                "修改头像失败原因：网络异常！", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(EditProfileActivity.this,
                            error + "失败原因：网络异常！", Toast.LENGTH_LONG).show();
                }
            });

            request.addStringUpload("action", "updateAvatar");
            request.addStringUpload("sid", Storage.sharedPref.getString(Storage.SESSION_ID, null));
//			request.addFileUpload("image", photo);
            request.addByteUpload("image", compressImage(Crop.getOutput(result).getPath(), 70));
            Network.getInstance().addToRequestQueue(request);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return (inSampleSize > 1 ? inSampleSize : 1);
    }

    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 240, 240);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //	public String compressImage (String filePath, int q) throws IOException{
//		Bitmap bm = getSmallBitmap(filePath);
//		File outputFile = createImage("compress");
//		FileOutputStream out = new FileOutputStream(outputFile);
//		bm.compress(Bitmap.CompressFormat.JPEG, q, out);
//		return outputFile.getAbsolutePath();
//	}
    public byte[] compressImage(String filePath, int q) {
        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, q, baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private File createImage() throws IOException {
        String imageFileName = "avatar";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
//		File image = File.createTempFile(
//				imageFileName,  /* prefix */
//				".jpg",         /* suffix */
//				storageDir      /* directory */
//		);
        File image = new File(storageDir, imageFileName);
        return image;
    }
}

