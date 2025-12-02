package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<MusicSheet> filterMusicSheetsByCategory(SheetCategory category) {
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
        if (item instanceof Instrument) {
            return instrumentRepository.save((Instrument) item);
        } else if (item instanceof MusicSheet) {
            return musicSheetRepository.save((MusicSheet) item);
        }
        
        throw new IllegalStateException("Unknown item type");
    }

    public Double getItemPrice(Long itemId) {
        Item item = findItemById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));
        return item.getPrice();
    }
}
