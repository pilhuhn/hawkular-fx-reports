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

import java.util.Arrays;
import java.util.List;

import de.bsd.hawkularFxReports.details.DeploymentResourceDetailBuilder;
import de.bsd.hawkularFxReports.details.ServletResourceDetailBuilder;
import de.bsd.hawkularFxReports.details.UrlResourceDetailBuilder;
import de.bsd.hawkularFxReports.details.WFResourceDetailsBuilder;
import de.bsd.hawkularFxReports.model.HawkResource;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;

/**
 * TODO document me
 *
 * @author Heiko W. Rupp
 */
public class ResourceDetailFactory {

    private static List<String> types;

    static {

        types = Arrays.asList("URL","WildFly Server","Deployment");//,"Servlet");
    }

    public static boolean supports(String type) {
        return types.contains(type);
    }

    public static SubreportBuilder getForType(String type, List<HawkResource> resources) {

        SubreportBuilder builder = null;

        switch (type) {
            case "URL":

                builder = cmp.subreport(new UrlResourceDetailBuilder(resources))
                        .setDataSource(new UrlResourceDetailBuilder.UrlResourceDatasource(resources));

                break;
            case "WildFly Server":
                builder = cmp.subreport(new WFResourceDetailsBuilder(resources))
                        .setDataSource(new WFResourceDetailsBuilder.WFResourceDatasource(resources));
                break;
            case "Servlet":
                builder = cmp.subreport(new ServletResourceDetailBuilder(resources))
                        .setDataSource(new ServletResourceDetailBuilder.ServletResourceDatasource(resources))
                ;
                break;

            case "Deployment":
                builder = cmp.subreport(new DeploymentResourceDetailBuilder(resources))
                        .setDataSource(new DeploymentResourceDetailBuilder.DeploymentResourceDatasource(resources))
                ;
                break;


            default:
                System.err.println("Type " + type + " not yet supported.");
                break;
        }

        return builder;
    }
}
