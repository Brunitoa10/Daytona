package com.daytona.input.binding;

import java.util.Optional;

/** Persistencia de perfiles de control por dispositivo. */
public interface BindingRepository {

    public Optional<BindingProfile> find(String deviceKey);

    public void save(BindingProfile profile);

    public void delete(String deviceKey);
}
