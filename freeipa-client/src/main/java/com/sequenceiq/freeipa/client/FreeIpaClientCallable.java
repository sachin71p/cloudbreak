package com.sequenceiq.freeipa.client;

import java.util.Optional;

@FunctionalInterface
public interface FreeIpaClientCallable<T> {
    T run() throws FreeIpaClientException;
}
