package pl.ttsw.GameRev;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.proxy.HibernateProxy;

public class Utils {

    public static String getAllFieldsToString(Object obj, boolean recursive) {
        return objectToString(obj, recursive, new HashSet<>());
    }

    private static String objectToString(Object obj, boolean recursive, Set<Object> visited) {
        if (obj == null) {
            return "null";
        }

        // Unwrap Hibernate proxies
        if (obj instanceof HibernateProxy) {
            obj = Hibernate.unproxy(obj);
        }

        if (visited.contains(obj)) {
            return obj.getClass().getSimpleName() + " (already visited)";
        }
        visited.add(obj);

        StringBuilder result = new StringBuilder();
        Class<?> objClass = obj.getClass();

        if (obj instanceof Collection) {
            result.append(objClass.getSimpleName()).append(" [\n");
            Collection<?> collection = (Collection<?>) obj;
            for (Object element : collection) {
                result.append("  ")
                        .append(objectToString(element, recursive, visited).replaceAll("(?m)^", "    "))
                        .append("\n");
            }
            result.append("]");
            return result.toString();
        }

        result.append(objClass.getSimpleName()).append(" {\n");

        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (recursive && value != null && !shouldSkip(value.getClass())) {
                    result.append("  ").append(field.getName()).append("= {\n")
                            .append(objectToString(value, true, visited).replaceAll("(?m)^", "    "))
                            .append("\n  }\n");
                } else {
                    result.append("  ").append(field.getName()).append("='").append(value).append("'\n");
                }
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                result.append("  ").append(field.getName()).append("= (inaccessible)\n");
            }
        }
        result.append("}");
        return result.toString();
    }

    private static boolean shouldSkip(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == String.class ||
                clazz == LocalDate.class ||
                clazz == LocalDateTime.class ||
                clazz == PersistentBag.class;
    }
}
