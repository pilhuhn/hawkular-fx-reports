package de.bsd.hawkularFxReports;
/**
 * TODO document me
 *
 * @author Heiko W. Rupp
 */

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.bsd.hawkularFxReports.model.HawkResource;
import de.bsd.hawkularFxReports.model.ResourceType;
import de.bsd.hawkularFxReports.model.Tenant;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;

public class FxReports extends Application {

    OkHttpClient httpClient;

    public static void main(String[] args) {
        launch(args);
    }



    @Override
    public void start(Stage stage) throws Exception {


        URL mainFxml = getClass().getClassLoader().getResource("main.fxml");
        System.out.println(mainFxml.toString());

        Parent root = FXMLLoader.load(mainFxml);
        Scene scene = new Scene(root, 450, 300);

        stage.setTitle("Hawkular FX Reports");
        stage.setScene(scene);
        stage.show();
    }


    @FXML
    private TextField userField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField urlField;

    @FXML
    private TextField msgField;

    @FXML
    void generateReport(ActionEvent event) {

        System.out.println("generate pressed");
        String tenant = fetchTenant();

        List<String> resourceTypes = getResourceTypesForTenant(tenant);

    }

    private List<String> getResourceTypesForTenant(String tenant) {
        httpClient = new OkHttpClient();

        Request request = getRequestForUrl("hawkular/inventory/resourceTypes", null);

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            msgField.setText(e.getMessage());
            return null;
        }

        try {
            String payload = response.body().string();
            List<ResourceType> typesMap = mapfromString(payload,new TypeReference<List<ResourceType>>() {});


            JasperReportBuilder reportBuilder = report()
                    .title(Components.text("Hawkular FX Report for " + getBaseUrlFromField())
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setStyle(Styles.style().bold()))
                    // The next may look odd, but  we need it for Jasper to render anything
                    .setDataSource(new JREmptyDataSource())
                    .pageFooter(Components.pageXofY());


            for (ResourceType type : typesMap ) {

                SubreportBuilder builder;
                String typeName = type.getId();
                if (ResourceDetailFactory.supports(typeName)) {
                    List<HawkResource> resources = getResourcesForType(tenant,type);
                    builder = ResourceDetailFactory.getForType(typeName,resources);
                    reportBuilder.addDetail(builder, cmp.verticalGap(20));
                }

            }

            // Show the report
            reportBuilder.show(false);

        } catch (IOException | DRException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

        return null;  // TODO: Customise this generated block
    }


    private List<HawkResource> getResourcesForType(String tenantId, ResourceType type) {

        httpClient = new OkHttpClient();

        Request request = getRequestForUrl("hawkular/inventory/resourceTypes/" + type.getId() + "/resources",null);

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            msgField.setText(e.getMessage());
            return Collections.EMPTY_LIST;
        }

        System.out.println(response.toString());
        String payload = null;
        try {
            payload = response.body().string();
            System.out.println(payload);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            return null;
        }

        List<HawkResource> resources = mapfromString(payload, new TypeReference<List<HawkResource>>() {});

        return resources;

    }

    private Request getRequestForUrl(String partialUrl, String parameters) {
        String baseUrl = getBaseUrlFromField();
        String url = baseUrl + partialUrl;

        if (parameters!=null) {
            url += "?" + parameters;
       }

        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Basic " + getBase64Credentials())
                .addHeader("Accept","application/json")
                .build();
    }

    /**
     * Return the base URL from the input field. If it does
     * not end in /, append a /
     * @return base url.
     */
    private String getBaseUrlFromField() {
        String text = urlField.getText();
        if (!text.endsWith("/")) {
            text += "/";
        }
        return text;
    }


    String fetchTenant()  {

        httpClient = new OkHttpClient();

        String url = getBaseUrlFromField() + "hawkular/inventory/tenant";


        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Basic " + getBase64Credentials())
                .get()
                .addHeader("Accept","application/json")
                .build();


        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            msgField.setText(e.getMessage());
            return null;
        }

        System.out.println(response.toString());
        if (response.code()!=200) {
            msgField.setText(response.message());
            return null;
        } else {


            String content = null;
            try {
                content = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();  // TODO: Customise this generated block
            }
            System.out.println(content);

            Tenant t = mapfromString(content ,Tenant.class);

            return t.getId();
        }

    }

    private String getBase64Credentials() {
        String base64Encode;
        try {
            base64Encode = Base64.getMimeEncoder().encodeToString((userField.getText()
                    + ':'    + passwordField.getText()).getBytes("UTF-8"));
            return base64Encode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            return null;
        }
    }

    private <T> T mapfromString(String content, Class<T> targetClass) {

        ObjectMapper mapper = new ObjectMapper();

        T ret = null;
        try {
            ret = (T)mapper.readValue(content,targetClass);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }
        return ret;
    }

    private <T> T mapfromString(String content, TypeReference<T> targetClass) {

        ObjectMapper mapper = new ObjectMapper();

        T ret = null;
        try {
            ret = (T)mapper.readValue(content,targetClass);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }
        return ret;
    }

}
