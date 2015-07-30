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
import java.util.Map;

import de.bsd.hawkularFxReports.model.HawkResource;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * Report about WildFly servers in Hawkular
 *
 * @author Heiko W. Rupp
 */
public class WFResourceDetailsBuilder extends AbstractDetailBuilder {


    public WFResourceDetailsBuilder() {
        super("WildFly Servers");
    }

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {

        report.columns(Columns.column("Name", "name", String.class).setTitleStyle(bold),
                       Columns.column("Host", "hostname", String.class).setTitleStyle(bold),
                       Columns.column("Version", "version", String.class).setTitleStyle(bold)
                )
                // We can't use .summary here, as this would be one chart for all resources
                .detail(cmp.subreport(new HiLoChart("Heap usage", null)), cmp.verticalGap(15))
        ;

        return report;
    }


    /**
     * Datasource for this report
     *
     * @author Heiko W. Rupp
     */
    public static class WFResourceDatasource extends AbstractSimpleExpression<JRDataSource> {
        private List<HawkResource> resources;

        public WFResourceDatasource(List<HawkResource> resources) {
            this.resources = resources;
        }


        @Override
        public JRDataSource evaluate(ReportParameters reportParameters) {

            DRDataSource source  = new DRDataSource("name","hostname","version","id");

            for (HawkResource resource : resources) {
                Object[] row = new Object[4];
                row[0] = resource.getId();
                List<Map<String,Object>> resourceConfiguration = (List<Map<String,Object>>) resource.getProperties().get
                        ("resourceConfiguration");
                row[1] = find(resourceConfiguration,"Hostname");
                row[2] = find(resourceConfiguration,"Version");

                // http://localhost:8080/hawkular/metrics/gauges/MI~R~%5Bsnert~Local~/%5D~MT~WildFly%20Memory%20Metrics~Heap%20Used/data?buckets=120
                row[3] = "MI~R~[" + resource.getId() + "]~MT~WildFly Memory Metrics~Heap Used";

                source.add(row);
            }

            return source;

        }

        String find(List<Map<String,Object>> config, String prop) {
            for (Map<String,Object> map : config) {
                if (map.get("name").equals(prop)) {
                    return (String) map.get("value");
                }
            }
            return "- not found -";
        }
    }

}
