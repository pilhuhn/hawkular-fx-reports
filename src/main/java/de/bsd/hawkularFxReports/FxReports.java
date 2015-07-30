package de.bsd.hawkularFxReports;
/**
 * Main class of a Reports generator for Hawkular.
 * This is a JavaFX application that drives
 * a DynamicReports (Jasper) generator.
 *
 * @author Heiko W. Rupp
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.bsd.hawkularFxReports.model.Tenant;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    TextField msgField;

    @FXML
    ProgressBar progressBar;

    @FXML
    void generateReport(ActionEvent event) {

        progressBar.setProgress(0.0);
        System.out.println("generate pressed");
        String tenant = fetchTenant();

        if (tenant == null) {
            msgField.setText("Can't connect to server.");
            return;
        }
        progressBar.setProgress(10.0);
        ValueKeeper.getInstance().setTenantId(tenant);
        ValueKeeper.getInstance().setUser(userField.getText());
        ValueKeeper.getInstance().setBaseUrl(getBaseUrlFromField());
        ValueKeeper.getInstance().setBase64Creds(getBase64Credentials());


        Generator generator = new Generator(this, tenant);
        Thread t = new Thread(generator);
        t.start();

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
}
