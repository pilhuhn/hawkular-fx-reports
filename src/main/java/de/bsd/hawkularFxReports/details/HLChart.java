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
package de.bsd.hawkularFxReports.details;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.bsd.hawkularFxReports.Generator;
import de.bsd.hawkularFxReports.ValueKeeper;
import de.bsd.hawkularFxReports.model.Metric;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignField;

/**
 * A HighLow chart
 *
 * @author Heiko W. Rupp
 */
public class HLChart extends AbstractSimpleExpression<JasperReportBuilder> {

    public HLChart() {
    }

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {


        // FInd the metric id, which is passed in from the outside
        DRDataSource ds = reportParameters.getParameterValue("REPORT_DATA_SOURCE");
        // String id = ds.currentRecord.get("id");
        JRDesignField field = new JRDesignField();
        field.setName("id");
        String id;
        try {
            id = (String) ds.getFieldValue(field);
        } catch (JRException e) {
            e.printStackTrace();  // TODO: Customise this generated block
            return null;
        }

        JRDataSource metrics = getMetrics(id);
        TextColumnBuilder<String> seriesColumn = Columns.column("series",String.class);
        TextColumnBuilder<Date> dateColumn = Columns.column("start",Date.class);
        TextColumnBuilder<Double> highColumn = Columns.column("high",Double.class);
        TextColumnBuilder<Double> avgColumn = Columns.column("avg",Double.class);
        TextColumnBuilder<Double> lowColumn = Columns.column("low",Double.class);


        JasperReportBuilder report = DynamicReports.report();

        report.setDataSource(new JREmptyDataSource());
        report.summary(DynamicReports.cht.highLowChart()
                .setDate(dateColumn)
                .setHigh(highColumn)
                .setLow(lowColumn)
                .setOpen(avgColumn)
                .setClose(avgColumn)
                .setVolume(avgColumn)
                .setShowOpenTicks(true)
                .setShowCloseTicks(true)
                .setDataSource(metrics)
                .setSeries(seriesColumn)
        );

        return report;
    }

    public JRDataSource getMetrics(String id) {

        // TODO the next is specific for URL resources, needs to made
        // more general
        String partialUrl = "hawkular/metrics/gauges/" + id + ".status.duration/data?buckets=120";

        ValueKeeper keeper = ValueKeeper.getInstance();
        String baseUrl = keeper.getBaseUrl();
        String url = baseUrl + partialUrl;


        try {

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Basic " + keeper.getBase64Creds())
                    .addHeader("Accept","application/json")
                    .addHeader("Hawkular-Tenant", keeper.getTenantId())
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();

            DRDataSource dataSource = new DRDataSource("series","start","high","avg","low");
            String payload = response.body().string();
            List<Metric> metrics = Generator.mapfromString(payload, new TypeReference<List<Metric>>() { });

            for (Metric m : metrics) {
                dataSource.add("Duration",new Date(m.getStart()),m.getMax(),m.getAvg(),m.getMin());
            }

            return dataSource;

        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

        return new JREmptyDataSource();
    }


}
