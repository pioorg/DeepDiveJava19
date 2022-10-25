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
package org.przybyl.ddj19.concurrency.virtThreads;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;

/**
 * Created by Piotr Przybył (piotr@przybyl.org)
 */
public class VirtThreads {

    public static void main(String[] args) throws InterruptedException {
        Thread.ofVirtual().start(() -> {
            Thread ct = Thread.currentThread();
            System.out.printf("Is current thread virtual? %b%n", ct.isVirtual());
            System.out.printf("Current thread's ID: %d%n", ct.threadId());
            System.out.printf("Current thread's deprecated ID: %d%n", ct.getId());
            System.out.printf("Is current thread a daemon? %b%n", ct.isDaemon());
            System.out.println();

        }).join();

        if (args.length == 0) {
            return;
        }

        class Greeter {
            public synchronized void getGreeting(HttpClient client, HttpRequest request, int attempt) {
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.print("[Attempt: " + attempt + ", length: " + response.body().length() + "] ");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var uri = URI.create(args[0]);
        var request = HttpRequest.newBuilder(uri).GET().build();

        Thread last = null;
        for (int i = 0; i < 20; i++) {
            final var c = i;
            var g = new Greeter();
            last = Thread.startVirtualThread(() -> g.getGreeting(client, request, c));
            System.out.print(("[ Created " + c + "] "));
        }
        System.out.println();
        last.join();
    }

}

