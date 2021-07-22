package com.cf.cf685.wifi;
//
public class DevGroup {
    public int  total;

    public int  devid0;
    public String devid0ip;
    public int  devid1;
    public String devid1ip;
    public int  devid2;
    public String devid2ip;
    public int  devid3;
    public String devid3ip;
    public int  devid4;
    public String devid4ip;






//DevGroup{total=1, devid0=10000, devid0ip='192.168.10.124', devid1=0, devid1ip='', devid2=0, devid2ip='', devid3=0, devid3ip='', devid4=0, devid4ip=''}
    @Override
    public String toString() {
        return "DevGroup{" +
                "total=" + total +
                ", devid0=" + devid0 +
                ", devid0ip='" + devid0ip + '\'' +
                ", devid1=" + devid1 +
                ", devid1ip='" + devid1ip + '\'' +
                ", devid2=" + devid2 +
                ", devid2ip='" + devid2ip + '\'' +
                ", devid3=" + devid3 +
                ", devid3ip='" + devid3ip + '\'' +
                ", devid4=" + devid4 +
                ", devid4ip='" + devid4ip + '\'' +
                '}';
    }
}
