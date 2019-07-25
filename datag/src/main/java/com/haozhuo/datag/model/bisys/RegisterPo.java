package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterPo {
    private String date;
    private int registerUsers;
    private int totalRegisterUsers;
    private int activeUsers;
}
