/*
 * Copyright 2014 Volkert de Buisonj&eacute;
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
 * 
 * NOTE: proxy-vole has a different license. See the license.txt file bundled
 *       with the proxy-vole dependency in the bundled-repo folder.
 */
package com.buisonje;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.TrackableBase;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.util.Logger;

/**
 * Goal which automatically detects and configures the current system proxy
 * server.
 * 
 * @author Volkert de Buisonj&eacute;
 *
 */
@Mojo(name = "detectProxy", defaultPhase = LifecyclePhase.VALIDATE, aggregator = false)
public class AutoProxyMojo extends AbstractMojo {

    private static final String BASIC_HTTP_URL_TO_TRY = "http://www.ams-ix.net";
    private static final String PROXY_DETECTION_TIME_KEY = Date.class.getName();
    private static final String PROXY_SELECTOR_KEY = ProxySelector.class
            .getName();

    /**
     * The Maven Settings.
     */
    @Parameter(defaultValue = "${settings}", readonly = false)
    private Settings settings;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    Map<String, Object> sessionMap = null;

    @Override
    public void execute() throws MojoExecutionException {

        final Log mojoLog = getLog();

        ProxySelector myProxySelector = null;

        if (isProxyDetectionAlreadyAttempted()) {
            mojoLog.info("Proxy server detection already attempted in this session. Reapplying result(s)...");
            myProxySelector = (ProxySelector) session.getSystemProperties()
                    .get(PROXY_SELECTOR_KEY);
        } else {
            showMavenProxySettings(mojoLog);
            // showCurrentSystemWideProxiesInfo(mojoLog);
            mojoLog.info("Autodetecting system proxy server(s)...");
            myProxySelector = autodetectProxySettings(mojoLog);
            session.getSystemProperties().put(PROXY_DETECTION_TIME_KEY,
                    new Date());
        }

        if (myProxySelector == null) {
            mojoLog.warn("Could not detect proxy server(s) automatically. Falling back to the initial settings...");
        } else {

            session.getSystemProperties().put(PROXY_SELECTOR_KEY,
                    myProxySelector);

            /*
             * NOTE: It seems that Maven either ignores the system-wide
             * (default) {@link ProxySelector} entirely, or doesn't update it
             * with the initial proxy settings until it actually starts
             * downloading something. Either way, replacing the default {@link
             * ProxySelector} might therefore be pointless here. But it
             * shouldn't hurt, either.
             */
            ProxySelector.setDefault(myProxySelector);

            mojoLog.info("Detected available proxy server(s) for HTTP connections:");
            List<Proxy> detectedAvailableProxies = getAvailableProxies(myProxySelector);
            showAvailableProxies(mojoLog, detectedAvailableProxies);
            Proxy firstDetectedAvailableProxy = detectedAvailableProxies.get(0);
            mojoLog.info(String
                    .format("Overriding Maven proxy settings with the first detected available proxy (%s)...",
                            firstDetectedAvailableProxy.address().toString()));
            overrideMavenProxySettings(firstDetectedAvailableProxy, mojoLog);
        }
    }

    private boolean isProxyDetectionAlreadyAttempted() {
        return session.getSystemProperties().get(PROXY_DETECTION_TIME_KEY) != null;
    }

    /**
     * Override the Maven proxy settings with the first available (actual)
     * Proxy.
     * 
     * @param overridingProxy
     *            The {@link Proxy} to use instead of whatever is configured in
     *            the initial Maven settings (settings.xml).
     */
    private void overrideMavenProxySettings(Proxy overridingProxy, Log mojoLog) {

        org.apache.maven.settings.Proxy mavenProxy = settings.getActiveProxy();

        if (mavenProxy == null) {
            /*
             * Create a new Maven proxy setting instance that will end up
             * containing the detected overriding proxy server configuration.
             */
            mavenProxy = new org.apache.maven.settings.Proxy();
        }

        if (mavenProxy.getSourceLevel() == null) {
            /*
             * User level should be enough for overriding the proxy for the
             * Maven goals/commands currently being executed.
             */
            mavenProxy.setSourceLevel(TrackableBase.USER_LEVEL);
        }

        if (Proxy.NO_PROXY.equals(overridingProxy)) {
            mojoLog.info("The detected proxy configuration is a direct connection. Overriding active proxy configured in Maven settings...");
            /*
             * There can be only one active proxy in the Maven settings at a
             * time, so if the currently active one is set to inactive, that
             * should imply a direct connection.
             */
            mavenProxy.setActive(false);
        } else {

            final String nonProxyHosts = mavenProxy.getNonProxyHosts();
            if (nonProxyHosts != null && !nonProxyHosts.isEmpty()) {
                mojoLog.info("Non-proxy hosts appear to have been specified in settings.xml. Leaving those untouched.");
            }

            mavenProxy.setActive(true);
            mavenProxy.setProtocol(overridingProxy.type().name().toLowerCase());

            SocketAddress overridingProxyAddress = overridingProxy.address();
            if (overridingProxyAddress instanceof InetSocketAddress) {
                InetSocketAddress overridingInetSocketAddress = (InetSocketAddress) overridingProxyAddress;
                mavenProxy.setHost(overridingInetSocketAddress.getHostName());
                mavenProxy.setPort(overridingInetSocketAddress.getPort());
            } else {
                /*
                 * Unlikely that the {@link SocketAddress} of the detected
                 * {@link java.net.Proxy} would be anything other than its
                 * subclass {@link InetSocketAddress}, but just in case, handle
                 * it somehow:
                 */
                mavenProxy.setHost(overridingProxyAddress.toString());
                mavenProxy.setPort(0);
            }

        }

    }

    /**
     * @param mojoLog
     * @return
     */
    private ProxySelector autodetectProxySettings(final Log mojoLog) {
        // Let proxy-vole log to the Maven plugin logger.
        Logger.setBackend(new MojoLogBackEnd(mojoLog));

        // Let Proxy Vole try to detect the system proxy settings automatically.
        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

        // Set the default proxy server as determined by Proxy Vole:
        ProxySelector myProxySelector = proxySearch.getProxySelector();
        return myProxySelector;
    }

    /**
     * @param proxySelector
     * @return
     */
    private List<Proxy> getAvailableProxies(ProxySelector proxySelector) {
        return proxySelector.select(getBasicHttpURIForProxySelection());
    }

    /**
     * @param mojoLog
     * @param availableProxies
     */
    private void showAvailableProxies(final Log mojoLog,
            List<Proxy> availableProxies) {
        for (int i = 0; i < availableProxies.size(); i++) {
            final Proxy proxy = availableProxies.get(i);
            mojoLog.info(String.format("Available proxy %d:", i + 1));
            mojoLog.info("  * Proxy type: " + proxy.type().name());
            String proxyAddress = proxy.type() == Type.DIRECT ? "n/a (Direct connection)"
                    : proxy.address().toString();
            mojoLog.info("  * Proxy address: " + proxyAddress);
        }
    }

    private void showMavenProxySettings(final Log mojoLog) {
        final org.apache.maven.settings.Proxy manuallyConfiguredActiveMavenProxy = settings
                .getActiveProxy();

        if (manuallyConfiguredActiveMavenProxy != null) {
            mojoLog.info("Manually configured active proxy found in settings.xml. Details:");
            mojoLog.info("  * Hostname: "
                    + manuallyConfiguredActiveMavenProxy.getHost());
            mojoLog.info("  * Port    : "
                    + manuallyConfiguredActiveMavenProxy.getPort());
            mojoLog.info("  * Protocol: "
                    + manuallyConfiguredActiveMavenProxy.getProtocol());
        } else {
            mojoLog.info("No currently active proxy found in settings.xml.");
        }
    }

    private boolean isDirectConnection(List<Proxy> availableProxies) {
        return (availableProxies.size() == 1 && availableProxies.get(0).type() == Type.DIRECT);
    }

    private static final URI getBasicHttpURIForProxySelection() {
        URI validHttpUri;
        try {
            validHttpUri = new URI(BASIC_HTTP_URL_TO_TRY);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("The String "
                    + BASIC_HTTP_URL_TO_TRY + " is not considered a valid URI.");
        }
        return validHttpUri;
    }

}
