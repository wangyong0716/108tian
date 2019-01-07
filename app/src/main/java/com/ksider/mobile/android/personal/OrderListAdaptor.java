package com.ksider.mobile.android.personal;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.OrderModel;
import com.ksider.mobile.android.utils.Status;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yong on 2015/5/22.
 */
public class OrderListAdaptor extends PagingBaseAdapter<OrderModel> {
    protected Activity mContext;
    private String type;

    public OrderListAdaptor(Activity context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public OrderModel getItem(int postion) {
        if (0 <= postion && postion < items.size()) {
            return items.get(postion);
        }
        return null;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(final int postion, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_order_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.listTitle = (TextView) view.findViewById(R.id.list_title);
            viewHolder.payState = (TextView) view.findViewById(R.id.pay_state);
            viewHolder.totalFee = (TextView) view.findViewById(R.id.total_fee);
            viewHolder.quantity = (TextView) view.findViewById(R.id.quantity);
            viewHolder.payStateLayout = (TextView) view.findViewById(R.id.pay_state_layout);
//            viewHolder.serialNumber = (TextView) view.findViewById(R.id.serial_number);
            view.setTag(viewHolder);
            viewHolder.loadImageView = (LoadImageView) view.findViewById(R.id.listview_headImage);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final OrderModel data = getItem(postion);
        if (data.getProductName() != null) {
            viewHolder.listTitle.setText(data.getProductName());
        } else {
            viewHolder.listTitle.setVisibility(View.GONE);
        }
        viewHolder.totalFee.setText(StringUtils.getPrice(data.getTotalFee()) + "元");
        viewHolder.quantity.setText(data.getQuantity() + "");
//        viewHolder.serialNumber.setText(StringUtils.serialNumberFormat(data.getSerialNumber() + ""));
        if (data.getProductImg() != null && data.getProductImg().length() > 4) {
            viewHolder.loadImageView.setImageResource(data.getProductImg());
        }
//        TextView payStateLayout = (TextView) view.findViewById(R.id.pay_state_layout);
//        payStateLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OrderModel model = getItem(postion);
//                JSONObject data = new JSONObject();
//                try {
//                    JSONObject order = new JSONObject();
//                    order.put("productName", model.getProductName());
//                    order.put("totalFee", model.getTotalFee());
//                    order.put("coupons", model.getCoupons());
//                    order.put("couponDiscount", model.getCouponDiscount());
//                    order.put("serialNumber", model.getSerialNumber());
//                    order.put("consumeTime", model.getConsumeTime());
//                    order.put("status", model.getStatus());
//                    order.put("quantity", model.getQuantity());
//                    order.put("productId", model.getProductId());
//                    data.put("order", order);
//                    Intent intent = new Intent(mContext, PurchaseAcitvity.class);
//                    intent.putExtra("data", data.toString());
//                    intent.putExtra("payment", true);
//                    mContext.startActivity(intent);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        if (type.equals(OrderActivity.choser[0])) {
            viewHolder.payState.setText("待付款");
            viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.personal_info_text_color));
            viewHolder.payStateLayout.setVisibility(View.VISIBLE);
            viewHolder.payStateLayout.setText(mContext.getResources().getString(R.string.order_pay_label_pay));
            viewHolder.payStateLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject order = new JSONObject();
                        order.put("productName", data.getProductName());
                        order.put("totalFee", data.getTotalFee());
                        order.put("coupons", data.getCoupons());
                        order.put("couponDiscount", data.getCouponDiscount());
                        order.put("serialNumber", data.getSerialNumber());
                        order.put("consumeTime", data.getConsumeTime());
                        order.put("status", data.getStatus());
                        order.put("quantity", data.getQuantity());
                        order.put("productId", data.getProductId());
                        order.put("refund", data.getRefund());
                        order.put("sellPrice", data.getSellPrice());
//                        data.put("order", order);
                        Intent intent = new Intent(mContext, PurchaseAcitvity.class);
//                        intent.putExtra("data", data.toString());
                        intent.putExtra("order", order.toString());
                        intent.putExtra("payment", true);
                        mContext.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (type.equals(OrderActivity.choser[1])) {
            viewHolder.payState.setText("待消费");
            viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.personal_info_text_color_selected));
            viewHolder.payStateLayout.setVisibility(View.GONE);
        } else if (type.equals(OrderActivity.choser[2])) {
            switch ((byte) data.getStatus()) {
                case Status.ORDER_REFOUND_REQUIRED:
                case Status.ORDER_REFOUND_APPROVED:
                case Status.ORDER_PARTIALLY_REFUND_REQUIRED:
                case Status.ORDER_PARTIALLY_REFUND_APPROVED:
                    viewHolder.payState.setText("退款中");
                    viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.order_detail_coupon_text_color));
                    viewHolder.payStateLayout.setVisibility(View.GONE);
                    break;
                case Status.ORDER_REFOUND_DONE:
                case Status.ORDER_PARTIALLY_REFUND_DONE:
                    viewHolder.payState.setText("退款成功");
                    viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.order_detail_coupon_text_color));
                    viewHolder.payStateLayout.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else if (type.equals(OrderActivity.choser[3])) {
            viewHolder.payState.setText("已消费");
            viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.personal_info_text_color));
            view.findViewById(R.id.pay_state_layout).setVisibility(View.VISIBLE);
            if (data.getEvaluate() == 1) {
                viewHolder.payStateLayout.setText(mContext.getResources().getString(R.string.order_pay_label_evaluated));
                viewHolder.payStateLayout.setBackgroundResource(R.drawable.evaluated_button_frame);
                viewHolder.payStateLayout.setClickable(false);
            } else {
                viewHolder.payStateLayout.setText(mContext.getResources().getString(R.string.order_pay_label_evaluate));
                viewHolder.payStateLayout.setBackgroundResource(R.drawable.login_button_frame);
                viewHolder.payStateLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (data != null) {
                            Intent intent = new Intent(mContext, EvaluateActivity.class);
                            intent.putExtra("serialNumber", data.getSerialNumber());
                            mContext.startActivity(intent);
                        }
                    }
                });
            }
        } else {
            return null;
        }
//        switch ((byte) data.getStatus()) {
//            case Status.ORDER_INIT:
//                viewHolder.payState.setText("待付款");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.personal_info_text_color));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.VISIBLE);
//                break;
//            case Status.ORDER_REFOUND_REQUIRED:
//            case Status.ORDER_REFOUND_APPROVED:
//            case Status.ORDER_PARTIALLY_REFUND_REQUIRED:
//            case Status.ORDER_PARTIALLY_REFUND_APPROVED:
//                viewHolder.payState.setText("退款中");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.order_detail_coupon_text_color));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//                break;
//            case Status.ORDER_REFOUND_DONE:
//            case Status.ORDER_PARTIALLY_REFUND_DONE:
//                viewHolder.payState.setText("退款成功");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.order_detail_coupon_text_color));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//                break;
//            case Status.ORDER_PAYMENT_DONE:
//            case Status.ORDER_REFOUND_REJECTED:
//            case Status.ORDER_PARTIALLY_REFUND_REJECTED:
//                viewHolder.payState.setText("待消费");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.personal_info_text_color_selected));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//                break;
//            case Status.ORDER_CANCELED:
//                viewHolder.payState.setText("订单取消");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.gray));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//            case Status.ORDER_EXPIRED:
//                viewHolder.payState.setText("订单过期");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.gray));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//            default:
//                viewHolder.payState.setText("错误订单");
//                viewHolder.payState.setTextColor(mContext.getResources().getColor(R.color.gray));
//                view.findViewById(R.id.pay_state_layout).setVisibility(View.GONE);
//                break;
//        }
        return view;
    }

    private static class ViewHolder {
        LoadImageView loadImageView;
        TextView listTitle;
        TextView payState;
        TextView totalFee;
        TextView quantity;
        TextView payStateLayout;
//        TextView serialNumber;
    }
}
