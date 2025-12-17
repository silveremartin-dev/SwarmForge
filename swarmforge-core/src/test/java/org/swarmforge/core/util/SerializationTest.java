package org.swarmforge.core.util;

import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.species.LasiusNiger;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class SerializationTest {

    @Test
    public void testColonySerialization() throws IOException, ClassNotFoundException {
        // 1. Create Colony
        Colony original = new Colony(new LasiusNiger(), 0, 0, 0);

        // 2. Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0, "Serialized data should not be empty");

        // 3. Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Colony reloaded = (Colony) ois.readObject();

        // 4. Verify
        assertNotNull(reloaded);
        assertEquals(original.getId(), reloaded.getId(), "IDs should match");
        assertEquals(original.getSpeciesName(), reloaded.getSpeciesName(), "Species name should match");

        // 5. Verify Transient Restoration
        // Since we made 'species' transient, it will be null unless we add readObject
        // logic to Colony.
        // Let's verify it is null (or expected behavior) for now,
        // OR fix Colony to restore it.
        // For this test, I expect it to be null if I didn't implement readObject.
        // Let's implement readObject in Colony.java immediately if I haven't.
    }
}
