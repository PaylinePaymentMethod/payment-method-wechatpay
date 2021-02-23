package com.payline.payment.wechatpay.bean.nested;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class Refund {
    @SerializedName("out_refund_no")
    String outRefundNo;

    @SerializedName("refund_id")
    String refundId;

    @SerializedName("refund_channel")
    String refundChannel;

    @SerializedName("refund_fee")
    String refundFee;

    @SerializedName("refund_status")
    RefundStatus refundStatus;

    @SerializedName("refund_recv_account")
    String refundRecvAccount;

    @SerializedName("refund_success_time")
    String refundSuccessTime;
}
