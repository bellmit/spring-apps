package com.haozhuo.datag.model.report;


public class ReVO {
    private String rpt_id;

    public String getRpt_id() {
        return rpt_id;
    }

    public void setRpt_id(String rpt_id) {
        this.rpt_id = rpt_id;
    }

    public String getChk_date() {
        return chk_date;
    }

    public void setChk_date(String chk_date) {
        this.chk_date = chk_date;
    }


    private String chk_date;

    public String getRpt_create_date() {
        return rpt_create_date;
    }

    public void setRpt_create_date(String rpt_create_date) {
        this.rpt_create_date = rpt_create_date;
    }

    private String rpt_create_date;
}
