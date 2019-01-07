package com.ksider.mobile.android.partion;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.PicsViewActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.AtlasModel;
import com.ksider.mobile.android.view.LoadImageView;

/**
 * Created by yong on 7/29/15.
 */
public class AtlasView extends LinearLayout {
    private Context context;

    public AtlasView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AtlasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public AtlasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_atlas_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void addMoreAtlas(final AtlasModel atlasModel) {
//        LinearLayout container = (LinearLayout) findViewById(R.id.atlas_container);
//        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.single_atlas_view, null);
//        ((AlignTextView) view.findViewById(R.id.atlas_content)).setContent(atlasModel.getDesc());
//        if (atlasModel.getImgsCount() > 0) {
//            LoadImageView liv = (LoadImageView) view.findViewById(R.id.atlas_atlas);
//            liv.setImageResource(atlasModel.getImgs().get(0));
//            liv.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(context, PicsViewActivity.class);
//                    intent.putExtra("pics", atlasModel.getImgs());
//                    context.startActivity(intent);
//                }
//            });
//        }
//        container.addView(view);
    }

    public void addAtlas(final AtlasModel atlasModel) {
        if (findViewById(R.id.atlas_relative_layout_1).getVisibility() != VISIBLE) {
            findViewById(R.id.atlas_relative_layout_1).setVisibility(VISIBLE);
            findViewById(R.id.atlas_content_1).setVisibility(VISIBLE);
//            ((AlignTextView) findViewById(R.id.atlas_content_1)).setContent(atlasModel.getDesc());
            ((TextView) findViewById(R.id.atlas_content_1)).setText(atlasModel.getDesc());
            if (atlasModel.getImgsCount() > 0) {
                LoadImageView liv = (LoadImageView) findViewById(R.id.atlas_atlas_1);
                liv.setImageResource(atlasModel.getImgs().get(0));
                liv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PicsViewActivity.class);
                        intent.putExtra("pics", atlasModel.getImgs());
                        context.startActivity(intent);
                    }
                });
            }
        } else if (findViewById(R.id.atlas_relative_layout_2).getVisibility() != VISIBLE) {
            findViewById(R.id.atlas_relative_layout_2).setVisibility(VISIBLE);
            findViewById(R.id.atlas_content_2).setVisibility(VISIBLE);
//            ((AlignTextView) findViewById(R.id.atlas_content_2)).setContent(atlasModel.getDesc());
            ((TextView) findViewById(R.id.atlas_content_2)).setText(atlasModel.getDesc());
            if (atlasModel.getImgsCount() > 0) {
                LoadImageView liv = (LoadImageView) findViewById(R.id.atlas_atlas_2);
                liv.setImageResource(atlasModel.getImgs().get(0));
                liv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PicsViewActivity.class);
                        intent.putExtra("pics", atlasModel.getImgs());
                        context.startActivity(intent);
                    }
                });
            }
        } else {
            addMoreAtlas(atlasModel);
        }
    }

    public void removeAtlas() {
//        LinearLayout container = (LinearLayout) findViewById(R.id.atlas_container);
//        container.removeAllViews();
    }
}
