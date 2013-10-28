package com.proofpoint.discovery.announcer;

import com.google.common.collect.ImmutableMap;
import com.proofpoint.units.Duration;
import com.proofpoint.units.MinDuration;
import org.testng.annotations.Test;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.proofpoint.configuration.testing.ConfigAssertions.assertFullMapping;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertLegacyEquivalence;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static com.proofpoint.configuration.testing.ConfigAssertions.recordDefaults;
import static com.proofpoint.testing.ValidationAssertions.assertFailsValidation;

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

    @Test
    public void testMissingCheckUri()
    {
        assertFailsValidation(new ServiceConfig(), "checkUri", "may not be null", NotNull.class);
    }

    @Test
    public void testTooShortInterval()
    {
        assertFailsValidation(new ServiceConfig()
                .setCheckUri(URI.create("http://localhost"))
                .setCheckInterval(new Duration(.999, TimeUnit.SECONDS)),
                "checkInterval", "{com.proofpoint.units.MinDuration.message}", MinDuration.class);
    }
}
