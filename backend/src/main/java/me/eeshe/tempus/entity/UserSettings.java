package me.eeshe.tempus.entity;

import java.util.Currency;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import me.eeshe.tempus.entity.converter.CurrencyConverter;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    public static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "background", column = @Column(name = "background_color", nullable = false, length = 7)),
            @AttributeOverride(name = "surface", column = @Column(name = "surface_color", nullable = false, length = 7)),
            @AttributeOverride(name = "text", column = @Column(name = "text_color", nullable = false, length = 7)),
            @AttributeOverride(name = "accent", column = @Column(name = "accent_color", nullable = false, length = 7)),
            @AttributeOverride(name = "danger", column = @Column(name = "danger_color", nullable = false, length = 7))
    })
    private Theme theme;

    @Convert(converter = CurrencyConverter.class)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency = DEFAULT_CURRENCY;

    public UserSettings() {
    }

    public UserSettings(User user) {
        this.user = user;
        this.theme = Theme.DEFAULT;
        this.currency = DEFAULT_CURRENCY;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserSettings other = (UserSettings) obj;
        return id == other.id;
    }
}
