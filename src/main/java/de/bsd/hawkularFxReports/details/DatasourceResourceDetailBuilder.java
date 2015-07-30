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

import java.util.List;

import de.bsd.hawkularFxReports.model.HawkResource;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * Report about servlets in Hawkular
 *
 * @author Heiko W. Rupp
 */
public class DatasourceResourceDetailBuilder extends AbstractDetailBuilder {



    public DatasourceResourceDetailBuilder() {
        super("Datasources");
    }

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {

        report.columns(
                Columns.column("Name", "name", String.class).setTitleStyle(bold),
                Columns.column("Server", "server", String.class).setTitleStyle(bold)
//                Columns.column("Available Connections", "avail_connections", Integer.class).setTitleStyle(bold)
                )
//                .detail(cmp.subreport(new HiLoChart("Used Connections", null)), cmp.verticalGap(15))
        ;

        return report;
    }

    public static class DatasourceResourceDatasource extends AbstractSimpleExpression<JRDataSource> {
        private List<HawkResource> resources;

        public DatasourceResourceDatasource(List<HawkResource> resources) {
            this.resources = resources;
        }


        @Override
        public JRDataSource evaluate(ReportParameters reportParameters) {

            DRDataSource source  = new DRDataSource("name","server","id");

            for (HawkResource resource : resources) {
                Object[] row = new Object[3];
                String name = (String) resource.getProperties().get("name");
                name = name.substring(12);
                name = name.substring(0,name.length()-1);
                row[0] = name;
                row[1] = resource.getId().substring(0,resource.getId().indexOf("/")-1);

//                http://localhost:8080/hawkular/metrics/gauges/MI~R~[snert~Local~/subsystem=datasources/data-source
//                // =HawkularDS]~MT~~/data?buckets=60&end=1438257339560&start=1438253739560
//                //MI~R~[snert~Local~/subsystem=datasources/data-source=KeycloakDS]~MT~Datasource Pool Metrics~In Use Count
                row[2] = "MI~R~[" + resource.getId() + "]~MT~Datasource Pool Metrics~In Use Count";

                source.add(row);
            }

            return source;

        }
    }
}
