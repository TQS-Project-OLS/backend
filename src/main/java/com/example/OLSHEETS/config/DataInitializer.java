package com.example.OLSHEETS.config;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private InstrumentRepository instrumentRepository;

        @Autowired
        private MusicSheetRepository musicSheetRepository;

        @PostConstruct
        public void init() {
                // Check if data already exists
                if (userRepository.count() > 0) {
                        System.out.println("Database already initialized. Skipping data insertion.");
                        return;
                }

                System.out.println("Initializing database with sample data...");

                // Create Users
                createUsers();

                // Create Instruments
                createInstruments();

                // Create Music Sheets
                createMusicSheets();

                System.out.println("Database initialization complete!");
        }

        private void createUsers() {
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                String hashedPassword = encoder.encode("password123");

                User[] users = {
                                new User("john_doe", "john@example.com", "John Doe", hashedPassword),
                                new User("jane_smith", "jane@example.com", "Jane Smith", hashedPassword),
                                new User("mike_wilson", "mike@example.com", "Mike Wilson", hashedPassword),
                                new User("sarah_jones", "sarah@example.com", "Sarah Jones", hashedPassword),
                                new User("david_brown", "david@example.com", "David Brown", hashedPassword),
                                new User("emily_davis", "emily@example.com", "Emily Davis", hashedPassword),
                                new User("chris_miller", "chris@example.com", "Chris Miller", hashedPassword),
                                new User("lisa_garcia", "lisa@example.com", "Lisa Garcia", hashedPassword)
                };

                userRepository.saveAll(Arrays.asList(users));
                System.out.println("Created " + users.length + " users (default password: password123)");
        }

        private void createInstruments() {
                Instrument[] instruments = {
                                createInstrument("Yamaha P-125 Digital Piano",
                                                "Professional 88-key digital piano with weighted keys",
                                                45.0, InstrumentType.DIGITAL, InstrumentFamily.KEYBOARD, 2, 1),
                                createInstrument("Fender Stratocaster", "Classic electric guitar with vintage tone",
                                                60.0,
                                                InstrumentType.ELECTRIC, InstrumentFamily.GUITAR, 5, 1),
                                createInstrument("Gibson Les Paul", "Premium electric guitar with mahogany body", 75.0,
                                                InstrumentType.ELECTRIC, InstrumentFamily.GUITAR, 3, 2),
                                createInstrument("Taylor 214ce Acoustic", "Beautiful acoustic guitar with cutaway",
                                                50.0,
                                                InstrumentType.ACOUSTIC, InstrumentFamily.GUITAR, 1, 2),
                                createInstrument("Roland TD-17 Drum Kit", "Electronic drum set with mesh heads", 80.0,
                                                InstrumentType.DRUMS, InstrumentFamily.PERCUSSION, 2, 3),
                                createInstrument("Yamaha YAS-280 Alto Sax", "Student-level alto saxophone", 55.0,
                                                InstrumentType.WIND,
                                                InstrumentFamily.WOODWIND, 4, 3),
                                createInstrument("Fender Precision Bass", "Classic 4-string electric bass", 55.0,
                                                InstrumentType.BASS,
                                                InstrumentFamily.GUITAR, 6, 4),
                                createInstrument("Korg Minilogue XD", "Polyphonic analog synthesizer", 65.0,
                                                InstrumentType.SYNTHESIZER,
                                                InstrumentFamily.KEYBOARD, 1, 4),
                                createInstrument("Martin D-28 Acoustic", "Premium dreadnought acoustic guitar", 85.0,
                                                InstrumentType.ACOUSTIC, InstrumentFamily.GUITAR, 10, 5),
                                createInstrument("Yamaha P-45 Digital Piano", "Compact 88-key digital piano", 35.0,
                                                InstrumentType.DIGITAL, InstrumentFamily.KEYBOARD, 3, 5),
                                createInstrument("Pearl Export Drum Kit", "5-piece acoustic drum set", 70.0,
                                                InstrumentType.DRUMS,
                                                InstrumentFamily.PERCUSSION, 5, 6),
                                createInstrument("Ibanez RG Series", "Modern electric guitar for metal/rock", 58.0,
                                                InstrumentType.ELECTRIC, InstrumentFamily.GUITAR, 2, 6),
                                createInstrument("Yamaha YTR-2330 Trumpet", "Student brass trumpet", 48.0,
                                                InstrumentType.WIND,
                                                InstrumentFamily.BRASS, 3, 7),
                                createInstrument("Moog Subsequent 37", "Analog synthesizer with classic sound", 95.0,
                                                InstrumentType.SYNTHESIZER, InstrumentFamily.KEYBOARD, 1, 7),
                                createInstrument("Music Man StingRay Bass", "Professional 4-string bass guitar", 68.0,
                                                InstrumentType.BASS, InstrumentFamily.GUITAR, 4, 8)
                };

                instrumentRepository.saveAll(Arrays.asList(instruments));
                System.out.println("Created " + instruments.length + " instruments");
        }

        private Instrument createInstrument(String name, String description, Double price,
                        InstrumentType type, InstrumentFamily family,
                        Integer age, int ownerId) {
                Instrument instrument = new Instrument();
                instrument.setName(name);
                instrument.setDescription(description);
                instrument.setPrice(price);
                instrument.setType(type);
                instrument.setFamily(family);
                instrument.setAge(age);
                instrument.setOwnerId(ownerId);
                return instrument;
        }

        private void createMusicSheets() {
                MusicSheet[] sheets = {
                                createMusicSheet("Moonlight Sonata", "Ludwig van Beethoven", "Piano solo masterpiece",
                                                "CLASSICAL",
                                                12.99, "Piano", 15.5f, 1),
                                createMusicSheet("Für Elise", "Ludwig van Beethoven", "Popular piano piece",
                                                "CLASSICAL", 8.99, "Piano",
                                                3.5f, 1),
                                createMusicSheet("Canon in D", "Johann Pachelbel", "Beautiful baroque composition",
                                                "CLASSICAL", 10.99,
                                                "String Ensemble", 5.0f, 2),
                                createMusicSheet("Take Five", "Dave Brubeck", "Jazz standard in 5/4 time", "JAZZ",
                                                14.99,
                                                "Jazz Quartet", 5.5f, 2),
                                createMusicSheet("Autumn Leaves", "Joseph Kosma", "Classic jazz ballad", "JAZZ", 11.99,
                                                "Piano/Vocal",
                                                4.0f, 3),
                                createMusicSheet("Bohemian Rhapsody", "Queen", "Epic rock opera", "ROCK", 16.99,
                                                "Full Band", 6.0f, 3),
                                createMusicSheet("Stairway to Heaven", "Led Zeppelin", "Legendary rock ballad", "ROCK",
                                                15.99,
                                                "Guitar/Band", 8.0f, 4),
                                createMusicSheet("Let It Be", "The Beatles", "Timeless pop ballad", "POP", 12.99,
                                                "Piano/Vocal", 4.0f,
                                                4),
                                createMusicSheet("Imagine", "John Lennon", "Iconic peace anthem", "POP", 11.99,
                                                "Piano/Vocal", 3.5f, 5),
                                createMusicSheet("Sweet Child O' Mine", "Guns N' Roses", "Classic rock anthem", "ROCK",
                                                14.99,
                                                "Full Band", 5.5f, 5),
                                createMusicSheet("The Entertainer", "Scott Joplin", "Ragtime piano classic", "JAZZ",
                                                9.99, "Piano",
                                                4.0f, 6),
                                createMusicSheet("Summertime", "George Gershwin", "Jazz standard from Porgy and Bess",
                                                "JAZZ", 13.99,
                                                "Vocal/Piano", 4.5f, 6),
                                createMusicSheet("Nocturne Op. 9 No. 2", "Frédéric Chopin", "Romantic piano nocturne",
                                                "CLASSICAL",
                                                11.99, "Piano", 5.0f, 7),
                                createMusicSheet("The Thrill Is Gone", "B.B. King", "Blues guitar classic", "BLUES",
                                                12.99,
                                                "Guitar/Band", 4.5f, 7),
                                createMusicSheet("Crossroads", "Robert Johnson", "Delta blues standard", "BLUES", 10.99,
                                                "Guitar/Vocal",
                                                3.0f, 8)
                };

                musicSheetRepository.saveAll(Arrays.asList(sheets));
                System.out.println("Created " + sheets.length + " music sheets");
        }

        private MusicSheet createMusicSheet(String name, String composer, String description,
                        String category, Double price, String instrumentation,
                        Float duration, int ownerId) {
                MusicSheet sheet = new MusicSheet();
                sheet.setName(name);
                sheet.setComposer(composer);
                sheet.setDescription(description);
                sheet.setCategory(category);
                sheet.setPrice(price);
                sheet.setInstrumentation(instrumentation);
                sheet.setDuration(duration);
                sheet.setOwnerId(ownerId);
                return sheet;
        }
}
