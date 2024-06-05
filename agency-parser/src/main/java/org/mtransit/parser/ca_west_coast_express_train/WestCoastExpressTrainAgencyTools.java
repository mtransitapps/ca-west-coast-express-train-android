package org.mtransit.parser.ca_west_coast_express_train;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// http://www.translink.ca/en/Schedules-and-Maps/Developer-Resources.aspx
// http://www.translink.ca/en/Schedules-and-Maps/Developer-Resources/GTFS-Data.aspx
// http://mapexport.translink.bc.ca/current/google_transit.zip
// http://ns.translink.ca/gtfs/notifications.zip
// http://ns.translink.ca/gtfs/google_transit.zip
// http://gtfs.translink.ca/static/latest
public class WestCoastExpressTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new WestCoastExpressTrainAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "West Coast Express";
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		//noinspection RedundantIfStatement
		if (!RSN_WCE.contains(gRoute.getRouteShortName())) {
			return EXCLUDE;
		}
		return KEEP;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	private static final long RID_WCE = 997L;

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "WCE":
			return RID_WCE;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	private static final List<String> RSN_WCE = Arrays.asList("997", "WCE", "WEST COAST EXPRESS");
	private static final String WCE_SHORT_NAME = "WCE";

	@NotNull
	@Override
	public String provideMissingRouteShortName(@NotNull GRoute gRoute) {
		final String routeLongNameLC = gRoute.getRouteLongNameOrDefault().toLowerCase(getFirstLanguageNN());
		if (routeLongNameLC.equals("west coast express")) {
			return "WCE";
		}
		return super.provideMissingRouteShortName(gRoute);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Pattern WCE_ = Pattern.compile("((^|\\W)(west coast express)(\\W|$))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = WCE_.matcher(routeLongName).replaceAll(EMPTY); // removing trade-mark
		return routeLongName;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_VIOLET = "87189D"; // VIOLET (from GTFS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_VIOLET;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), tripHeadsign);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STATION_STN = Pattern.compile("(station|stn)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UNLOADING = Pattern.compile("(unload(ing)?( only)?$)", Pattern.CASE_INSENSITIVE);

	private static final String WCE_REPLACEMENT = "$2" + WCE_SHORT_NAME + "$4";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), gStopName);
		gStopName = STATION_STN.matcher(gStopName).replaceAll(EMPTY);
		gStopName = UNLOADING.matcher(gStopName).replaceAll(EMPTY);
		gStopName = WCE_.matcher(gStopName).replaceAll(WCE_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		if (!StringUtils.isEmpty(gStop.getStopCode()) && CharUtils.isDigitsOnly(gStop.getStopCode())) {
			return Integer.parseInt(gStop.getStopCode()); // using stop code as stop ID
		}
		//noinspection deprecation
		return 1_000_000 + Integer.parseInt(gStop.getStopId());
	}
}
