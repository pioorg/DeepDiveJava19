/*
 *  Copyright (C) 2022 Piotr Przybył
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.przybyl.ddj19.concurrency;

import eu.rekawek.toxiproxy.model.*;
import org.hamcrest.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.*;
import org.testcontainers.utility.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.stream.*;

// Disabled, because not passing. Can you fix it?
@Disabled
class VirtThreadsPinTest {

    private ByteArrayOutputStream testStream;

    @BeforeEach
    void setUp() {
        testStream = new ByteArrayOutputStream();
    }

    @Test
    public void shouldPin() throws IOException, InterruptedException, URISyntaxException {

        // first let's make sure we're really tracing the pinned virtual threads
        MatcherAssert.assertThat(System.getProperty("jdk.tracePinnedThreads"), is(notNullValue()));

        var network = Network.newNetwork();
        var index = MountableFile.forClasspathResource("index.html");

        try (
            var nginx = new NginxContainer<>("nginx:1.23.1")
                .withCopyFileToContainer(index, "/usr/share/nginx/html/index.html")
                .waitingFor(new HttpWaitStrategy())
                .withNetwork(network);
            var toxiProxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                .withNetwork(network)
        ) {

            Stream.of(nginx, toxiProxy).parallel().forEach(GenericContainer::start);

            var proxy = toxiProxy.getProxy(nginx, 80);
            proxy.toxics().latency("latency", ToxicDirection.DOWNSTREAM, 1_000);

            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            var uri = URI.create(String.format("http://%s:%d/", proxy.getContainerIpAddress(), proxy.getProxyPort()));
            var request = HttpRequest.newBuilder(uri).GET().build();

            var stdOut = System.out;
            System.setOut(new PrintStream(testStream));
            Thread last = null;
            for (int i = 0; i < 20; i++) {
                final var c = i;
                var g = new Greeter();
                last = Thread.startVirtualThread(() -> g.getGreeting(client, request, c));
                System.out.print(("[ Created " + c + "] "));
            }
            System.out.println();
            last.join();
            System.setOut(stdOut);

            MatcherAssert.assertThat(testStream.toString(), not(containsString("onPinned")));
        }

    }

    static class Greeter {
        public synchronized void getGreeting(HttpClient client, HttpRequest request, int attempt) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.print("[Attempt: " + attempt + ", length: " + response.body().length() + "] ");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}