package com.daytona.input.binding;

import java.util.Optional;

/** Persistencia de perfiles de control por dispositivo. */
public interface BindingRepository {

    Optional<BindingProfile> find(String deviceKey);

    void save(BindingProfile profile);

    void delete(String deviceKey);
}
