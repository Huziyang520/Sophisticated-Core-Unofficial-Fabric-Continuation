package net.p3pp3rf1y.sophisticatedcore.util;

public class MixinHelper {

    public static <T> T cast(Object obj) {
        //noinspection unchecked
        return (T) obj;
    }
}
