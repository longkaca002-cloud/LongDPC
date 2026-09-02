import com.longkaca.ocr.EmailExtractor;

public final class EmailExtractorSelfTest {
    private static void check(String expected, String input) {
        String actual = EmailExtractor.firstEmail(input);
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected [" + expected + "] but got [" + actual + "] for [" + input + "]");
        }
    }

    public static void main(String[] args) {
        check("long.kaca+01@gmail.com", "Mail: long.kaca+01@gmail.com end");
        check("long_01@gmail.com", "long_01 @ gmail . com");
        check("abc-123@yahoo.co.jp", "abc-123@yahoo.co.jp");
        check("", "long@gmail");
        check("", "không có email");
        System.out.println("PASS Long OCR email extraction: 5 cases");
    }
}
