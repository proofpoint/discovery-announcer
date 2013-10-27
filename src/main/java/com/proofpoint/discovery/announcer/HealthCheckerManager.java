package com.proofpoint.discovery.announcer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.proofpoint.discovery.client.announce.Announcer;
import com.proofpoint.http.client.HttpClient;
import com.proofpoint.node.NodeInfo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class HealthCheckerManager
{
    private final AnnouncerConfig announcerConfig;
    private final HttpClient httpClient;
    private final Announcer announcer;
    private final NodeInfo nodeInfo;

    private final ScheduledExecutorService executorService;

    @Inject
    public HealthCheckerManager(AnnouncerConfig announcerConfig, @HealthCheck HttpClient httpClient, Announcer announcer, NodeInfo nodeInfo)
    {
        this.announcerConfig = announcerConfig;
        this.httpClient = httpClient;
        this.announcer = announcer;
        this.nodeInfo = nodeInfo;
        executorService = newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("health-checker-%s").setDaemon(true).build());
    }

    @PostConstruct
    public void start()
    {
        for (Entry<String, ServiceConfig> entry : announcerConfig.getServices().entrySet()) {
            ServiceConfig serviceConfig = entry.getValue();
            Runnable checker = new HealthChecker(entry.getKey(), serviceConfig.getCheckUri(), serviceConfig.getProperties(), httpClient, announcer, nodeInfo);
            executorService.scheduleWithFixedDelay(checker, 0, serviceConfig.getCheckInterval().roundTo(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        }
    }

    @PreDestroy
    public void stop()
    {
        executorService.shutdownNow();
    }
}
