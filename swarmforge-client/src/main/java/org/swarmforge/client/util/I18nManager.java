package org.swarmforge.client.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages Internationalization (I18n) for the SwarmForge Client.
 * Supports dynamic locale switching and UTF-8 property resource bundles.
 */
public class I18nManager {
    private static final Logger LOG = Logger.getLogger(I18nManager.class.getName());
    private static final I18nManager INSTANCE = new I18nManager();

    private final ObjectProperty<Locale> locale;
    private final List<Locale> supportedLocales;
    private ResourceBundle bundle;

    private I18nManager() {
        supportedLocales = Arrays.asList(
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.forLanguageTag("es"),
            Locale.forLanguageTag("de"),
            Locale.forLanguageTag("zh")
        );
        locale = new SimpleObjectProperty<>(Locale.getDefault());
        locale.addListener((observable, oldValue, newValue) -> loadBundle(newValue));
        loadBundle(Locale.getDefault());
    }

    public static I18nManager getInstance() {
        return INSTANCE;
    }

    private void loadBundle(Locale locale) {
        try {
            // Custom Control to read properties as UTF-8
            this.bundle = ResourceBundle.getBundle("i18n.messages", locale, new UTF8Control());
        } catch (MissingResourceException e) {
            LOG.warning("Could not find resource bundle for locale " + locale + ", falling back to default.");
            if (this.bundle == null) {
                // Fallback to English if completely failed
                this.bundle = ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH, new UTF8Control());
            }
        }
    }

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public String get(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Creates a StringBinding that automatically updates when the locale changes.
     * Useful for binding UI text properties.
     */
    public StringBinding createStringBinding(String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    // Inner class to handle UTF-8 properties files
    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    bundle = new PropertyResourceBundle(reader);
                }
            }
            return bundle;
        }
    }
}
