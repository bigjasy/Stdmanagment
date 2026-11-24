package com.example.studentmanagement.support;

import java.lang.reflect.Field;

public final class TestIds {

    private TestIds() {
    }

    public static <T> T withId(T entity, long id) {
        Class<?> type = entity.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
                return entity;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }
        throw new IllegalStateException("No id field on " + entity.getClass().getName());
    }
}
