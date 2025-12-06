package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.FileReference;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    public List<Instrument> searchInstrumentsByName(String name) {
        return instrumentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Instrument> filterInstrumentsByType(InstrumentType type) {
        return instrumentRepository.findByType(type);
    }
    public List<Instrument> filterInstrumentsByFamily(InstrumentFamily family) {
        return instrumentRepository.findByFamily(family);
    }

    public List<MusicSheet> searchMusicSheetsByName(String name) {
        return musicSheetRepository.findByNameContainingIgnoreCase(name);
    }

    public List<MusicSheet> filterMusicSheetsByCategory(String category) {
        return musicSheetRepository.findByCategory(category);
    }

    // Pricing management methods
    private Optional<Item> findItemById(Long itemId) {
        Optional<Item> item = instrumentRepository.findById(itemId).map(i -> (Item) i);
        if (item.isEmpty()) {
            item = musicSheetRepository.findById(itemId).map(m -> (Item) m);
        }
        return item;
    }

    public Item updateItemPrice(Long itemId, Double newPrice) {
        if (newPrice == null || newPrice < 0) {
            throw new IllegalArgumentException("Price must be a positive number");
        }

        Item item = findItemById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));
        
        item.setPrice(newPrice);
        
        // Save based on item type
        if (item instanceof Instrument instrument) {
            return instrumentRepository.save(instrument);
        } else if (item instanceof MusicSheet musicSheet) {
            return musicSheetRepository.save(musicSheet);
        }
        
        throw new IllegalStateException("Unknown item type");
    }

    public Double getItemPrice(Long itemId) {
        Item item = findItemById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));
        return item.getPrice();
    }

    public Instrument registerInstrument(InstrumentRegistrationRequest request) {
        Instrument instrument = new Instrument();
        instrument.setName(request.getName());
        instrument.setDescription(request.getDescription());
        instrument.setPrice(request.getPrice());
        instrument.setOwnerId(request.getOwnerId());
        instrument.setAge(request.getAge());
        instrument.setType(request.getType());
        instrument.setFamily(request.getFamily());

        // Create file references for photos if provided
        if (request.getPhotoPaths() != null && !request.getPhotoPaths().isEmpty()) {
            List<FileReference> fileReferences = new ArrayList<>();
            for (String photoPath : request.getPhotoPaths()) {
                FileReference fileRef = new FileReference("photo", photoPath, instrument);
                fileReferences.add(fileRef);
            }
            instrument.setFileReferences(fileReferences);
        }

        return instrumentRepository.save(instrument);
    }
}
