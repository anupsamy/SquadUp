// test/locationService.test.ts
import dotenv from "dotenv";
dotenv.config();

import { locationService } from "./src/services/location.service";
import type { UserLocation } from "./src/types/location.types";

(async () => {
  try {
    console.log("Loaded API key prefix:", process.env.MAPS_API_KEY?.slice(0, 10));

    // Dummy users for testing
    const users: UserLocation[] = [
      { userID: "user1", lat: 49.2827, lng: -123.1207 }, // Vancouver
      { userID: "user2", lat: 49.1666, lng: -123.1336 }, // Richmond
      { userID: "user3", lat: 49.2463, lng: -123.1162 }, // Burnaby
      { userID: "user4", lat: 49.1951, lng: -123.1450 }, // Delta
      { userID: "user5", lat: 49.3000, lng: -123.1300 }, // North Vancouver
    ];

    console.log("▶ Running LocationService test...");

    // 1️⃣ Compute initial geographic midpoint
    const midpoint = locationService.getGeographicMidpoint(users);
    console.log("Initial geographic midpoint:", midpoint);

    // 2️⃣ Display travel time of each user to midpoint
    console.log("Travel times from each user to midpoint:");
    for (const user of users) {
      const time = await locationService.getTravelTime(user, midpoint, "transit");
      console.log(`${user.userID}: ${time.toFixed(2)} minutes`);
    }

    // 3️⃣ Find optimal meeting point iteratively
    const optimalPoint = await locationService.findOptimalMeetingPoint(users, "transit", 5);
    console.log("Optimal meeting point:", optimalPoint);

    // 4️⃣ Get nearby restaurants around the optimal point
    const nearbyRestaurants = await locationService.getTopNearbyPlaces(optimalPoint, "restaurant", 2000);
    console.log("Nearby restaurants around midpoint:", nearbyRestaurants.map(r => r.name));

  } catch (err) {
    console.error("Test failed:", err);
  }
})();
