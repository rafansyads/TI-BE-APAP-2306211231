package apap.ti._5.accommodation_2306211231_be.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Static lookup for Indonesian province codes and names.
 * Codes follow Kemendagri-style IDs (e.g., 11=Aceh, 31=DKI Jakarta, 96=Papua Barat Daya).
 * Asterisks or plus signs in source tables are ignored for name matching.
 */
public final class ProvinceUtil {

    private static final Map<Integer, String> CODE_TO_NAME;
    private static final Map<String, Integer> NAME_TO_CODE;
    private static final String WILAYAH_URL = "https://wilayah.id/api/provinces.json";

    static {
        Map<Integer, String> m = new LinkedHashMap<>();

        // Fetch data from WILAYAH_URL
        try {
            // Implementation to fetch and parse JSON data from WILAYAH_URL
            // and populate the map 'm' goes here.
            var json = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(URI.create(WILAYAH_URL).toURL().openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                // The API endpoint returns an object with a `data` array of provinces (or historically a plain array)
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json.toString());
                JsonNode dataNode = root.isArray() ? root : (root.isObject() ? root.path("data") : null);
                if (dataNode != null && dataNode.isArray()) {
                    for (JsonNode province : dataNode) {
                        try {
                            // API provides "code" as string (e.g. "11"); older sources may use numeric "id"
                            int code;
                            String codeStr = province.path("code").asText("");
                            if (!codeStr.isEmpty()) {
                                code = Integer.parseInt(codeStr);
                            } else {
                                code = province.path("id").asInt();
                            }
                            String name = province.path("name").asText("");
                            if (!name.isBlank()) {
                                m.put(code, name);
                            }
                        } catch (Exception ignore) {
                            // skip malformed entries
                        }
                    }
                }
            }
            // If remote parse succeeded but yielded no entries (schema drift), fill fallback
            if (m.isEmpty()) {
                fillFallback(m);
            }
        }
        catch (Exception e) { 
            // Fallback to hardcoded data if fetching fails
            fillFallback(m);
        }

        CODE_TO_NAME = Collections.unmodifiableMap(m);

        Map<String, Integer> n2c = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> e : CODE_TO_NAME.entrySet()) {
            n2c.put(normalizeName(e.getValue()), e.getKey());
        }
        NAME_TO_CODE = Collections.unmodifiableMap(n2c);
    }

    private ProvinceUtil() {}

    /** Returns the official province name for the given code, or empty if unknown. */
    public static Optional<String> getNameByCode(Integer code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(CODE_TO_NAME.get(code));
    }

    /** Returns the province code for a given name (case-insensitive, diacritics/marks ignored), or empty if not found. */
    public static Optional<Integer> getCodeByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return Optional.ofNullable(NAME_TO_CODE.get(normalizeName(name)));
    }

    /** True if the code exists in the static table. */
    public static boolean isValidCode(Integer code) {
        return code != null && CODE_TO_NAME.containsKey(code);
    }

    /** Immutable map view of code→name. */
    public static Map<Integer, String> getAll() {
        return CODE_TO_NAME;
    }

    private static String normalizeName(String s) {
        // Remove *, + and trim; collapse multiple spaces; lowercase for map key
        String cleaned = s.replace("*", "").replace("+", "").trim();
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private static void fillFallback(Map<Integer, String> m) {
        // Sumatera
        m.put(11, "Aceh");
        m.put(12, "Sumatera Utara");
        m.put(13, "Sumatera Barat");
        m.put(14, "Riau");
        m.put(15, "Jambi");
        m.put(16, "Sumatera Selatan");
        m.put(17, "Bengkulu");
        m.put(18, "Lampung");
        m.put(19, "Kepulauan Bangka Belitung");
        m.put(21, "Kepulauan Riau");

        // Jawa
        m.put(31, "DKI Jakarta");
        m.put(32, "Jawa Barat");
        m.put(33, "Jawa Tengah");
        m.put(34, "Daerah Istimewa Yogyakarta");
        m.put(35, "Jawa Timur");
        m.put(36, "Banten");

        // Bali & Nusa Tenggara
        m.put(51, "Bali");
        m.put(52, "Nusa Tenggara Barat");
        m.put(53, "Nusa Tenggara Timur");

        // Kalimantan
        m.put(61, "Kalimantan Barat");
        m.put(62, "Kalimantan Tengah");
        m.put(63, "Kalimantan Selatan");
        m.put(64, "Kalimantan Timur");
        m.put(65, "Kalimantan Utara");

        // Sulawesi
        m.put(71, "Sulawesi Utara");
        m.put(72, "Sulawesi Tengah");
        m.put(73, "Sulawesi Selatan");
        m.put(74, "Sulawesi Tenggara");
        m.put(75, "Gorontalo");
        m.put(76, "Sulawesi Barat");

        // Maluku & Papua
        m.put(81, "Maluku");
        m.put(82, "Maluku Utara");
        m.put(91, "Papua");
        m.put(92, "Papua Barat");
        m.put(93, "Papua Selatan");
        m.put(94, "Papua Tengah");
        m.put(95, "Papua Pegunungan");
        m.put(96, "Papua Barat Daya");
    }
}
