package mobac.program.download;

public class UserAgent {

	public static final String IE7_XP = "Mozilla/4.0 (compatible; MSIE 7.0; "
			+ "Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1))";

	public static final String IE6_XP = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

	public static final String FF2_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.8.1.17) Gecko/20080829";

	public static final String FF3_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2) Gecko/20100115 Firefox/3.6";

	public static final String OPERA9_XP = "Opera/9.6 (Windows NT 5.1; U; en) Presto/2.1.1";

	public static final String OPERA10_XP = "Opera/9.80 (Windows NT 5.1; U; en) Presto/2.2.15 Version/10.01";

	public static final UserAgent[] USER_AGENTS = new UserAgent[] {
			new UserAgent("Internet Explorer 7 WinXP", IE7_XP),
			new UserAgent("Internet Explorer 6 WinXP", IE6_XP),
			new UserAgent("Firefox 2 WinXP", FF2_XP), // 
			new UserAgent("Firefox 3.5 WinXP", FF3_XP),
			new UserAgent("Opera 9.6 WinXP", OPERA9_XP), // 
			new UserAgent("Opera 10.01 WinXP", OPERA10_XP) };

	private String name;
	private String userAgent;

	protected UserAgent(String name, String userAgent) {
		super();
		this.name = name;
		this.userAgent = userAgent;
	}

	public String getName() {
		return name;
	}

	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public String toString() {
		return name;
	}

}
