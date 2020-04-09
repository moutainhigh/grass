package com.yanglinkui.grass.client;

public interface FallbackFactory<T> {
    T create(Throwable var1);

    public static class Default<T> implements FallbackFactory<T> {

        private final T instance;

        public Default(T instance) {
            this.instance = instance;
        }

        @Override
        public T create(Throwable var1) {
            return this.instance;
        }
    }

}

