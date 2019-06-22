package softmaticbd.com.techunter.Model;

public class ServiceExpert {
    private String expId;
    private String expService;
    private String expPrice;
    private String serviceLocation;

    public ServiceExpert() {
    }

    public ServiceExpert(String expId, String expService, String expPrice) {
        this.expId = expId;
        this.expService = expService;
        this.expPrice = expPrice;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getExpService() {
        return expService;
    }

    public void setExpService(String expService) {
        this.expService = expService;
    }

    public String getExpPrice() {
        return expPrice;
    }

    public void setExpPrice(String expPrice) {
        this.expPrice = expPrice;
    }

    public String getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(String serviceLocation) {
        this.serviceLocation = serviceLocation;
    }
}
