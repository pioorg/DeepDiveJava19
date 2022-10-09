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
package org.przybyl.ddj19.varia;


import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

// run as e.g.
// java org/przybyl/ddj19/varia/Varia.java
public class Varia {
    public static void main(String[] args) {

        var now = ZonedDateTime.now();
        Stream.of("pl", "en", "nl", "fr", "de").
            map(Locale::of)
            .map(loc -> new DateTimeFormatterBuilder()
                .appendLocalized("yMMM")
                .toFormatter(loc))
            .map(f -> f.format(now))
            .forEach(System.out::println);

        var newSystemProps = HashMap.newHashMap(2);
        newSystemProps.put("stdout.encoding", System.getProperty("stdout.encoding"));
        newSystemProps.put("stderr.encoding", System.getProperty("stderr.encoding"));
        newSystemProps.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).forEach(System.out::println);
    }

}
