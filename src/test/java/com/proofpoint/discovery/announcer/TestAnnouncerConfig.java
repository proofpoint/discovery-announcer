package com.proofpoint.discovery.announcer;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;

import static com.proofpoint.configuration.testing.ConfigAssertions.assertFullMapping;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertLegacyEquivalence;
import static com.proofpoint.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static com.proofpoint.configuration.testing.ConfigAssertions.recordDefaults;

public class TestAnnouncerConfig
{
    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(recordDefaults(AnnouncerConfig.class)
                .setServices(null));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put("service.foo.check.uri", "http://localhost:1234/foo")
                .build();

        AnnouncerConfig expected = new AnnouncerConfig()
                .setServices(
                        ImmutableMap.of("foo",
                                new ServiceConfig()
                                        .setCheckUri(URI.create("http://localhost:1234/foo"))));


        assertFullMapping(properties, expected);
    }

    @Test
    public void testLegacyProperties()
    {
        Map<String, String> currentProperties = new ImmutableMap.Builder<String, String>()
                .put("service.foo.check.uri", "http://localhost:1234/foo")
                .build();

        assertLegacyEquivalence(AnnouncerConfig.class, currentProperties);
    }
}
