package apap.ti._5.accommodation_2306211231_be.config;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import apap.ti._5.accommodation_2306211231_be.util.ProvinceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PropertyRepository propertyRepository;

    private static final String[] HOTEL_TYPES = {"Single Room","Double Room","Deluxe Room","Superior Room","Suite","Family Room"};
    private static final String[] VILLA_TYPES = {"Luxury","Beachfront","Mountside","Eco-friendly","Romantic"};
    private static final String[] APT_TYPES = {"Studio","1BR","2BR","3BR","Penthouse"};

    @Override
    public void run(String... args) {
        if (propertyRepository.count() > 0) return; // seed only once

        // provinces to assign (round-robin, with robust fallback)
        List<Integer> provinces = new ArrayList<>(ProvinceUtil.getAll().keySet());
        if (provinces.isEmpty()) {
            // In case external source returned empty without throwing, use a sensible default set
            provinces = new ArrayList<>(List.of(
                    31, // DKI Jakarta
                    32, // Jawa Barat
                    33, // Jawa Tengah
                    34, // DI Yogyakarta
                    35, // Jawa Timur
                    36, // Banten
                    51, // Bali
                    12, // Sumatera Utara
                    13, // Sumatera Barat
                    14, // Riau
                    15  // Jambi
            ));
        }
        // Shuffle once so distribution is even but not always starting from the same province
        Collections.shuffle(provinces, new Random(42)); // deterministic for repeatable seeding
        int provinceIdx = 0;

        // Create 10 properties: 3 Hotel (type=1), 3 Villa (type=2), 4 Apartemen (type=3)
        int[] types = {1,1,1, 2,2,2, 3,3,3,3};
        String[] names = {
                "Hotel Alpha","Hotel Beta","Hotel Gamma",
                "Villa Azure","Villa Breeze","Villa Coral",
                "Apartemen Nexus","Apartemen Orion","Apartemen Prism","Apartemen Quantum"
        };

        // Room allocations per group
        int[] perHotel = {30,30,30}; // 90 total
        int[] perVilla = {18,18,19}; // 55 total
        int[] perApt   = {13,14,14,14}; // 55 total

        int propIndex = 0;
        long globalSeq = 0;
        for (int t : types) {
            globalSeq++;
            String propName = names[propIndex];
            Integer province;
            if (!provinces.isEmpty()) {
                province = provinces.get(provinceIdx);
                provinceIdx++;
                if (provinceIdx >= provinces.size()) provinceIdx = 0; // cycle
            } else {
                // Absolute fallback: Jakarta
                province = 31;
            }
            UUID owner = UUID.randomUUID();
            String propId = IdUtil.generatePropertyId(t, owner, globalSeq);

            Property p = Property.builder()
                    .propertyId(propId)
                    .propertyName(propName)
                    .type(t)
                    .address("Jl. Sample No." + (propIndex+1))
                    .province(province)
                    .description("Seeded property")
                    .totalRoom(0) // set later
                    .activeStatus(1)
                    .ownerName("Owner " + (propIndex+1))
                    .ownerId(owner)
                    .build();

            // Build room types list depending on property type
            String[] rtypes = t==1? HOTEL_TYPES : (t==2? VILLA_TYPES : APT_TYPES);
            List<RoomType> listRt = new ArrayList<>();
            int floor = 1;
            for (String rtName : rtypes) {
                String rtId = IdUtil.generateRoomTypeId(propId, rtName, floor);
                RoomType rt = RoomType.builder()
                        .roomTypeId(rtId)
                        .name(rtName)
                        .price(500_000 + 50_000*floor)
                        .description("Seeded room type")
                        .capacity(t==1? (rtName.contains("Family")?4:2) : (t==2?4: (rtName.equals("Studio")?1: (rtName.equals("1BR")?2: (rtName.equals("2BR")?4: (rtName.equals("3BR")?6:4))))) )
                        .facility("AC, TV, WiFi")
                        .floor(floor)
                        .property(p)
                        .build();
                listRt.add(rt);
                floor++;
            }

            // Allocate rooms per property
            int roomsTarget;
            if (t==1) roomsTarget = perHotel[countInGroup(propIndex,0)];
            else if (t==2) roomsTarget = perVilla[countInGroup(propIndex,3)];
            else roomsTarget = perApt[countInGroup(propIndex,6)];

            int created = 0;
            // Track running unit number per floor to avoid duplicate room IDs across room types
            Map<Integer, Integer> nextUnitByFloor = new HashMap<>();
            while (created < roomsTarget) {
                for (RoomType rt : listRt) {
                    // For each room type, add up to 2 rooms per iteration
                    for (int unit=1; unit<=2 && created<roomsTarget; unit++) {
                        int floorNo = rt.getFloor();
                        int unitIndex = nextUnitByFloor.getOrDefault(floorNo, 1);
                        String roomId = IdUtil.generateRoomId(propId, floorNo, unitIndex);
                        Room room = Room.builder()
                                .roomId(roomId)
                                .name("Room " + roomId.substring(roomId.lastIndexOf('-')+1))
                                .availabilityStatus(1)
                                .activeRoom(1)
                                .roomType(rt)
                                .build();
                        rt.addRoom(room);
                        nextUnitByFloor.put(floorNo, unitIndex + 1);
                        created++;
                    }
                }
            }

            // attach room types to property
            for (RoomType rt : listRt) p.addRoomType(rt);
            p.setTotalRoom(roomsTarget);

            propertyRepository.save(p);
            propIndex++;
        }
    }

    private int countInGroup(int propIndex, int groupStart) {
        return propIndex - groupStart;
    }
}
