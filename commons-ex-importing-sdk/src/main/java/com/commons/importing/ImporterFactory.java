package com.commons.importing;

public interface ImporterFactory<T> {
    Importer<T> createImporter();
}
