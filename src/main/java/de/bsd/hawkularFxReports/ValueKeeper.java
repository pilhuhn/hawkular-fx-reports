package de.bsd.hawkularFxReports;

/**
 * Singleton to keep some globally needed stuff around.
 *
 * @author Heiko W. Rupp
 */
public class ValueKeeper {
    private static ValueKeeper ourInstance = new ValueKeeper();

    private String user;
    private String tenantId;
    private String baseUrl;
    private String base64Creds;

    public static ValueKeeper getInstance() {
        return ourInstance;
    }

    private ValueKeeper() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBase64Creds() {
        return base64Creds;
    }

    public void setBase64Creds(String base64Creds) {
        this.base64Creds = base64Creds;
    }
}
