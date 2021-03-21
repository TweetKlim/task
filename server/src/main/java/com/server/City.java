package com.server;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "cities")
public class City {
    @Column(name = "city")
    public String city;

    @Column(name = "country")
    public String country;
}
