import com.longkaca.ocr.EmailExtractor;
import java.util.Arrays;
import java.util.List;

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
        if (EmailExtractor.allEmails("a@gmail.com\nb@yahoo.co.jp\na@gmail.com").size() != 2) {
            throw new AssertionError("Expected two unique emails in stable order");
        }
        List<String> wrapped = EmailExtractor.usEmailsFromLines(Arrays.asList(
                "FirstUser@some-domain", ".us|password", "SecondUser", "@other.mail.us|password"));
        if (!wrapped.equals(Arrays.asList("FirstUser@some-domain.us", "SecondUser@other.mail.us"))) {
            throw new AssertionError("Wrapped .us extraction failed: " + wrapped);
        }
        List<String> generic = EmailExtractor.usEmailsFromLines(Arrays.asList(
                "ThirdUser@different.us|password"));
        if (!generic.equals(Arrays.asList("ThirdUser@different.us"))) {
            throw new AssertionError("Generic .us extraction failed: " + generic);
        }
        System.out.println("PASS Long OCR email extraction: 8 cases");
    }
}
