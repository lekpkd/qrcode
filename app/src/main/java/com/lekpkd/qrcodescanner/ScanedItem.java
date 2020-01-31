package com.lekpkd.qrcodescanner;

import java.util.Date;

class ScanedItem {

    private  String content;
    private Date date;

    public ScanedItem(String content, Date date) {
        this.content = content;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
