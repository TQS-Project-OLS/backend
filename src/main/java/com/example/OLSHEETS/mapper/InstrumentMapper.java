package com.example.OLSHEETS.mapper;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.model.Instrument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstrumentMapper {

    public Instrument toModel(InstrumentEntity entity) {
        if (entity == null) {
            return null;
        }

        Instrument instrument = new Instrument();
        instrument.setId(entity.getId());
        instrument.setName(entity.getName());
        instrument.setDescription(entity.getDescription());
        instrument.setOwnerId(entity.getOwner_id());
        instrument.setPrice(entity.getPrice());
        instrument.setAge(entity.getAge());
        instrument.setType(entity.getType());
        instrument.setFamily(entity.getFamily());

        return instrument;
    }

    public InstrumentEntity toEntity(Instrument model) {
        if (model == null) {
            return null;
        }

        InstrumentEntity entity = new InstrumentEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setOwner_id(model.getOwnerId());
        entity.setPrice(model.getPrice());
        entity.setAge(model.getAge());
        entity.setType(model.getType());
        entity.setFamily(model.getFamily());

        return entity;
    }

    public List<Instrument> toModelList(List<InstrumentEntity> entities) {
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
