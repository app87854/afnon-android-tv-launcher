package tv.afnon.home;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A city's official prayer table, for cities whose authority publishes one (Libya: Ministry of Awqaf). The file
 * {@code assets/times/<city id>.txt} has one line per calendar day, "MM-DD fajr sunrise dhuhr asr maghrib isha",
 * local clock, leap day included; lines starting with # are comments. A city with no file is calculated instead.
 */
final class OfficialTimes {
    private static final Map<String, OfficialTimes> CACHE = new HashMap<>();

    /** Minutes after midnight for the six times, by month * 32 + day; null for a day the file does not have. */
    private final int[][] byDay = new int[13 * 32][];

    private OfficialTimes() {}

    /** The table for this city, or null if it has none (or the file cannot be read). */
    static synchronized OfficialTimes of(Context c, String cityId) {
        if (CACHE.containsKey(cityId)) return CACHE.get(cityId);
        OfficialTimes table = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                c.getAssets().open("times/" + cityId + ".txt"), StandardCharsets.UTF_8))) {
            OfficialTimes t = new OfficialTimes();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                t.add(line.split(" "));
            }
            table = t;
        } catch (IOException | RuntimeException e) {
            // No table for this city, or a damaged one: the times are calculated instead.
        }
        CACHE.put(cityId, table);
        return table;
    }

    private void add(String[] f) {
        int[] minutes = new int[6];
        for (int i = 0; i < 6; i++) {
            String[] hm = f[i + 1].split(":");
            minutes[i] = Integer.parseInt(hm[0]) * 60 + Integer.parseInt(hm[1]);
        }
        byDay[Integer.parseInt(f[0].substring(0, 2)) * 32 + Integer.parseInt(f[0].substring(3, 5))] = minutes;
    }

    /** The six times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) on the day of {@code day}; null if the table lacks it. */
    Date[] on(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        int[] m = byDay[(cal.get(Calendar.MONTH) + 1) * 32 + cal.get(Calendar.DAY_OF_MONTH)];
        if (m == null) return null;
        Date[] out = new Date[6];
        for (int i = 0; i < 6; i++) {
            cal.set(Calendar.HOUR_OF_DAY, m[i] / 60);
            cal.set(Calendar.MINUTE, m[i] % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            out[i] = cal.getTime();
        }
        return out;
    }
}
