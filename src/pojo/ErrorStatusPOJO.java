package pojo;

/**
 * Created by a on 2017-06-12.
 */
public class ErrorStatusPOJO {

    private int order;
    private String errstat_start_md;
    private String errstat_start_time;
    private String errstat_progress_time;

    public ErrorStatusPOJO(int order, String start_md, String start_hm, String passed_hm){
        this.order = order;
        this.errstat_start_md = start_md;
        this.errstat_start_time = start_hm;
        this.errstat_progress_time = passed_hm;
    }

    private ErrorStatusPOJO(){}

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getErrstat_start_md() {
        return errstat_start_md;
    }

    public void setErrstat_start_md(String errstat_start_md) {
        this.errstat_start_md = errstat_start_md;
    }

    public String getErrstat_start_time() {
        return errstat_start_time;
    }

    public void setErrstat_start_time(String errstat_start_time) {
        this.errstat_start_time = errstat_start_time;
    }

    public String getErrstat_progress_time() {
        return errstat_progress_time;
    }

    public void setErrstat_progress_time(String errstat_progress_time) {
        this.errstat_progress_time = errstat_progress_time;
    }
}
