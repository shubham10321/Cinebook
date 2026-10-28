package cinema.util;

import java.util.Random;

/**
 * OTP utility — DEMO MODE.
 * In a real app this would send an email.  Here the OTP is shown on screen.
 */
public class OtpUtil {

    private static final int OTP_LENGTH = 6;
    private static final Random RNG = new Random();

    /** Generate a random 6-digit OTP string. */
    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(RNG.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * "Send" the OTP — in demo mode this just returns the OTP so the caller
     * can display it.  Replace this body with JavaMail code in production.
     *
     * @param email      recipient email address (displayed in the dialog)
     * @param otp        the OTP code
     * @return the same OTP (demo passthrough)
     */
    public static String send(String email, String otp) {
        // DEMO: OTP is shown on screen by the UI layer.
        System.out.println("[DEMO OTP] To: " + email + "  OTP: " + otp);
        return otp;
    }
}
