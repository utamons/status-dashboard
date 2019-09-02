package com.corn.util;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang.math.RandomUtils.nextLong;

/**
 * @author Oleg Zaidullin
 */
@SuppressWarnings("unused")
public class Utils {

    private Utils() {/*the class isn't meant to be instantiated.*/}

    public static boolean isEmpty(Collection object) {
        return object == null || object.isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static <T> Optional<T> last(List<T> list) {
        if (isEmpty(list))
            return Optional.empty();
        else
            return Optional.of(list.get(list.size()-1));
    }

    public static Instant randomInstant() {
        return Instant.ofEpochMilli(nextLong());
    }

    public static boolean randomBoolean() {
        return nextInt() % 2 == 0;
    }
}
