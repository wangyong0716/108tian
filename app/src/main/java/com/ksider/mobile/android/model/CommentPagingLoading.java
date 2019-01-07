package com.ksider.mobile.android.model;

import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class CommentPagingLoading {
	protected int mPage = 0;
	protected String mHostId;
	protected Boolean mHasMore = true;
	public CommentPagingLoading(String uid) {
		mHostId = uid;
	}

	public void loadMoreData() {
		if(mHasMore){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("act", "get");
			params.put("hostId", mHostId);
			params.put("page", mPage);
			params.put("step", Constants.PAGING_STEP);
			Network.getInstance().addToRequestQueue(getRequest(APIUtils.getUrl(APIUtils.COMMENT, params)));
			mPage++;
		}
	}
	public void reload(){
		mHasMore = true;
		mPage = 0;
		loadMoreData();
	}

	protected void parseComment(JSONObject data) {
		try {
			JSONObject users = data.getJSONObject("users");
			JSONArray comments = data.getJSONArray("comments");
			JSONArray items = new JSONArray();
			for (int i = 0; i < comments.length(); i++) {
				try {
					JSONObject item = new JSONObject();
					JSONObject comment = comments.getJSONObject(i);
					item.put("content", comment.getString("content"));
					item.put("created", comment.getLong("created"));
					item.put("uid", comment.getString("uid"));
					item.put("cid", comment.getString("_id"));
					JSONObject user = users.getJSONObject(comment.getString("uid"));
					item.put("name", user.getString("name"));
					try {
						item.put("figureurl", user.getString("figureurl"));
					} catch (Exception e) {
					}

					try {
						item.put("total", comment.getDouble("total"));
					} catch (Exception e) {
						item.put("total", 0);
					}
					try {
						item.put("thumbsUp", comment.getString("thumbsUp")); 	
                    } catch (Exception e) {
                    	item.put("thumbsUp", "0");
                    }
					try {
						item.put("thumbsDown", comment.getString("thumbsDown"));
					}catch (Exception e) {
                    	item.put("thumbsDown", "0");
                    }
					items.put(item);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mHasMore = items.length()>0;
			JSONObject result = new JSONObject();
			JSONObject score = data.getJSONObject("score");
			try {
				result.put("count", score.get("count"));
            } catch (Exception e) {
            	result.put("count", 0);
            }
			try {
			result.put("grade", score.get("avg"));
			}catch (Exception e) {
            	result.put("grade", 0);
            }
			result.put("items", items);
			itemRender(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected JsonObjectRequest getRequest(String url) {
		return new JsonObjectRequest(url, null, new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					if (response != null && response.getInt("status") == 0) {
						parseComment(response.getJSONObject("data"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.v(Constants.LOG_TAG, "response:" + error.toString());
			}
		});
	}
	public abstract void itemRender(JSONObject data);

}
