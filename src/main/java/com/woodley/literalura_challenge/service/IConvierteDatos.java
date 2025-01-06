package com.woodley.literalura_challenge.service;

public interface IConvierteDatos {
    <T> T convertirDatos(String json, Class<T> clase);
}
