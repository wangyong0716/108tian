package com.ksider.mobile.android.personal;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.EvaluationModel;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.EvaluateDialog;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.evaluation.AutoLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 11/4/15.
 */
public class EvaluateActivity extends BaseActivity {
    private AutoLayout evaluationLayout;
    private AutoLayout inputLayout;
    private ArrayList<EvaluationModel> originEvaluations = new ArrayList<EvaluationModel>();
    private ArrayList<EvaluationModel> selfEvaluations = new ArrayList<EvaluationModel>();

    private long serialNumber;
    private double score;
    private String content;
    private boolean anonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_score);
        evaluationLayout = (AutoLayout) findViewById(R.id.evaluation_layout);
        inputLayout = (AutoLayout) findViewById(R.id.evaluation_input);
        new SlidingLayout(this);
        customActionBar();

        serialNumber = getIntent().getExtras().getLong("serialNumber");

        TextView title = (TextView) findViewById(R.id.list_title);
        title.setText("评价");

        RatingBar rating = (RatingBar) findViewById(R.id.rating_bar);
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int rate = (int) rating;
                if (rate < 1) {
                    rate = 1;
                    ratingBar.setRating(rate);
                }
                switch (rate) {
                    case 1:
                        ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.evaluate_description_1));
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.evaluate_description_2));
                        break;
                    case 3:
                        ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.evaluate_description_3));
                        break;
                    case 4:
                        ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.evaluate_description_4));
                        break;
                    case 5:
                        ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.evaluate_description_5));
                        break;
                    default:
                        break;
                }
            }
        });
        CheckBox anonymousBox = (CheckBox) findViewById(R.id.anonymous_button);
        anonymous = anonymousBox.isChecked();
        anonymousBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                anonymous = isChecked;
            }
        });

        findViewById(R.id.commit_evaluation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEvaluateString();
                if (content != null && !content.equals("")) {
                    Network.getInstance().addToRequestQueue(getEvaluateRequest());
                    EvaluateActivity.this.finish();
                } else {
                    Toast.makeText(EvaluateActivity.this, "请添加标签！", Toast.LENGTH_LONG).show();
                }
            }
        });

        inputLayout.setOnItemClickListener(new AutoLayout.OnItemClickListener() {
            @Override
            public void onClick(int index) {

            }
        });
        Network.getInstance().addToRequestQueue(geProductEvaluationsRequest());
    }

    public void getEvaluateString() {
        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        score = ratingBar.getRating();
        ArrayList<Integer> indexArray = evaluationLayout.getSelectedArray();
        JSONArray array = new JSONArray();
        for (int i = 0; i < indexArray.size(); i++) {
            int temp = indexArray.get(i);
            if (temp >= 0 && temp < originEvaluations.size()) {
                array.put(originEvaluations.get(temp).getContent());
            }
        }
        //add more evaluation
        ArrayList<Integer> inputArray = inputLayout.getSelectedArray();
        for (int i = 0; i < inputArray.size(); i++) {
            int temp = inputArray.get(i);
            if (temp >= 0 && temp < selfEvaluations.size()) {
                array.put(selfEvaluations.get(temp).getContent());
            }
        }

        if (array.length() <= 0) {
            content = "";
        } else {
            content = array.toString();
        }
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException ue) {
            content = "";
            ue.printStackTrace();
        }
    }

    public void showEvaluations(final AutoLayout layout, ArrayList<EvaluationModel> evaluations) {
        layout.removeAllViews();
        for (int i = 0; i < evaluations.size(); i++) {
            TextView evaluation = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
            evaluation.setText(getResources().getString(R.string.evaluate_tag, evaluations.get(i).getContent(), evaluations.get(i).getHitCount()));
            layout.addView(evaluation);
        }
        TextView moreButton = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        moreButton.setBackgroundResource(R.drawable.more_evaluation_unselect);
        layout.setMoreButton(moreButton, new AutoLayout.OnItemClickListener() {
            @Override
            public void onClick(int index) {
                Log.i("AAA", "more evaluation");
            }
        });
        TextView addButton = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        addButton.setBackgroundResource(R.drawable.add_evaluation_unselect);
        layout.setAddButton(addButton, new AutoLayout.OnItemClickListener() {
            @Override
            public void onClick(int index) {
                layout.setAddEnable(true);
                EvaluateDialog.Builder builder = new EvaluateDialog.Builder(EvaluateActivity.this);
                builder.setOnEditDoneListener(new EvaluateDialog.Builder.OnEditDoneListener() {
                    @Override
                    public void editDone(String content) {
                        if (content != null && !content.equals("")) {
                            EvaluationModel model = new EvaluationModel();
                            model.setContent(content);
                            addEvaluation(model);
                        }
                    }
                }).setDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        layout.setAddEnable(false);
                    }
                }).show();
            }
        });
        layout.setOnItemClickListener(new AutoLayout.OnItemClickListener() {
            @Override
            public void onClick(int index) {
                Log.i("AAA", "index=" + index);
            }
        });
    }

    public void addEvaluation(AutoLayout layout, ArrayList<EvaluationModel> evaluations) {
        layout.removeAllViews();
        for (int i = 0; i < evaluations.size(); i++) {
            TextView evaluation = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
            evaluation.setText(evaluations.get(i).getContent());
            layout.addView(evaluation, true);
        }
    }

    public void addEvaluation(EvaluationModel model) {
        selfEvaluations.add(model);
        TextView evaluation = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        evaluation.setText(model.getContent());
        inputLayout.addView(evaluation, true);
    }

    public void processEvaluations(JSONArray array) {
        originEvaluations.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                EvaluationModel model = new EvaluationModel();
                model.setContent(object.getString("content"));
                model.setHitCount(object.getInt("hitCount"));
                originEvaluations.add(model);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        showEvaluations(evaluationLayout, originEvaluations);
    }

    public String geProductEvaluationsUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNumber", serialNumber);
        params.put("action", "getEvaluateTags");
        return APIUtils.getUrl(APIUtils.EVALUATE, params);
    }

    public JsonObjectRequest geProductEvaluationsRequest() {
        Log.v("AAA", "geProductEvaluations->url=" + geProductEvaluationsUrl());
        return new JsonObjectRequest(geProductEvaluationsUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                try {
                    processEvaluations(object.getJSONArray("data"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    public String evaluationsUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNumber", serialNumber);
        params.put("action", "addEvaluate");
        params.put("score", score);
        params.put("content", content);
        params.put("anonymous", anonymous);
        return APIUtils.getUrl(APIUtils.EVALUATE, params);
    }

    public JsonObjectRequest getEvaluateRequest() {
        String url = evaluationsUrl();
        Log.v("AAA", "getEvaluateRequest->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                try {
                    if (object.getInt("status") == 0) {
                        //evaluate success
                        Log.i("AAA", "evaluate success!");
                    } else {
                        Toast.makeText(EvaluateActivity.this, object.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException js) {
                    Toast.makeText(EvaluateActivity.this, "评价失败，请稍后重试～", Toast.LENGTH_LONG).show();
                    js.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(EvaluateActivity.this, "评价失败，请稍后重试～", Toast.LENGTH_LONG).show();
            }
        });
    }
}