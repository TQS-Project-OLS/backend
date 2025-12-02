package com.example.OLSHEETS.data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class FileReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String path;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @JsonBackReference
    private Item item;

    public FileReference() {}

    public FileReference(String type, String path, Item item) {
        this.type = type;
        this.path = path;
        this.item = item;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getPath() { return path; }
    public Item getItem() { return item; }
    
    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setPath(String path) { this.path = path; }
    public void setItem(Item item) { this.item = item; }
}
