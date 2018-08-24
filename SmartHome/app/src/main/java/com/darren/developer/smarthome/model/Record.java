package com.darren.developer.smarthome.model;

public class Record {
    private float units1;
    private float units2;
    private float units3;
    private float units4;
    private float units5;
    private float units6;
    private long date;

    public Record(){
    }

    public Record(long date, float units1, float units2, float units3, float units4, float units5, float units6) {
        this.date = date;
        this.units1 = units1;
        this.units2 = units2;
        this.units3 = units3;
        this.units4 = units4;
        this.units5 = units5;
        this.units6 = units6;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getUnits1() {
        return units1;
    }

    public void setUnits1(float units1) {
        this.units1 = units1;
    }

    public float getUnits2() {
        return units2;
    }

    public void setUnits2(float units2) {
        this.units2 = units2;
    }

    public float getUnits3() {
        return units3;
    }

    public void setUnits3(float units3) {
        this.units3 = units3;
    }

    public float getUnits4() {
        return units4;
    }

    public void setUnits4(float units4) {
        this.units4 = units4;
    }

    public float getUnits5() {
        return units5;
    }

    public void setUnits5(float units5) {
        this.units5 = units5;
    }

    public float getUnits6() {
        return units6;
    }

    public void setUnits6(float units6) {
        this.units6 = units6;
    }
}
