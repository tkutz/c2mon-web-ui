/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.configviewer.statistics;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.statistics.generator.charts.WebChart;
import cern.c2mon.web.configviewer.service.ProcessService;

/**
 * This class acts as a service to provide statistics about the C2MON server and
 * running DAQ processes from various sources.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class StatisticsService {

  /**
   * Reference to the {@link StatisticsMapper} db access bean.
   */
  @Autowired
  private StatisticsMapper mapper;

  /**
   * Reference to the {@link ProcessService} bean.
   */
  @Autowired
  private ProcessService processService;

  /**
   * A list of pre-configured chart objects generated by the
   * c2mon-statistics-generator module.
   */
  private List<WebChart> charts = new ArrayList<>();

  /**
   * Retrieve a pre-configured chart object.
   *
   * @param chartId the id of the chart
   *
   * @return the {@link WebChart} instance corresponding to the given id, or
   *         null if the chart was not found.
   */
  public WebChart getChart(String chartId) {
    for (WebChart chart : charts) {
      if (chart.getChartId().equals(chartId)) {
        return chart;
      }
    }

    return null;
  }

  /**
   * Set the list of pre-configured chart objects generated by the
   * c2mon-statistics-generator module.
   *
   * @param charts the charts to set
   */
  public void setCharts(List<WebChart> charts) {
    this.charts = charts;
  }

  /**
   * Retrieve a list of {@link SupervisionEvent} objects from the STL for a
   * given process for a given year.
   *
   * @param name the process name
   * @param year the year of events to retrieve
   *
   * @return the list of {@link SupervisionEvent} objects
   *
   * @throws Exception if an invalid year was given, or if an error occurs
   *           getting the process id
   */
  public List<SupervisionEvent> getSupervisionEventsForYear(String name, Integer year) throws Exception {
    Long id = processService.getProcessConfiguration(name).getProcessID();

    // Generate dates for the first and last days of the given year.
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date from = format.parse(String.valueOf(year) + "-01-01");
    Date to = format.parse(String.valueOf(year) + "-12-31");
    return mapper.getSupervisionEvents(id, new Timestamp(from.getTime()), new Timestamp(to.getTime()));
  }

  /**
   * Retrieve a list of {@link ServerLifecycleEvent} objects from the STL for a
   * given year.
   *
   * @param year the year of events to retrieve
   *
   * @return the list of {@link ServerLifecycleEvent} objects
   *
   * @throws ParseException if an invalid year was given
   */
  public List<ServerLifecycleEvent> getServerLifecycleEventsForYear(Integer year) throws ParseException {
    // Generate dates for the first and last days of the given year.
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date from = format.parse(String.valueOf(year) + "-01-01");
    Date to = format.parse(String.valueOf(year) + "-12-31");
    return mapper.getServerLifecycleEvents(new Timestamp(from.getTime()), new Timestamp(to.getTime()));
  }

  /**
   * Retrieve the list of names of all currently configured processes.
   *
   * @return the list of process names
   */
  public Collection<String> getProcessNames() {
    return processService.getProcessNames();
  }

  /**
   * Since the server names in the STL are not completely consistent (the names
   * were changed a couple of times), this method normalises them so that they
   * are all the same.
   *
   * @param events the list of events to normalise
   *
   * @return the normalised list
   */
  public List<ServerLifecycleEvent> normaliseServerNames(List<ServerLifecycleEvent> events) {
    for (ServerLifecycleEvent event : events) {
      if (event.getServerName().equals("C2MON-primary") || event.getServerName().equals("C2MON-PRO1") || event.getServerName().equals("C2MON-TIM-PRO1")) {
        event.setServerName("C2MON-TIM-PRO1");
      }
      if (event.getServerName().equals("C2MON-second") || event.getServerName().equals("C2MON-PRO2") || event.getServerName().equals("C2MON-TIM-PRO2")) {
        event.setServerName("C2MON-TIM-PRO2");
      }
    }

    return events;
  }

  /**
   * Retrieve the current tag statistics from the server.
   *
   * @return a {@link TagStatisticsResponse} object
   */
  public TagStatisticsResponse getTagStatistics() {
    return C2monServiceGateway.getTagManager().getTagStatistics();
  }
}