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

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;

import java.util.List;

import de.bsd.hawkularFxReports.ValueKeeper;
import de.bsd.hawkularFxReports.model.HawkResource;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * Report about URL resources in Hawkular
 *
 * @author Heiko W. Rupp
 */
public class UrlResourceDetailBuilder extends AbstractDetailBuilder {



    public UrlResourceDetailBuilder() {
        super("URL Resources");
    }

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {

        report.columns(
                Columns.column("Name", "url", String.class).setTitleStyle(bold),
                Columns.column("Server", "server", String.class)
                        .setTitleStyle(bold)
                        .setStretchWithOverflow(true),
                Columns.emptyColumn().setFixedWidth(10),
                Columns.column("IP", "ip", String.class)
                        .setTitleStyle(bold)
//                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                )
                ;
        if (ValueKeeper.getInstance().isShowCharts()) {
            // We can't use .summary here, as this would be one chart for all resources
            report.detail(cmp.subreport(new HiLoChart("Duration", ".status.duration")));
        }

        return report;
    }

    public static class UrlResourceDatasource extends AbstractSimpleExpression<JRDataSource> {
        private List<HawkResource> resources;

        public UrlResourceDatasource(List<HawkResource> resources) {
            this.resources = resources;
        }


        @Override
        public JRDataSource evaluate(ReportParameters reportParameters) {

            DRDataSource source  = new DRDataSource("url","server","ip","id");

            for (HawkResource resource : resources) {
                Object[] row = new Object[4];
                row[0] = resource.getProperties().get("url");
                row[1] = resource.getProperties().get("trait-powered-by");
                row[2] = resource.getProperties().get("trait-remote-address");
                row[3] = resource.getId(); // Id of the metric to display for the chart

                source.add(row);
            }

            return source;

        }
    }

}
