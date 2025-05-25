package putrack.server.dto;

public class SensorDataDto {

    private Integer deviceId;
    private String timestamp;
    private Double airTemp;
    private Double airHumid;
    private Double cushionTemp;

    public SensorDataDto() {}

    public SensorDataDto(Integer deviceId, String timestamp, Double airTemp, Double airHumid, Double cushionTemp) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.airTemp = airTemp;
        this.airHumid = airHumid;
        this.cushionTemp = cushionTemp;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Double getAirTemp() {
        return airTemp;
    }

    public Double getAirHumid() {
        return airHumid;
    }

    public Double getCushionTemp() {
        return cushionTemp;
    }
}
