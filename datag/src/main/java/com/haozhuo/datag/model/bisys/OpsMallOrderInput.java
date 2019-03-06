package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 3/6/19.
 */
@Getter
@Setter
public class OpsMallOrderInput extends OpsMallOrder {
    public OpsMallOrderInput(int id, String date, int orderNum, double orderAmount, int payOrderNum, double payOrderAmount, int applyRefundOrderNum, double applyRefundOrderAmount, int refundOrderNum, double refundOrderAmount) {
        super(id, date, orderNum, orderAmount, payOrderNum, payOrderAmount, applyRefundOrderNum, applyRefundOrderAmount, refundOrderNum, refundOrderAmount);
    }
    private String uploadTime;
    private String operateAccount;
}
