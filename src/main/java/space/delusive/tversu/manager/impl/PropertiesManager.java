package space.delusive.tversu.manager.impl;

import lombok.extern.log4j.Log4j2;
import space.delusive.tversu.exception.PropertiesException;
import space.delusive.tversu.manager.DataManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Класс для работы с property-файлами
 *
 * @author Delusive-
 * @version 1.1
 */

@Log4j2
public class PropertiesManager implements DataManager {
    private final Properties properties = new Properties();
    private final InputStream inputStream;

    public PropertiesManager(String fileName) {
        inputStream = getClass().getResourceAsStream(fileName);
        if (inputStream == null) throw new PropertiesException("File \"" + fileName + "\" not found!");
        try {
            properties.load(inputStream);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    /**
     * Получение int-значения из property файла
     *
     * @param key Ключ
     * @return Значение
     * @throws PropertiesException Либо если property-файл не загружен, либо если передан null
     */
    public int getInt(String key) throws PropertiesException {
        return Integer.parseInt(getString(key));
    }

    /**
     * Получение String-значения из property файла
     *
     * @param key Ключ
     * @return Значение
     * @throws PropertiesException Либо если property-файл не загружен, либо если передан null
     */
    public String getString(String key) throws PropertiesException {
        baseCheck(key);
        return new String(properties.getProperty(key).getBytes(StandardCharsets.ISO_8859_1));
    }

    private void baseCheck(String... keys) throws PropertiesException {
        for (String key : keys) {
            if (key == null) throw new PropertiesException("Each parameter must be not null!");
        }
        if (inputStream == null) throw new PropertiesException("Properties file not loaded!");
    }
}

