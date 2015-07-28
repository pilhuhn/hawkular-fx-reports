/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bsd.hawkularFxReports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.bsd.hawkularFxReports.model.HawkResource;
import de.bsd.hawkularFxReports.model.ResourceType;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;

/**
 * The actual generator driving DynamicReports
 *
 * @author Heiko W. Rupp
 */
public class Generator implements Runnable {
    private final FxReports main;
    private String tenant;
    private OkHttpClient httpClient;

    public Generator(FxReports main, String tenant) {
        this.main = main;

        this.tenant = tenant;
        httpClient = new OkHttpClient();
    }

    @Override
    public void run() {

        // runs the generation
        List<ResourceType> resourceTypes = getResourceTypesForCurrentTenant();
        main.progressBar.setProgress(20.0);
        if (resourceTypes.isEmpty()) {
            return;
        }
        generateForResourceTypes(resourceTypes);

    }


    private List<ResourceType> getResourceTypesForCurrentTenant() {

        Request request = getRequestForUrl("hawkular/inventory/resourceTypes", null);

        List<ResourceType> typesMap = Collections.EMPTY_LIST;
        try {
            Response response = httpClient.newCall(request).execute();
            String payload = response.body().string();

            typesMap = mapfromString(payload, new TypeReference<List<ResourceType>>() { });

        } catch (Exception e) {
            e.printStackTrace(); // TODO: Customise this generated block
            main.msgField.setText(e.getMessage());
        }

        return typesMap;

    }

    private void generateForResourceTypes(List<ResourceType> typesMap) {

        try {
            JasperReportBuilder reportBuilder = report()
                    .title(Components.text("Hawkular FX Report for " + ValueKeeper.getInstance().getBaseUrl())
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setStyle(Styles.style().bold()))
                    // The next may look odd, but  we need it for Jasper to render anything
                    .setDataSource(new JREmptyDataSource())
                    .pageFooter(Components.pageXofY());

            double progress = 30.0;
            main.progressBar.setProgress(progress);

            for (ResourceType type : typesMap ) {

                SubreportBuilder builder;
                String typeName = type.getId();
                if (ResourceDetailFactory.supports(typeName)) {
                    List<HawkResource> resources = getResourcesForType(tenant,type);
                    builder = ResourceDetailFactory.getForType(typeName,resources);
                    reportBuilder.addDetail(builder, cmp.verticalGap(30));

                    progress += 50.0/ResourceDetailFactory.itemCount();
                    main.progressBar.setProgress(progress);
                }

            }

            // Show the report
            reportBuilder.show(true);
            main.progressBar.setProgress(100.0);

        } catch (DRException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }
    }


    private List<HawkResource> getResourcesForType(String tenantId, ResourceType type) {


        Request request = getRequestForUrl("hawkular/inventory/resourceTypes/" + type.getId() + "/resources",null);

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            main.msgField.setText(e.getMessage());
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
        String baseUrl = ValueKeeper.getInstance().getBaseUrl();
        String url = baseUrl + partialUrl;

        if (parameters!=null) {
            url += "?" + parameters;
       }

        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Basic " + ValueKeeper.getInstance().getBase64Creds())
                .addHeader("Accept","application/json")
                .build();
    }


    public static <T> T mapfromString(String content, TypeReference<T> targetClass) {

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
