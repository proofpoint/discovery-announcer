package com.proofpoint.discovery.announcer;

import com.proofpoint.configuration.Config;
import com.proofpoint.configuration.ConfigMap;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class AnnouncerConfig
{
    private Map<String, ServiceConfig> services = null;

    @NotNull
    public Map<String, ServiceConfig> getServices()
    {
        return services;
    }

    @Config("service")
    @ConfigMap(ServiceConfig.class)
    public AnnouncerConfig setServices(Map<String, ServiceConfig> services)
    {
        this.services = services;
        return this;
    }
}