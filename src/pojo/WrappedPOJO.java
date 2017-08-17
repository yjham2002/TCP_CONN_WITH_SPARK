package pojo;

/**
 * Created by a on 2017-08-17.
 */
public class WrappedPOJO {
    private RealtimePOJO realtimePOJO;
    private String farmString;
    private String harvString;

    public WrappedPOJO(RealtimePOJO realtimePOJO, String farmString, String harvString) {
        this.realtimePOJO = realtimePOJO;
        this.farmString = farmString;
        this.harvString = harvString;
    }

    public RealtimePOJO getRealtimePOJO() {
        return realtimePOJO;
    }

    public void setRealtimePOJO(RealtimePOJO realtimePOJO) {
        this.realtimePOJO = realtimePOJO;
    }

    public String getFarmString() {
        return farmString;
    }

    public void setFarmString(String farmString) {
        this.farmString = farmString;
    }

    public String getHarvString() {
        return harvString;
    }

    public void setHarvString(String harvString) {
        this.harvString = harvString;
    }
}
