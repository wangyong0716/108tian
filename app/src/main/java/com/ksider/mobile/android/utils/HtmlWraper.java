package com.ksider.mobile.android.utils;

public class HtmlWraper {

	public static String getHtmlDoc(String content){
		return  "<!DOCTYPE html><html><head><meta charset='utf8' /><title>108天</title><meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />"
				+
				"<style>html,body,dl,dt,dd,ul,li,table,form{margin:0;padding:0;font:normal 15px/23px arial}i,em{font-style:normal;font-weight:normal}.html{color:#7d7d7d !important;font-size:15px;text-align:justify;word-break:break-all;line-height:23px}.html h1,.html h2,.html h3{position:relative;margin:0 0 8px 0;padding:0 0 0 10px;font-size:15px;font-weight:normal;color:#7d7d7d !important}.html h1:before,.html h2:before,.html h3:before{content:'•';position:absolute;left:0;top:0;width:10px;height:23px;line-height:23px;font-size:18px}.html h4,.html h5,.html h6,.html p,.html div{margin:0 0 8px 0;padding:0;font-weight:normal;color:#7d7d7d !important}.html h4+.pic,.html h5+.pic,.html h6+.pic,.html p+.pic,.html div+.pic{margin-top:15px}.html strong,.html b{font-weight:normal}.html .pic{margin:8px 0;line-height:0;background:#edefee url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJIAAABKBAMAAACiOBD/AAAAMFBMVEX///////////////////////////////////////////////////////////////9Or7hAAAAAD3RSTlMAESIzRFVmd4iZqrvM3e5GKvWZAAAIB0lEQVRYw+2YWXAURRiAe2Y32SAgEwkg+sAm4VAfIJBwqFWyQDiCDwHkUKDKBKEosYQQQMIRopwGkENADoVQhQiIkJSWCRhgsUpZDmF94Q7ZWGUp5NhBOZPdnfb/u6dnZjcbcHnywX7Y7e7p/ub/u/9rlxDWpNT0PgrRm5SSbhmxh+l9ko2hvWBeZlgbVjDHXNxt9fZ9TjFoP6H4wMrB5sOuBQe2r3rDQDtuNGuXzMUTL/gDaXo/acGxav/1/WKrPGz1eb+/6sBc8aYE2qw9MOVfR6lByqr2U6r5z+jjuON+Fcf1uf+G1MFNaVDf2eEwfxrazISSZ4jlFfqC+BtV5yqhHfPRBvyu/OXGKYM0UjVlGil23mITHS+IcWi2fuLFq6bhQU9w0zNT4Xvo9OLlApS0ixok205jZz7qzcBV5+FV9Ht+UnJGenIitK6l1NMbO6kZvQUpy2eSOlULkrYX1LMthl7D/gVe+KpNI9bWvoSWO8NmSL9D1CS9At36rQU4dRXWxZ0AZMWQ9BnwstCosG22ZqT2a6lJkiZD93QaEzPgguOF74Z8fifa7EfKJHVdeMFCsn0C794ER1cK31PADFGrQXA464G0NJK0V7HK+JaPWkioTd1o6CxBosJIN5P5WFuqRJA2WSfiv8DDNawgHgS8gr1JKtW+5KTLuH4mrFoWRgKxw0gJeADnfILUCZinnPw+tXIgwfimU8gUpl03N91kHSeAOsGVbkFCVziFbxoAJI9C4ryWc8oNI3V30zC04zDccnqJIHUG0s9I6uFlwuAN1C9TpH7wrjoXCzkpKYmsjfDSzcm8m5KSzGS6NgZvipNeAtJ3whUbnUSaBLJ4+qQuAi09qKWtuHjVONYW+mgF740vKF6ukIRD11codkM7NMxv2XnCVJOT+11wawGM6vNR1rjq6qqvWTum0uu8d/C8H97iOLg5mRgySa9bZQoASZ4IQjXcAJEqmHfFw536eaNGz4+WYh/vIhbSpEgSszBs9fncamjU9jCZSIkKeZRMRF7M115xPYYkjKxF7YbpYebaCuZk9oOVP2xnDQ3xKOvtqKz8yvlYklAO1GPmJA8dN5hfPVr9bNZ7JnPcYOVxJCkLzrpuW/FxmOeen5iob5pJNe0jPQyIuUeQbBBytHKnhH4eHBXhwFVqZKiLuDvDnoCEHl07hgdoLd+6p0MpPeq7ldYiKUwmsMxnYXwJRYcIiLHBbAO8dEtpuJhWkukt3cHvbjlZQviJGb9wF9FgIr8kIjxYSa3Qg/VXaped8nsgyh59TK9adZlMQ64iLfKgWiB5FDRLjUUhjA3WU5GXQK55X611tUTCc/GYkY6RmEz9I0jdTkAce9kXfgtWksOnK4HRd6/C0p2hnZUEx7SHdPDysBiNFHeYGzPWHKFlCp4Tj3yY8E+bhyIVUjBLeRcNjG6BhDFS+xTcC7NULk/mLENMZhZqWtMJWjsKeCoG+6gkljk9Q9LRpjHavgDD4JxEKRUt00wl0kSVOQ+cZmBMdBJ5FeuAozsugDJXYCnmFu304FTM5tTMwXbI9nilSV4aLpSF1Am3aFhmMJO2YzUV3PYxWDytMwxaBpHYSIIbCcxxRiWxCohXPbnCfSDIqtRq4h1P6PdAeoCxXLNkrySzppOy9OyOdQY/WhGf8nU1pIxFIGCuUW5pFWN7RyEZQulVgGTUeB5hTTZMWbodsRdrZ+c5De00o/aVhn+DqtTvE3Xn5+cpu4Up+vO+WJbVTRFb0Xa1s9vGiBLq3LGrAiuNgFqw/sggoxxfjTXUUXHZSWtYpWhcGAYNLAD1zUOnjZtqPGs/fv78D8xKXsqYDuOxepE5YQeWW2dMI5LfRVTgHRJjQx8A1edYpxaL+BFTk/EC6lZYA5XUd6EvtDRmEmQtreGzyPC29uqg2EnywrPz+kTMSX2nKrGTpP4rnc0nnwD0f/uvNTd+OLzst6C8k3pw6CKOQMwgB8VPcEQNvOA1cIcfoTC//SSkIiQl0C0jaBkhvj8zSxoJyb5N5CGxgp6jSOoCP7Cz75I4mgYyKkiKXTe1EUmzaqC+aOQqUdcTkdoFMpBUugFKZI04guBG1NXaGyiPPwJZaicNwD2syS2k5fr6F33BHJafMMquySnUNpJCXju0ybEjyZ0H+yiRaQ5pFSLtKH2A4pVgulOIG3/hfshAWJAH+MXch10Q5oJYKvGIxUigEfuY1TTf+ztxlDyYC6R4Wp5ZSPOIm24Zrv7NFr8dmjuR5knqw8wlsNqtLh9JvXBXu5uTHNwYspkVtGkCPdQy4obXD+T/efjAREpqWsEayfsre+CFB+trmpPWa5X0D0GyY8B2A+kknAIj2VCP55c+jYPsO+xBUQ0RF2QlOUCXzrA6W1im/CbKBGfUmpHiQ2xLrzvwATh8UFQWlcRe5t4tSB3xL4gydh2cpBs+2wkz+CCS5M1D0ZXsv7htcZINkmatL0ZSKcgZR/kUfHBSa23FIHZOJinItgzEi2wbnVQEZ5fQRAbescrERqqVZAdPIm0vtruPh3U3KqnXPUJ63idt8d59G3QS6uoIOyeibsBLeyoEwNKaqKTW2uw432+wbw9Up2nozEDq0pQGtetJQcLoUNTk7KiW2ehFKCTzopJkLEFc3D9A9C7MW/CPCK3ktiDhcaNraE4yC74aSVQS/vjB3zrxPv53EQ3gvnXwu7LnPSsJ3JVuhD1uGsppRhLpnf+HKWeyjJrEU31mWrPgkeFieTczPBf/Azu0kwNeu/61AAAAAElFTkSuQmCC) no-repeat center center;background-size:73px 37px}.html .pic+.pic{margin-top:4px}.html img{width:100% !important;height:auto !important;min-height:120px}"
				+"</style></head><body ontouchmove=''><div class='html'>"+content+"</div></body></html>";
	}
	
	public static String formatImageUrl(String url){
		String source = url;
		if(source == null||source.startsWith("http://")){
			return source;
		}
		
		if(source.startsWith("u_")){
			source = source.substring(2);
		}
		source = ImageUtils.formatImageUrl(source, ImageUtils.MOBILE);
		return source;
	}
}
