package com.example.data.local

object InitialCatalogSeeder {
    suspend fun seedInitialProfilesAndComponents(dao: RepairDao) {
        // Prevent duplicate seeding if the base catalog has already been initialized
        if (dao.getDeviceProfileByFingerprint("AMD_RYZEN_7_7800X3D") != null) {
            return
        }

        // Clean up legacy/obsolete monolithic keys if present from earlier runs
        dao.deleteDeviceProfileByFingerprint("CUSTOM_PC_BUILD_ATX")
        dao.deleteComponentsForDevice("CUSTOM_PC_BUILD_ATX")
        dao.deleteDeviceProfileByFingerprint("ESP32_ROBOTICS_4WD")
        dao.deleteComponentsForDevice("ESP32_ROBOTICS_4WD")

        val profiles = mutableListOf<DeviceProfileEntity>()
        val components = mutableListOf<DeviceComponentEntity>()

        // 1. PC-Bau & Hardware: components sorted by real manufacturers (AMD, Intel, ASUS, MSI, Corsair, Samsung, be quiet!)
        CatalogHardwareSeeder.seedHardwareProfiles(profiles, components)

        // 2. Robotik & Maker: individual boards sorted by real manufacturers (Espressif, Arduino, Raspberry Pi)
        CatalogMakerSeeder.seedMakerProfiles(profiles, components)

        // 3. Smartphones, Laptops, Tablets, Haushaltsgeräte, Gaming, Audio, Fahrrad & KFZ
        CatalogDevicesSeeder.seedOtherDeviceProfiles(profiles, components)

        // Insert / Update in database
        dao.insertAllDeviceProfiles(profiles)
        dao.insertAllComponents(components)

        // Office data should remain empty for a new customer
        // OfficeDataSeeder.seedOfficeData(dao)
    }
}

