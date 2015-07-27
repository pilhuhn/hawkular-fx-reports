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
import static net.sf.dynamicreports.report.builder.DynamicReports.report;

import java.util.List;

import de.bsd.hawkularFxReports.model.HawkResource;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * TODO document me
 *
 * @author Heiko W. Rupp
 */
public class ServletResourceDetailBuilder extends AbstractSimpleExpression<JasperReportBuilder> {


    private final List<HawkResource> resources;

    public ServletResourceDetailBuilder(List<HawkResource> resources) {

        this.resources = resources;
    }

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {
        JasperReportBuilder report = report();

        report.title(cmp.text("Servlet Resources")
                .setStyle(Styles.style().bold()))
                .setDataSource(resources)
                .addNoData(cmp.text("No data available"))
                .columns(
                        Columns.column("Name", "name", String.class)
                );

        return report;
    }

    public static class ServletResourceDatasource extends AbstractSimpleExpression<JRDataSource> {
        private List<HawkResource> resources;

        public ServletResourceDatasource(List<HawkResource> resources) {
            this.resources = resources;
        }


        @Override
        public JRDataSource evaluate(ReportParameters reportParameters) {

            DRDataSource source  = new DRDataSource("name");

            for (HawkResource resource : resources) {
                Object[] row = new Object[1];
                row[0] = resource.getProperties().get("name");

                source.add(row);
            }

            return source;

        }
    }
}
