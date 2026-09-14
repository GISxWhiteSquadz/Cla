package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RepairHistoryEntity::class,
        RepairProjectEntity::class,
        KnowledgeChunkEntity::class,
        SavedManualEntity::class,
        DeviceProfileEntity::class,
        DeviceComponentEntity::class,
        OfficeDocumentEntity::class,
        PartComparisonEntity::class,
        RepairSkillPackEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class RepairDatabase : RoomDatabase() {
    abstract fun repairDao(): RepairDao

    companion object {
        @Volatile
        private var INSTANCE: RepairDatabase? = null

        fun getInstance(context: Context): RepairDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RepairDatabase::class.java,
                    "repair_agent_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).repairDao()
                populateInitialKnowledgeBase(dao)
                InitialCatalogSeeder.seedInitialProfilesAndComponents(dao)
                RepairSkillPacksCatalog.seedInitialSkillPacks(dao)
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).repairDao()
                populateInitialKnowledgeBase(dao)
                InitialCatalogSeeder.seedInitialProfilesAndComponents(dao)
                RepairSkillPacksCatalog.seedInitialSkillPacks(dao)
            }
        }
    }
}

suspend fun populateInitialKnowledgeBase(dao: RepairDao) {
    if (dao.getChunksCount() > 0) return

    val initialChunks = listOf(
        // Smartphone: iPhone 13
        KnowledgeChunkEntity(
            deviceFingerprintKey = "APPLE_IPHONE_13",
            manufacturer = "Apple",
            model = "iPhone 13",
            category = "Smartphone",
            title = "Displaywechsel & True Tone / Face ID Kalibrierung",
            content = "Vor dem Öffnen den Akku auf unter 25% entladen (Brandgefahr bei Stichbeschädigung). Display bei 75°C vorsichtig erwärmen. Pentalobe P2 Schrauben lösen. Beim Aufklappen nach rechts aufpassen: Display- und Digitizer-Flexkabel reißen leicht ab! Face ID Sensor-Assembly vom alten Display vorsichtig mit Isopropanol ablösen und auf das neue Panel umbauen, um Face ID Funktionalität zu erhalten.",
            vectorTokensCsv = "apple,iphone,13,display,oled,faceid,pentalobe,akku,flexkabel,glas,touch,reparatur",
            source = "Herstellerhandbuch",
            confidence = 0.98f
        ),
        // Smartphone: Samsung Galaxy S23
        KnowledgeChunkEntity(
            deviceFingerprintKey = "SAMSUNG_GALAXY_S23",
            manufacturer = "Samsung",
            model = "Galaxy S23",
            category = "Smartphone",
            title = "USB-C Ladebuchse & Sub-Board Austausch",
            content = "Wenn das Gerät 'Feuchtigkeit im USB-Port erkannt' meldet oder nur im Winkel lädt: Rückseite mit Heißluft (80°C) lösen. 14 Phillips #00 Schrauben entfernen. Akku-Konnektor als Erstes trennen! Sub-Board mit Ladebuchse entnehmen. Vor dem Zusammenbau prüfen, ob das neue Board die Original-Schnellladung (25W Super Fast Charging) und Mikrofone unterstützt.",
            vectorTokensCsv = "samsung,galaxy,s23,usbc,laden,feuchtigkeit,subboard,akku,ladebuchse,port,reparatur",
            source = "Herstellerhandbuch",
            confidence = 0.96f
        ),
        // Kaffeemaschine / Haushaltsgerät: DeLonghi Magnifica / ECAM
        KnowledgeChunkEntity(
            deviceFingerprintKey = "DELONGHI_MAGNIFICA_ECAM",
            manufacturer = "DeLonghi",
            model = "Magnifica S / ECAM 22.110",
            category = "Haushaltsgeräte",
            title = "Allgemeine Störung / Fehler 8 / Brüheinheit blockiert",
            content = "Brüheinheit lässt sich nicht entnehmen oder Motor brummt laut: Gerät ausschalten und Netzstecker ziehen! Hintere und seitliche Verkleidung (Torx T20 mit Bohrung) entfernen. Spindel der Brüheinheit auf Kaffeesatz und fehlendes Schmiermittel prüfen. Mit Silikonfett (lebensmittelecht nach NSF H1) neu fetten. Endschalter oben und unten mit Multimeter auf Durchgang prüfen. Bei Fehler 8 ist oft der Antriebsriemen gerissen oder der Mikroschalter defekt.",
            vectorTokensCsv = "delonghi,magnifica,ecam,kaffeemaschine,brueheinheit,fehler8,motor,spindel,silikonfett,mikroschalter,antrieb",
            source = "Herstellerhandbuch",
            confidence = 0.99f
        ),
        // Haushalt: Bosch Waschmaschine Serie 6
        KnowledgeChunkEntity(
            deviceFingerprintKey = "BOSCH_SERIE_6_WASCHMASCHINE",
            manufacturer = "Bosch",
            model = "Serie 6 / WAN",
            category = "Haushaltsgeräte",
            title = "Fehlercode E18 / Pumpe pumpt nicht ab",
            content = "Sicherheits-Hinweis: Vor Arbeiten Netzstecker ziehen und Zulaufhahn schließen! Fehler E18 bedeutet Abpumpzeit überschritten. Notentleerungsschlauch an der rechten unteren Sockelklappe in flache Schale entleeren. Flusensieb herausdrehen und auf Fremdkörper (Münzen, Haarklammern) prüfen. Mit Taschenlampe prüfen, ob sich der Pumpenflügel frei mit leichtem magnetischem Rastwiderstand drehen lässt. Ablaufschlauch auf Knicke und Siphon auf Verstopfung untersuchen.",
            vectorTokensCsv = "bosch,serie6,waschmaschine,e18,pumpe,abpumpen,flusensieb,notentleerung,sockel,ablauf,wasser",
            source = "Herstellerhandbuch",
            confidence = 0.99f
        ),
        // Laptop: MacBook Pro 14 (M1/M2/M3)
        KnowledgeChunkEntity(
            deviceFingerprintKey = "APPLE_MACBOOK_PRO",
            manufacturer = "Apple",
            model = "MacBook Pro 14",
            category = "Laptop & PC",
            title = "Lüfterreinigung & Wärmeleitpaste / Akkutausch",
            content = "P5 Pentalobe Schrauben am Unterboden lösen (Längen merken!). Gehäusedeckel mit Saugnapf leicht anheben und nach vorne schieben, um die Verriegelungsclips nicht zu brechen. Sofort den Batterie-Trennschalter (Battery Disconnect) betätigen! Lüfter mit Pinsel und Druckluft säubern, dabei die Lüfterflügel blockieren, um keine Induktionsspannung ins Mainboard einzuspeisen.",
            vectorTokensCsv = "apple,macbook,pro,m1,m2,luefter,akku,waermeleitpaste,heiss,laut,pentalobe,tastatur,display",
            source = "Herstellerhandbuch",
            confidence = 0.97f
        ),
        // Laptop: Lenovo ThinkPad T14
        KnowledgeChunkEntity(
            deviceFingerprintKey = "LENOVO_THINKPAD_T14",
            manufacturer = "Lenovo",
            model = "ThinkPad T14",
            category = "Laptop & PC",
            title = "Tastaturtausch & CMOS Reset nach Nichtstart",
            content = "Im BIOS vorab 'Disable Internal Battery' auswählen, falls noch bedienbar. Bei schwarzem Bildschirm: Notfall-Reset-Taster (kleines Loch an der Gehäuseunterseite mit Büroklammer für 10 Sekunden drücken). Zum Tastaturwechsel Schrauben unter den TrackPoint-Tasten bzw. Tastaturrahmen nach oben schieben.",
            vectorTokensCsv = "lenovo,thinkpad,t14,laptop,tastatur,cmos,reset,akku,ram,mainboard,boot,reparatur",
            source = "Herstellerhandbuch",
            confidence = 0.95f
        ),
        // Fahrzeug: OBD-2 Diagnose & Batterie
        KnowledgeChunkEntity(
            deviceFingerprintKey = "FAHRZEUG_OBD2_SYSTEM",
            manufacturer = "Universal / VAG / BMW / Mercedes",
            model = "OBD-2 Diagnose",
            category = "Fahrzeug",
            title = "Fehlercodes P0300 (Fehlzündungen) & AGM Batteriewechsel",
            content = "Fehler P0300/P0301-P0304 bedeutet Verbrennungsaussetzer auf Zylindern. Prüfreihenfolge: 1. Zündkerzenbild (verrußt, abgebrannt, ölig). 2. Zündspule quer zu anderem Zylinder tauschen und prüfen, ob Fehler mitwandert. 3. Einspritzventil & Falschluft im Ansaugtrakt mit Nebelmaschine prüfen. Bei Batteriewechsel: AGM-Batterie muss im Batteriemanagement (BMS) angelernt werden, sonst überlädt die Lichtmaschine den neuen Akku!",
            vectorTokensCsv = "fahrzeug,auto,obd2,p0300,zuendspule,zuendkerzen,agm,batterie,bms,lichtmaschine,motorleuchte",
            source = "Werkstatt-Datenbank",
            confidence = 0.98f
        ),
        // E-Bike: Bosch Performance Line CX
        KnowledgeChunkEntity(
            deviceFingerprintKey = "BOSCH_EBIKE_PERFORMANCE",
            manufacturer = "Bosch",
            model = "Performance Line CX Gen 4",
            category = "Fahrrad & E-Bike",
            title = "Fehlercode 500 & Speichenmagnet-Justierung",
            content = "Fehler 500 deutet auf einen internen Elektronik- oder Motorsensorfehler hin. Zuerst Kontakte am Akkuhalter mit Kontaktreiniger säubern. Häufigste Ursache für Aussetzer: Der Speichenmagnet am Hinterrad hat sich verdreht. Abstand zum Sensor an der Kettenstrebe muss zwischen 5 mm und 17 mm betragen. Drehmoment der Kurbelschrauben prüfen (50 Nm mit Isis/ISIS-Drive).",
            vectorTokensCsv = "bosch,ebike,fahrrad,motor,fehler500,speichenmagnet,sensor,akku,antrieb,kette,schaltung",
            source = "Herstellerhandbuch",
            confidence = 0.98f
        ),
        // Gaming: Sony PlayStation 5
        KnowledgeChunkEntity(
            deviceFingerprintKey = "SONY_PLAYSTATION_5",
            manufacturer = "Sony",
            model = "PlayStation 5",
            category = "Gaming & Konsolen",
            title = "Flüssigmetall-Oxidation & Überhitzung (3 Beeps)",
            content = "Symptom: Konsole schaltet sich nach 15-30 Minuten im Spiel ohne Vorwarnung ab. Lüfter und Kühlkörperlamellen (Power Supply Vents) auf dichten Staub prüfen. Bei vertikalem Betrieb über Jahre kann das Flüssigmetall (Gallium-Indium) am APU-Die oxidieren oder ungleichmäßig verlaufen sein ('Dry Spot'). Warnung: Flüssigmetall ist stark elektrisch leitfähig! Nur mit Kaptonband-Schutz und Q-Tips erneuern.",
            vectorTokensCsv = "sony,ps5,playstation5,konsole,ueberhitzung,luefter,fluessigmetall,hdmi,netzteil,abschaltung",
            source = "Werkstatt-Datenbank",
            confidence = 0.97f
        ),
        // Gaming: Nintendo Switch Joy-Con
        KnowledgeChunkEntity(
            deviceFingerprintKey = "NINTENDO_SWITCH_JOYCON",
            manufacturer = "Nintendo",
            model = "Switch Joy-Con",
            category = "Gaming & Konsolen",
            title = "Stick-Drift Beseitigung & Hall-Sensor Upgrade",
            content = "Ursache für Stick-Drift: Graphitschicht auf den internen Potentiometern scheuert durch. Schnelle Behebung: Kontaktspray (kein WD-40, sondern Tuner 600 / Kontakt WL) unter die Gummilippe sprühen. Dauerhafte Lösung: Austausch des Analogsticks gegen moderne Hall-Effekt Sticks (elektromagnetisch, kein mechanischer Abrieb). Tri-Wing Y00 Schrauben verwenden, Akku vorab vorsichtig abhebeln.",
            vectorTokensCsv = "nintendo,switch,joycon,stickdrift,analogstick,hall,triwing,driften,tasten,controller",
            source = "Community-Fix",
            confidence = 0.96f
        )
    )

    dao.insertAllChunks(initialChunks)

    // Also insert a helpful saved manual
    dao.insertManual(
        SavedManualEntity(
            title = "DeLonghi Magnifica S - Fehler 8 & Wartungsleitfaden",
            category = "Haushaltsgeräte",
            deviceFingerprintKey = "DELONGHI_MAGNIFICA_ECAM",
            summary = "Komplette Wartungsanleitung für Brüheinheit, O-Ringe und Endschalter.",
            fullContent = "Schritt 1: Netzstecker ziehen.\nSchritt 2: Brüheinheit mit Drücken der beiden roten Tasten entnehmen.\nSchritt 3: Im warmen Wasser spülen (keine Spülmaschine!).\nSchritt 4: O-Ringe mit lebensmittelechtem Silikonfett schmieren.\nSchritt 5: Spindel im Maschineninneren reinigen und nachfetten."
        )
    )
}
