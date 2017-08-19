package com.openlocate.android.config;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ConfigurationTest {
    private String url = "url";
    private HashMap<String, String> headers = new HashMap<>();
    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration(url, headers);
    }

    @Test
    public void getUrl() {
        assertThat(configuration.getUrl(), is(url));
    }

    @Test
    public void getHeaders() {
        assertThat(configuration.getHeaders(), is(headers));
    }

    @Test
    public void isValid() {
        assertThat(configuration.isValid(), is(true));
    }

    @Test
    public void isValidNullValuesFalse() {
        Configuration badConfiguration = new Configuration(null, null);
        assertThat(badConfiguration.isValid(), is(false));
    }

    @Test
    public void testConfigurationBuilder() {
        Configuration builderConfiguration = new Configuration.ConfigurationBuilder()
                .setUrl(url)
                .setHeaders(headers)
                .createConfiguration();

        assertThat(builderConfiguration.isValid(), is(true));
        assertThat(builderConfiguration.getUrl(), is(url));
        assertThat(builderConfiguration.getHeaders(), is(headers));
    }
}
