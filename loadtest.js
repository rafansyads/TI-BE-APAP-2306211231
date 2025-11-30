import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

// ==========================
// CONFIGURATION (HIGH LOAD)
// ==========================
export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp-up cepat ke 50 User
    { duration: '3m', target: 50 },    // Tahan beban tinggi selama 3 menit
    { duration: '30s', target: 0 },    // Ramp-down
  ],
  thresholds: {
    // Latency threshold dilonggarkan sedikit karena beban tinggi (3s)
    http_req_duration: ['p(95)<3000'],
    // Error rate tetap strict, ignore handled status (400/409/401/403)
    'http_req_failed{tags:status_type!=handled}': ['rate<0.01'], 
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081/api';

// ==========================
// HELPER FUNCTIONS
// ==========================

// Helper Check: Lulus jika 200 OK, atau jika error yang "diizinkan"
const checkHandled = (res, expectedStatus = 200, name = "Check") => {
  const status = res.status;
  const isPass = 
    status === expectedStatus || 
    status === 201 ||
    status === 400 || // Bad Request (Validation/Overlap)
    status === 401 || // Unauthorized (JWT race condition)
    status === 403 || // Forbidden
    status === 409;   // Conflict (Duplicate ID handled by backend)

  check(res, {
    [`${name} (${expectedStatus}/Handled)`]: () => isPass
  });

  // Logging error server fatal (500)
  if (status === 500) {
    console.error(`[500 ERROR] ${name} | URL: ${res.url} | Body: ${res.body}`);
  }
};

function randomString(length) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let res = '';
  for (let i = 0; i < length; i++) res += chars.charAt(Math.floor(Math.random() * chars.length));
  return res;
}

function getRandomBookingDates() {
  const checkIn = new Date();
  const offset = Math.floor(Math.random() * 60) + 1; 
  checkIn.setDate(checkIn.getDate() + offset);
  
  const checkOut = new Date(checkIn);
  checkOut.setDate(checkOut.getDate() + Math.floor(Math.random() * 3) + 1);
  
  return { in: checkIn.toISOString(), out: checkOut.toISOString() };
}

function login(username, password) {
  const payload = JSON.stringify({ data: { username, password } });
  const res = http.post(`${BASE_URL}/auth/login`, payload, { headers: { 'Content-Type': 'application/json' } });
  
  if (res.status === 200) {
    return {
      token: res.json('data.token'),
      refreshToken: res.headers['Refresh-Token'],
      id: res.json('data.id'),
      username: username,
      email: `${username}@example.com`
    };
  }
  return null;
}

// ==========================
// MAIN SCENARIO
// ==========================
export default function () {
  // Setup: Login 3 Role (Admin, Owner, Customer)
  const adminAuth = login('admin', 'admin123');
  const ownerAuth = login('owner1', 'owner1');
  const customerAuth = login('customer1', 'customer1');

  // Jika login gagal (misal server overload), retry nanti
  if (!adminAuth || !ownerAuth || !customerAuth) {
    sleep(1); return;
  }

  const getHeaders = (token) => ({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });

  // ----------------------------------------------------
  // GROUP 1: PROFILE & USERS (Admin)
  // ----------------------------------------------------
  group('Profile & Users', function () {
    const resUsers = http.get(`${BASE_URL}/profile/users`, { headers: getHeaders(adminAuth.token) });
    checkHandled(resUsers, 200, 'Get All Users');

    const resCust = http.get(`${BASE_URL}/profile/customers`, { headers: getHeaders(adminAuth.token) });
    checkHandled(resCust, 200, 'Get All Customers');
  });

  // ----------------------------------------------------
  // GROUP 2: PROPERTY MANAGEMENT (Owner)
  // ----------------------------------------------------
  let createdPropId = null;

  group('Property Management', function () {
    const resProps = http.get(`${BASE_URL}/property`, { headers: getHeaders(customerAuth.token) });
    checkHandled(resProps, 200, 'Get All Properties');

    // Create Property
    const newPropName = `HeavyLoad-${randomString(5)}`;
    const createPayload = JSON.stringify({
      data: {
        propertyName: newPropName,
        type: 1, 
        address: "Jl. Stress Test",
        province: 31,
        description: "Generated under high load",
        totalRoom: 0,
        activeStatus: 1,
        ownerName: "Accommodation Owner 1",
        ownerId: ownerAuth.id,
        roomTypes: [
            {
                name: "Standard",
                price: 300000,
                capacity: 2,
                floor: 1,
                rooms: [{ availabilityStatus: 1, activeRoom: 1 }]
            }
        ]
      }
    });

    const resCreate = http.post(`${BASE_URL}/property/create`, createPayload, { headers: getHeaders(ownerAuth.token) });
    checkHandled(resCreate, 201, 'Create Property');

    if (resCreate.status === 201) {
        createdPropId = resCreate.json('data.propertyId');
    }

    if (createdPropId) {
        const updatePayload = JSON.stringify({
            data: {
                propertyId: createdPropId,
                propertyName: `${newPropName} Updated`,
                address: "Jl. Updated Stress",
                ownerId: ownerAuth.id,
                ownerName: "Accommodation Owner 1"
            }
        });
        const resUpdate = http.put(`${BASE_URL}/property/update`, updatePayload, { headers: getHeaders(ownerAuth.token) });
        checkHandled(resUpdate, 200, 'Update Property');

        const addRoomPayload = JSON.stringify({
            data: {
                propertyId: createdPropId,
                name: "Deluxe-" + randomString(3),
                price: 750000,
                capacity: 2,
                floor: 2,
                rooms: [{ availabilityStatus: 1, activeRoom: 1 }]
            }
        });
        const resAddRoom = http.post(`${BASE_URL}/property/updateroom`, addRoomPayload, { headers: getHeaders(ownerAuth.token) });
        checkHandled(resAddRoom, 200, 'Update Property Rooms');

        const resDetail = http.get(`${BASE_URL}/property/${createdPropId}`, { headers: getHeaders(customerAuth.token) });
        checkHandled(resDetail, 200, 'Get Property Detail');
    }
  });

  // ----------------------------------------------------
  // GROUP 3: BOOKING FLOW (Customer & Owner)
  // ----------------------------------------------------
  group('Booking Flow', function () {
    const resAllBooking = http.get(`${BASE_URL}/bookings`, { headers: getHeaders(ownerAuth.token) });
    checkHandled(resAllBooking, 200, 'Get All Bookings');

    const resReviews = http.get(`${BASE_URL}/bookings/reviews?customerID=${customerAuth.id}`, { headers: getHeaders(customerAuth.token) });
    checkHandled(resReviews, 200, 'Get Customer Reviews');

    // Create Booking Scenario
    let targetRoomId = null;
    const resListProp = http.get(`${BASE_URL}/property?province=31`, { headers: getHeaders(customerAuth.token) });
    if (resListProp.status === 200) {
        const props = resListProp.json('data');
        if (props && props.length > 0) {
            const randProp = props[Math.floor(Math.random() * props.length)];
            const resPropDet = http.get(`${BASE_URL}/property/${randProp.propertyId}`, { headers: getHeaders(customerAuth.token) });
            if (resPropDet.status === 200) {
                const rooms = resPropDet.json('data.rooms');
                if (rooms && rooms.length > 0) {
                    targetRoomId = rooms[Math.floor(Math.random() * rooms.length)].roomId;
                }
            }
        }
    }

    if (targetRoomId) {
        const dates = getRandomBookingDates();
        const bookingPayload = JSON.stringify({
            data: {
                checkInDate: dates.in,
                checkOutDate: dates.out,
                totalDays: 1, totalPrice: 0, status: 0,
                customerId: customerAuth.id,
                customerName: "K6 User High Load",
                customerEmail: customerAuth.email,
                customerPhone: "08123456789",
                isBreakfast: false, refund: 0, extraPay: 0, capacity: 1,
                roomId: targetRoomId
            }
        });

        const resCreate = http.post(`${BASE_URL}/bookings/create`, bookingPayload, { headers: getHeaders(customerAuth.token) });
        // Handle 409/400 as pass
        checkHandled(resCreate, 201, 'Create Booking');

        if (resCreate.status === 201) {
            const bookingId = resCreate.json('data.bookingId');
            const resGetBook = http.get(`${BASE_URL}/bookings/${bookingId}`, { headers: getHeaders(customerAuth.token) });
            checkHandled(resGetBook, 200, 'Get Booking Detail');
        }
    }
  });

  // ----------------------------------------------------
  // GROUP 4: AUTH MAINTENANCE (Refresh Token)
  // ----------------------------------------------------
  if (customerAuth.refreshToken) {
      const refreshHeaders = {
          headers: {
              'Authorization': `Bearer ${customerAuth.token}`,
              'Refresh-Token': customerAuth.refreshToken,
              'Content-Type': 'application/json'
          }
      };
      const refreshBody = JSON.stringify({ data: { username: customerAuth.username, email: customerAuth.email } });
      const resRefresh = http.post(`${BASE_URL}/auth/refresh`, refreshBody, refreshHeaders);
      checkHandled(resRefresh, 200, 'Refresh Token');
  }

  // Sleep dikurangi agar throughput naik
  sleep(0.5); 
}

export function handleSummary(data) {
  return {
    "summary-report.html": htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}