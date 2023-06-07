package com.electrodiux.register;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Register<T extends Registrable> {

    private Map<String, T> registers;

    public Register() {
        registers = new LinkedHashMap<String, T>();
    }

    public void register(T value) {
        Objects.requireNonNull(value, "The register value cannot be null");

        String registerName = value.getRegistryName();
        Objects.requireNonNull(registerName, "The register name cannot be null");
        if (containsRegister(registerName))
            throw new IllegalStateException("The register " + registerName + " already exists");

        registers.put(value.getRegistryName(), value);
    }

    @SafeVarargs
    public final void registerAll(T... values) {
        for (T value : values) {
            register(value);
        }
    }

    public boolean containsRegister(String registryName) {
        return registers.containsKey(registryName);
    }

    public T getRegister(String registryName) {
        return registers.get(registryName);
    }

    public void unregister(String registryName) {
        registers.remove(registryName);
    }

}
