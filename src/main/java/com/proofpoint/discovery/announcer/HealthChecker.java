package com.proofpoint.discovery.announcer;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import com.proofpoint.discovery.client.announce.Announcer;
import com.proofpoint.discovery.client.announce.ServiceAnnouncement;
import com.proofpoint.discovery.client.announce.ServiceAnnouncement.ServiceAnnouncementBuilder;
import com.proofpoint.http.client.HttpClient;
import com.proofpoint.http.client.StatusResponseHandler.StatusResponse;
import com.proofpoint.log.Logger;
import com.proofpoint.node.NodeInfo;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.proofpoint.discovery.client.announce.ServiceAnnouncement.serviceAnnouncement;
import static com.proofpoint.http.client.HttpUriBuilder.uriBuilder;
import static com.proofpoint.http.client.Request.Builder.prepareGet;
import static com.proofpoint.http.client.StatusResponseHandler.createStatusResponseHandler;

public class HealthChecker
    implements Runnable
{
    private static final Logger log = Logger.get(HealthChecker.class);
    private static final int NUM_FAILURES_TO_DROP_ANNOUNCEMENT = 2;

    private final String serviceType;
    private final URI checkUri;
    private final Map<String, String> properties;
    private final HttpClient httpClient;
    private final Announcer announcer;
    private final NodeInfo nodeInfo;

    private final AtomicReference<UUID> announcementUuid = new AtomicReference<>();
    private final AtomicInteger failureCount = new AtomicInteger(NUM_FAILURES_TO_DROP_ANNOUNCEMENT);

    public HealthChecker(String serviceType, URI checkUri, @Nullable Map<String, String> properties, HttpClient httpClient, Announcer announcer, NodeInfo nodeInfo)
    {
        this.serviceType = checkNotNull(serviceType, "serviceType is null");
        this.checkUri = checkNotNull(checkUri, "checkUri is null");
        if (properties == null) {
            this.properties = null;
        }
        else {
            this.properties = ImmutableMap.copyOf(properties);
        }
        this.httpClient = checkNotNull(httpClient, "httpClient is null");
        this.announcer = checkNotNull(announcer, "announcer is null");
        this.nodeInfo = checkNotNull(nodeInfo, "nodeInfo is null");
    }

    @Override
    public void run()
    {
        boolean failed;
        try {
            StatusResponse response = httpClient.execute(prepareGet().setUri(checkUri).build(), createStatusResponseHandler());
            failed = response.getStatusCode() >= 300;
        }
        catch (RuntimeException ignored) {
            failed = true;
        }

        if (failed && failureCount.incrementAndGet() == NUM_FAILURES_TO_DROP_ANNOUNCEMENT) {
            announcer.removeServiceAnnouncement(announcementUuid.getAndSet(null));
            log.warn("Revoking announcement of %s", serviceType);
        }
        else if (!failed && announcementUuid.get() == null) {
            failureCount.set(0);
            ServiceAnnouncementBuilder announcementBuilder = serviceAnnouncement(serviceType);

            if (properties != null) {
                announcementBuilder.addProperties(properties);
            }
            else if (checkUri.getScheme().equals("https")) {
                announcementBuilder.addProperty("https",
                        uriBuilder()
                                .scheme("https")
                                .host(nodeInfo.getInternalHostname())
                                .port(checkUri.getPort())
                                .build()
                                .toString()
                );
            }
            else {
                announcementBuilder.addProperty("http",
                        uriBuilder()
                                .scheme("http")
                                .host(InetAddresses.toUriString(nodeInfo.getInternalIp()))
                                .port(checkUri.getPort())
                                .build()
                                .toString()
                );
            }
            ServiceAnnouncement announcement = announcementBuilder.build();
            announcer.addServiceAnnouncement(announcement);
            announcementUuid.set(announcement.getId());
            log.info("Announcing %s", serviceType);
        }
    }
}
