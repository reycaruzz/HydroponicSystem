package com.hidroponik.farm;

public class DataSensor {
    String tm, ppm_value, ph_value, suhu_value, setpoint_value, p_abmix, p_air, p_phu, p_phd;

    DataSensor(String tm, String ppm_value, String ph_value, String suhu_value,
               String setpoint_value, String p_abmix, String p_air, String p_phu, String p_phd) {
        this.tm = tm;
        this.ppm_value = ppm_value;
        this.ph_value = ph_value;
        this.suhu_value = suhu_value;
        this.setpoint_value = setpoint_value;
        this.p_abmix = p_abmix;
        this.p_air = p_air;
        this.p_phu = p_phu;
        this.p_phd = p_phd;
    }

    public String getTm() {
        return tm;
    }

    public String getPpm_value() {
        return ppm_value;
    }

    public String getPh_value() {
        return ph_value;
    }

    public String getSuhu_value() {
        return suhu_value;
    }

    public String getSetpoint_value() {
        return setpoint_value;
    }

    public String getP_abmix() {
        return p_abmix;
    }

    public String getP_air() {
        return p_air;
    }

    public String getP_phu() {
        return p_phu;
    }

    public String getP_phd() {
        return p_phd;
    }
}
