package AutoplayAddon.utils;

public class WaitUtil {
    public static void wait1sec() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ee) {
            ee.printStackTrace();
        }
    }
}
