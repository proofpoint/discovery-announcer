package com.proofpoint.discovery.announcer;

import com.google.common.collect.ImmutableMap;
import com.proofpoint.units.Duration;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.proofpoint.configuration.testing.ConfigAssertions.assertFullMapping;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertLegacyEquivalence;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static com.proofpoint.configuration.testing.ConfigAssertions.recordDefaults;

public class TestServiceConfig
{
    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(recordDefaults(ServiceConfig.class)
                .setCheckUri(null)
                .setCheckInterval(new Duration(10, TimeUnit.SECONDS))
                .setProperties(null));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put("check.uri", "http://localhost:1234/foo")
                .put("check.interval", "1m")
                .put("property.foo", "bar")
                .build();

        ServiceConfig expected = new ServiceConfig()
                .setCheckUri(URI.create("http://localhost:1234/foo"))
                .setCheckInterval(new Duration(1, TimeUnit.MINUTES))
                .setProperties(ImmutableMap.of("foo", "bar"));

        assertFullMapping(properties, expected);
    }

    @Test
    public void testLegacyProperties()
    {
        Map<String, String> currentProperties = new ImmutableMap.Builder<String, String>()
                .put("check.uri", "http://localhost:1234/foo")
                .build();

        assertLegacyEquivalence(ServiceConfig.class, currentProperties);
    }
}
