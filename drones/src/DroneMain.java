import java.util.Locale;

public class DroneMain {
	private static final String logFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s [%2$s] %5$s%6$s%n";
	private static final Locale locale = new Locale("en", "US");

	public static void main(String[] args) {
	  Locale.setDefault(locale);
		System.setProperty("java.util.logging.SimpleFormatter.format", logFormat);
		new ControlFrame();
	}
}